package garlic_heist;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class BroullonTestSourisMultijoueur extends JFrame {
    private static final int CASE = 42;
    private static final int PERIODE_MS = 500;
    private static final long INVULNERABILITE_MS = 1800;

    private final Carte carte;
    private final Joueur joueur;
    private final boolean hote;
    private final Serveur accesBot;
    private final Connection db;
    private final Bot bot;
    private final int origineX, origineY;
    private final JPanel dessin;
    private final JLabel etat = new JLabel("", SwingConstants.CENTER);
    private final ScheduledExecutorService reseau = Executors.newSingleThreadScheduledExecutor();
    private final AtomicBoolean fermeture = new AtomicBoolean(false);
    private volatile double[] positionLocale;
    private volatile Map<Integer, double[]> joueursDistants = Collections.emptyMap();
    private volatile Map<Integer, double[]> objets = Collections.emptyMap();
    private volatile int[] positionBot = null;
    private long dernierDegat = 0;

    public BroullonTestSourisMultijoueur(Joueur joueur, Carte carte, boolean hote) throws SQLException {
        this.joueur = joueur;
        this.carte = carte;
        this.hote = hote;
        int[] origine = premiereCaseAccessibleAuCentre();
        origineX = origine[0];
        origineY = origine[1];
        positionLocale = joueur.getPosition();

        // Une connexion SQL distincte de celle de Joueur, toujours utilisee par le meme thread.
        accesBot = new Serveur();
        db = accesBot.getConnexion();
        if (db == null) throw new SQLException("Connexion pour le bot impossible");

        int[] pointBot = spawnBot();
        bot = new Bot("Gardien", pointBot[0], pointBot[1], 1.0);
        initialiserBot(pointBot);
        if (hote) initialiserObjetsSurCarte();
        lireEtat();

        setTitle("Garlic Heist - " + joueur.getNom());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        add(etat, BorderLayout.NORTH);

        dessin = new JPanel() {
            @Override protected void paintComponent(Graphics graphisme) {
                super.paintComponent(graphisme);
                Graphics2D g = (Graphics2D) graphisme.create();
                carte.rendu(g);
                for (double[] p : objets.values()) {
                    int x = versCarteX(p), y = versCarteY(p);
                    if (carte.caseAccessible(x, y)) {
                        g.setColor(Color.YELLOW);
                        g.fillOval(x * CASE + 12, y * CASE + 12, 18, 18);
                        g.setColor(Color.BLACK);
                        g.drawOval(x * CASE + 12, y * CASE + 12, 18, 18);
                    }
                }
                for (Map.Entry<Integer, double[]> autre : joueursDistants.entrySet())
                    dessinerRond(g, versCarteX(autre.getValue()), versCarteY(autre.getValue()),
                            Color.RED, String.valueOf(autre.getKey()));
                if (positionBot != null)
                    dessinerRond(g, positionBot[0], positionBot[1], Color.BLACK, "B");
                dessinerRond(g, versCarteX(positionLocale), versCarteY(positionLocale),
                        Color.BLUE, String.valueOf(joueur.getIdJoueur()));
                g.dispose();
            }
        };
        dessin.setPreferredSize(new Dimension(carte.getNbColonnes() * CASE, carte.getNbLignes() * CASE));
        add(new JScrollPane(dessin), BorderLayout.CENTER);
        add(new JLabel("ZQSD : deplacer la souris | Jaune : objet | Noir : bot", SwingConstants.CENTER),
                BorderLayout.SOUTH);
        touches();
        setSize(1000, 750);
        setLocationRelativeTo(null);
        rafraichirVue();

        reseau.scheduleWithFixedDelay(() -> {
            if (fermeture.get()) return;
            try {
                if (hote) avancerBot();
                lireEtat();
                verifierCollision();
                rafraichirVue();
            } catch (SQLException ex) {
                System.err.println("Actualisation impossible : " + ex.getMessage());
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
        }, 0, PERIODE_MS, TimeUnit.MILLISECONDS);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                if (!fermeture.compareAndSet(false, true)) return;
                reseau.execute(() -> {
                    try { joueur.quitterPartie(); }
                    finally { accesBot.fermerConnexion(); }
                });
                reseau.shutdown();
            }
        });
    }

    private int[] premiereCaseAccessibleAuCentre() {
        int cx = carte.getNbColonnes() / 2, cy = carte.getNbLignes() / 2;
        int[] meilleur = null;
        int distance = Integer.MAX_VALUE;
        for (int y = 0; y < carte.getNbLignes(); y++)
            for (int x = 0; x < carte.getNbColonnes(); x++)
                if (carte.caseAccessible(x, y)) {
                    int d = Math.abs(x - cx) + Math.abs(y - cy);
                    if (d < distance) { distance = d; meilleur = new int[]{x, y}; }
                }
        if (meilleur == null) throw new IllegalStateException("Aucune case libre sur cette carte");
        return meilleur;
    }

    private boolean accessibleDepuisOrigine(int x, int y) {
        return carte.caseAccessible(x, y) &&
                (x == origineX && y == origineY ||
                        !carte.trouverChemin(origineX, origineY, x, y).isEmpty());
    }

    private int[] spawnBot() {
        int[] meilleur = {origineX, origineY};
        int distanceMax = -1;
        for (int y = 0; y < carte.getNbLignes(); y++)
            for (int x = 0; x < carte.getNbColonnes(); x++) {
                if (!accessibleDepuisOrigine(x, y)) continue;
                int d = Math.abs(x - origineX) + Math.abs(y - origineY);
                if (d > distanceMax) { distanceMax = d; meilleur = new int[]{x, y}; }
            }
        return meilleur;
    }

    private int versCarteX(double[] p) { return origineX + (int) p[0]; }
    private int versCarteY(double[] p) { return origineY - (int) p[1]; }

    private void dessinerRond(Graphics2D g, int x, int y, Color couleur, String numero) {
        if (!carte.caseAccessible(x, y)) return;
        int px = x * CASE, py = y * CASE;
        g.setColor(couleur);
        g.fillOval(px + 5, py + 5, CASE - 10, CASE - 10);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 13));
        FontMetrics m = g.getFontMetrics();
        g.drawString(numero, px + (CASE - m.stringWidth(numero)) / 2, py + CASE / 2 + 5);
    }

    private void initialiserBot(int[] p) throws SQLException {
        String sql = "INSERT IGNORE INTO bots_partie (partie_id, position_x, position_y) VALUES (?, ?, ?)";
        try (PreparedStatement q = db.prepareStatement(sql)) {
            q.setInt(1, joueur.getIdPartie()); q.setInt(2, p[0]); q.setInt(3, p[1]);
            q.executeUpdate();
        }
        // Si le bot existe deja, l'hote reprend sa position partagee.
        int[] partage = lirePositionBot();
        if (partage != null) { bot.setX(partage[0]); bot.setY(partage[1]); }
    }

    private int[] lirePositionBot() throws SQLException {
        try (PreparedStatement q = db.prepareStatement(
                "SELECT position_x, position_y FROM bots_partie WHERE partie_id = ?")) {
            q.setInt(1, joueur.getIdPartie());
            try (ResultSet r = q.executeQuery()) {
                return r.next() ? new int[]{r.getInt(1), r.getInt(2)} : null;
            }
        }
    }

    private void avancerBot() throws SQLException {
        Map<Integer, double[]> positions = joueur.recupererPositions();
        positions.put(joueur.getIdJoueur(), joueur.getPosition());
        int[] cible = null;
        int plusCourt = Integer.MAX_VALUE;
        for (double[] p : positions.values()) {
            int x = versCarteX(p), y = versCarteY(p);
            if (!carte.caseAccessible(x, y)) continue;
            int distance = Math.abs(x - bot.getX()) + Math.abs(y - bot.getY());
            if (distance < plusCourt) { plusCourt = distance; cible = new int[]{x, y}; }
        }
        if (cible != null && bot.deplacerVers(carte, cible[0], cible[1])) bot.avancer();
        try (PreparedStatement q = db.prepareStatement(
                "UPDATE bots_partie SET position_x=?, position_y=? WHERE partie_id=?")) {
            q.setInt(1, bot.getX()); q.setInt(2, bot.getY()); q.setInt(3, joueur.getIdPartie());
            q.executeUpdate();
        }
    }

    private void initialiserObjetsSurCarte() throws SQLException {
        double[][] positions = new Objets().getNourritures();
        Set<String> dejaPris = new HashSet<>();
        for (int i = 0; i < positions.length; i++) {
            int cibleX = origineX + (int) positions[i][0];
            int cibleY = origineY - (int) positions[i][1];
            int[] place = null;
            int meilleureDistance = Integer.MAX_VALUE;
            for (int y = 0; y < carte.getNbLignes(); y++)
                for (int x = 0; x < carte.getNbColonnes(); x++) {
                    if (!accessibleDepuisOrigine(x, y) ||
                            (x == origineX && y == origineY) ||
                            dejaPris.contains(x + ":" + y)) continue;
                    int distance = Math.abs(x - cibleX) + Math.abs(y - cibleY);
                    if (distance < meilleureDistance) {
                        meilleureDistance = distance;
                        place = new int[]{x, y};
                    }
                }
            if (place == null) continue;
            dejaPris.add(place[0] + ":" + place[1]);
            // Stocker des coordonnees relatives, comme dans Joueur.ramasserObjets().
            try (PreparedStatement q = db.prepareStatement(
                    "INSERT IGNORE INTO objets_partie " +
                    "(partie_id, objet_id, position_x, position_y) VALUES (?, ?, ?, ?)")) {
                q.setInt(1, joueur.getIdPartie());
                q.setInt(2, i + 1);
                q.setInt(3, place[0] - origineX);
                q.setInt(4, origineY - place[1]);
                q.executeUpdate();
            }
        }
    }

    private void lireEtat() throws SQLException {
        joueursDistants = new HashMap<>(joueur.recupererPositions());
        objets = new HashMap<>(joueur.recupererObjetsDisponibles());
        positionBot = lirePositionBot();
        positionLocale = joueur.getPosition();
    }

    private void verifierCollision() {
        int[] b = positionBot;
        if (b == null || joueur.getVie() <= 0) return;
        double[] p = joueur.getPosition();
        if (versCarteX(p) == b[0] && versCarteY(p) == b[1] &&
                System.currentTimeMillis() - dernierDegat > INVULNERABILITE_MS) {
            dernierDegat = System.currentTimeMillis();
            joueur.verifierEtMettreAJourVie(joueur.getVie() - 1);
            System.out.println("Le bot vous touche ! Vies restantes : " + joueur.getVie());
        }
    }

    private void rafraichirVue() {
        SwingUtilities.invokeLater(() -> {
            if (fermeture.get()) return;
            etat.setText("Partie " + joueur.getIdPartie() + " | " + joueur.getNom() +
                    " | Vie : " + joueur.getVie() + " | " +
                    (hote ? "Hote" : "Invite"));
            dessin.repaint();
            double[] p = positionLocale;
            dessin.scrollRectToVisible(new Rectangle(
                    versCarteX(p) * CASE, versCarteY(p) * CASE, CASE, CASE));
        });
    }

    private void touches() {
        touche(KeyEvent.VK_Z, "haut", 0, -1);
        touche(KeyEvent.VK_S, "bas", 0, 1);
        touche(KeyEvent.VK_Q, "gauche", -1, 0);
        touche(KeyEvent.VK_D, "droite", 1, 0);
    }

    private void touche(int code, String action, int dx, int dy) {
        dessin.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(code, 0), action);
        dessin.getActionMap().put(action, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (fermeture.get()) return;
                reseau.execute(() -> {
                    double[] p = joueur.getPosition();
                    int nx = versCarteX(p) + dx, ny = versCarteY(p) + dy;
                    if (!carte.caseAccessible(nx, ny)) return;
                    switch (action) {
                        case "haut": joueur.deplacerHaut(); break;
                        case "bas": joueur.deplacerBas(); break;
                        case "gauche": joueur.deplacerGauche(); break;
                        case "droite": joueur.deplacerDroite(); break;
                    }
                    try {
                        joueur.ramasserObjets();
                        lireEtat();
                        verifierCollision();
                        rafraichirVue();
                    } catch (SQLException ex) {
                        System.err.println("Erreur deplacement/ramassage : " + ex.getMessage());
                    }
                });
            }
        });
    }

    public static void main(String[] args) {
        Joueur joueur = null;
        try {
            String nom = JOptionPane.showInputDialog(null, "Nom du joueur :");
            if (nom == null || nom.isBlank()) return;
            String choix = JOptionPane.showInputDialog(null,
                    "1 - Creer une partie\n2 - Rejoindre une partie");
            if (choix == null) return;
            boolean hote = choix.trim().equals("1");
            if (!hote && !choix.trim().equals("2")) return;
            int id;
            if (hote) {
                id = Joueur.creerPartie();
                JOptionPane.showMessageDialog(null, "Code de partie : " + id);
            } else {
                String saisie = JOptionPane.showInputDialog(null, "Code de partie :");
                if (saisie == null) return;
                id = Integer.parseInt(saisie.trim());
            }
            Carte carte = new Carte(0);
            joueur = new Joueur(nom, id);
            BroullonTestSourisMultijoueur fenetre =
                    new BroullonTestSourisMultijoueur(joueur, carte, hote);
            fenetre.setVisible(true);
        } catch (Exception ex) {
            if (joueur != null) joueur.fermerConnexion();
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erreur : " + ex.getMessage());
        }
    }
}
