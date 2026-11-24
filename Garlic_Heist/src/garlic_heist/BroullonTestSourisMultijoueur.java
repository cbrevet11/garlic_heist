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
    private static final int CASE = Carte.TAILLE;
    private static final int PERIODE_MS = 200; // Bot et actualisation : 5 fois par seconde
    private static final int DEPLACEMENT_MS = 120; // Un pas toutes les 120 ms pendant le maintien
    private static final long INVULNERABILITE_MS = 1800;
    private static final long REAPPARITION_OBJET_MS = 3000;

    private final Carte carte;
    private final Joueur joueur;
    private final boolean hote;
    private final Serveur accesBot;
    private final Connection db;
    private final Bot bot;
    private final Cuisine cuisine;
    // Le calcul des points est confie a la classe Score existante.
    private final Score score = new Score();
    private volatile Map<Integer, Integer> scoresPartie = Collections.emptyMap();
    // Les types d'ingredients proviennent exclusivement de Objets.
    private final Objets definitionObjets = new Objets();
    private final int tableX, tableY;
    private volatile String infoCuisine = "";
    private final int origineX, origineY;
    private final JPanel dessin;
    private final JLabel etat = new JLabel("", SwingConstants.CENTER);
    private final ScheduledExecutorService reseau = Executors.newSingleThreadScheduledExecutor();
    private final AtomicBoolean fermeture = new AtomicBoolean(false);
    // Les touches sont gerees sur le thread graphique Swing (EDT).
    // La derniere touche appuyee a priorite si plusieurs sont maintenues.
    private final LinkedHashSet<Integer> touchesEnfoncees = new LinkedHashSet<>();
    private final javax.swing.Timer minuterieDeplacement;
    private final AtomicBoolean deplacementEnCours = new AtomicBoolean(false);
    private volatile int toucheActive = -1;
    private volatile double[] positionLocale;
    private volatile Map<Integer, double[]> joueursDistants = Collections.emptyMap();
    private volatile Map<Integer, double[]> objets = Collections.emptyMap();
    private volatile int[] positionBot = null;
    private long dernierDegat = 0;
    // L'hote gere seul le replacement des objets deja ramasses.
    private final Map<Integer, Long> objetsEnAttente = new HashMap<>();
    private final Random aleatoire = new Random();
    // Historique local du joueur, comme la liste A/B/C de Testsouris.
    private final List<Integer> ramassagesLocaux = new java.util.concurrent.CopyOnWriteArrayList<>();

    public BroullonTestSourisMultijoueur(Joueur joueur, Carte carte, boolean hote) throws SQLException {
        this.joueur = joueur;
        this.carte = carte;
        this.hote = hote;
        int[] origine = premiereCaseAccessibleAuCentre();
        origineX = origine[0];
        origineY = origine[1];
        int[] table = trouverTable();
        tableX = table[0];
        tableY = table[1];
        positionLocale = joueur.getPosition();

        // Une connexion SQL distincte de celle de Joueur, toujours utilisee par le meme thread.
        accesBot = new Serveur();
        db = accesBot.getConnexion();
        if (db == null) throw new SQLException("Connexion pour le bot impossible");
        cuisine = new Cuisine(db, joueur.getIdPartie(), joueur.getIdJoueur());
        if (definitionObjets.getNourritures() == null
                || definitionObjets.getNourritures().length != cuisine.getNombreIngredients()) {
            throw new IllegalStateException("Incoherence entre Objets et Cuisine");
        }
        actualiserInfoCuisine();
        initialiserScore();

        int[] pointBot = spawnBot();
        bot = new Bot("Gardien", pointBot[0], pointBot[1], 1.0);
        if (hote) {
            initialiserBot(pointBot);
            initialiserObjetsSurCarte();
        }
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
                // Table de cuisine provisoire sur une case accessible.
                g.setColor(new Color(145, 91, 41));
                g.fillRoundRect(tableX * CASE + 4, tableY * CASE + 4, CASE - 8, CASE - 8, 8, 8);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 13));
                g.drawString("T", tableX * CASE + 16, tableY * CASE + 27);
                for (Map.Entry<Integer, double[]> objet : objets.entrySet()) {
                    double[] p = objet.getValue();
                    int x = versCarteX(p), y = versCarteY(p);
                    if (carte.caseAccessible(x, y)) {
                        g.setColor(Color.YELLOW);
                        g.fillOval(x * CASE + 7, y * CASE + 7, CASE - 14, CASE - 14);
                        g.setColor(Color.BLACK);
                        g.drawOval(x * CASE + 7, y * CASE + 7, CASE - 14, CASE - 14);
                        g.setFont(new Font("Arial", Font.BOLD, 13));
                        String lettre = nomObjet(objet.getKey());
                        FontMetrics m = g.getFontMetrics();
                        g.drawString(lettre, x * CASE + (CASE - m.stringWidth(lettre)) / 2,
                                y * CASE + CASE / 2 + 5);
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
        add(new JLabel("Maintenir ZQSD : deplacement | T : table / cuisson 10 s | E : cuisiner | I : stock", SwingConstants.CENTER),
                BorderLayout.SOUTH);
        touches();
        minuterieDeplacement = new javax.swing.Timer(DEPLACEMENT_MS, e -> deplacerSiMaintenu());
        minuterieDeplacement.setCoalesce(true);
        setSize(1000, 750);
        setLocationRelativeTo(null);
        rafraichirVue();

        reseau.scheduleWithFixedDelay(() -> {
            if (fermeture.get()) return;
            try {
                if (hote) {
                    avancerBot();
                    replacerObjetsRamasses();
                }
                lireEtat();
                scoresPartie = lireScores();
                // Demarrage automatique si la souris est sur T et possede A, B et C.
                if (!cuisine.enCours() && estSurTable() && cuisine.commencer(true)) {
                    System.out.println("Cuisson automatique commencee : 10 secondes sur la table.");
                }
                if (cuisine.actualiser(estSurTable())) {
                    System.out.println("Plat prepare ! Un exemplaire de A, B et C a ete consomme.");
                    score.fabriquerPlat();
                    enregistrerScore();
                    actualiserInfoCuisine();
                } else if (cuisine.enCours()) {
                    infoCuisine = "Cuisson : " + cuisine.secondesRestantes() + " s";
                }
                verifierCollision();
                rafraichirVue();
            } catch (SQLException ex) {
                System.err.println("Actualisation impossible : " + ex.getMessage());
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
        }, 0, PERIODE_MS, TimeUnit.MILLISECONDS);

        addWindowFocusListener(new WindowAdapter() {
            @Override public void windowLostFocus(WindowEvent e) {
                toucheActive = -1;
                touchesEnfoncees.clear();
                minuterieDeplacement.stop();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                if (!fermeture.compareAndSet(false, true)) return;
                minuterieDeplacement.stop();
                toucheActive = -1;
                touchesEnfoncees.clear();
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

    // Case de test pour la table, proche du depart et atteignable a pied.
    private int[] trouverTable() {
        int[] resultat = {origineX, origineY};
        int meilleureDistance = Integer.MAX_VALUE;
        for (int y = 0; y < carte.getNbLignes(); y++) {
            for (int x = 0; x < carte.getNbColonnes(); x++) {
                if (!accessibleDepuisOrigine(x, y)) continue;
                int d = Math.abs(x - origineX) + Math.abs(y - origineY);
                if (d > 0 && d < meilleureDistance) {
                    meilleureDistance = d;
                    resultat = new int[]{x, y};
                }
            }
        }
        return resultat;
    }

    private boolean estSurTable() {
        double[] p = joueur.getPosition();
        return versCarteX(p) == tableX && versCarteY(p) == tableY;
    }

    private void actualiserInfoCuisine() throws SQLException {
        int[] stock = cuisine.stock();
        StringBuilder texte = new StringBuilder("Ingredients : ");
        for (int i = 0; i < stock.length; i++) {
            if (i > 0) texte.append("  ");
            texte.append(nomObjet(i + 1)).append(":").append(stock[i]);
        }
        infoCuisine = texte.append(" | Plats:").append(cuisine.nombrePlats()).toString();
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
        if (cible != null && bot.deplacerVers(carte, cible[0], cible[1])) {
            bot.avancer();
        }
        try (PreparedStatement q = db.prepareStatement(
                "UPDATE bots_partie SET position_x=?, position_y=? WHERE partie_id=?")) {
            q.setInt(1, bot.getX()); q.setInt(2, bot.getY()); q.setInt(3, joueur.getIdPartie());
            q.executeUpdate();
        }
    }

    private void initialiserObjetsSurCarte() throws SQLException {
        double[][] positions = definitionObjets.getNourritures();
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
                            (x == tableX && y == tableY) ||
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

    private static String nomObjet(int id) {
        // Meme notation A, B, C que Testsouris.
        return id >= 1 && id <= 26 ? String.valueOf((char) ('A' + id - 1)) : "O" + id;
    }

    // L'hote replace les objets ramasses sur une case accessible et libre.
    // Le delai permet aux deux ordinateurs de constater leur disparition.
    private void replacerObjetsRamasses() throws SQLException {
        long maintenant = System.currentTimeMillis();
        Set<Integer> encoreRamasses = new HashSet<>();
        try (PreparedStatement q = db.prepareStatement(
                "SELECT objet_id FROM objets_partie WHERE partie_id=? AND ramasse_par IS NOT NULL")) {
            q.setInt(1, joueur.getIdPartie());
            try (ResultSet r = q.executeQuery()) {
                while (r.next()) {
                    int id = r.getInt(1);
                    encoreRamasses.add(id);
                    objetsEnAttente.putIfAbsent(id, maintenant);
                }
            }
        }
        objetsEnAttente.keySet().retainAll(encoreRamasses);

        // Cases occupees par les autres objets, joueurs et le bot.
        Set<String> occupees = new HashSet<>();
        for (double[] p : joueur.recupererObjetsDisponibles().values()) {
            occupees.add(versCarteX(p) + ":" + versCarteY(p));
        }
        Map<Integer, double[]> positions = joueur.recupererPositions();
        positions.put(joueur.getIdJoueur(), joueur.getPosition());
        for (double[] p : positions.values()) {
            occupees.add(versCarteX(p) + ":" + versCarteY(p));
        }
        int[] pb = lirePositionBot();
        if (pb != null) occupees.add(pb[0] + ":" + pb[1]);

        for (Integer id : new ArrayList<>(objetsEnAttente.keySet())) {
            if (maintenant - objetsEnAttente.get(id) < REAPPARITION_OBJET_MS) continue;
            List<int[]> libres = new ArrayList<>();
            for (int y = 0; y < carte.getNbLignes(); y++) {
                for (int x = 0; x < carte.getNbColonnes(); x++) {
                    if (accessibleDepuisOrigine(x, y) && !(x == tableX && y == tableY)
                            && !occupees.contains(x + ":" + y)) {
                        libres.add(new int[]{x, y});
                    }
                }
            }
            if (libres.isEmpty()) continue;
            int[] p = libres.get(aleatoire.nextInt(libres.size()));
            try (PreparedStatement q = db.prepareStatement(
                    "UPDATE objets_partie SET position_x=?, position_y=?, ramasse_par=NULL " +
                    "WHERE partie_id=? AND objet_id=? AND ramasse_par IS NOT NULL")) {
                q.setInt(1, p[0] - origineX);
                q.setInt(2, origineY - p[1]);
                q.setInt(3, joueur.getIdPartie());
                q.setInt(4, id);
                if (q.executeUpdate() == 1) {
                    occupees.add(p[0] + ":" + p[1]);
                    objetsEnAttente.remove(id);
                    System.out.println("Objet " + nomObjet(id) + " replace sur la carte.");
                }
            }
        }
    }

    // Les ramassages de Testsouris restent visibles, sans classe Inventaire.
    private Set<Integer> objetsRamassesParMoi() throws SQLException {
        Set<Integer> ids = new HashSet<>();
        try (PreparedStatement q = db.prepareStatement(
                "SELECT objet_id FROM objets_partie WHERE partie_id=? AND ramasse_par=?")) {
            q.setInt(1, joueur.getIdPartie());
            q.setInt(2, joueur.getIdJoueur());
            try (ResultSet r = q.executeQuery()) {
                while (r.next()) ids.add(r.getInt(1));
            }
        }
        return ids;
    }

    private void enregistrerRamassagesLocaux(Set<Integer> avant) throws SQLException {
        Set<Integer> apres = objetsRamassesParMoi();
        apres.removeAll(avant);
        for (int id : apres) {
            cuisine.ajouterIngredient(id);
            actualiserInfoCuisine();
            ramassagesLocaux.add(id);
            score.ajouterPointsObjet();
            enregistrerScore();
            System.out.println(joueur.getNom() + " a ramasse l'objet " + nomObjet(id));
        }
    }

    private void lireEtat() throws SQLException {
        joueursDistants = new HashMap<>(joueur.recupererPositions());
        objets = new HashMap<>(joueur.recupererObjetsDisponibles());
        positionBot = lirePositionBot();
        positionLocale = joueur.getPosition();
    }

    // Scores partages via MariaDB. Le calcul reste exclusivement dans Score.java.
    private void initialiserScore() throws SQLException {
        try (PreparedStatement q = db.prepareStatement(
                "INSERT INTO scores_partie (partie_id, joueur_id, points) "
                + "VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE points = VALUES(points)")) {
            q.setInt(1, joueur.getIdPartie());
            q.setInt(2, joueur.getIdJoueur());
            q.setInt(3, score.getScoreTotal());
            q.executeUpdate();
        }
    }

    private void enregistrerScore() throws SQLException {
        try (PreparedStatement q = db.prepareStatement(
                "UPDATE scores_partie SET points = ? "
                + "WHERE partie_id = ? AND joueur_id = ?")) {
            q.setInt(1, score.getScoreTotal());
            q.setInt(2, joueur.getIdPartie());
            q.setInt(3, joueur.getIdJoueur());
            q.executeUpdate();
        }
    }

    private Map<Integer, Integer> lireScores() throws SQLException {
        Map<Integer, Integer> scores = new TreeMap<>();
        try (PreparedStatement q = db.prepareStatement(
                "SELECT joueur_id, points FROM scores_partie WHERE partie_id = ?")) {
            q.setInt(1, joueur.getIdPartie());
            try (ResultSet r = q.executeQuery()) {
                while (r.next()) scores.put(r.getInt(1), r.getInt(2));
            }
        }
        return scores;
    }

    private String texteScores() {
        Map<Integer, Integer> snapshot = scoresPartie;
        if (snapshot.isEmpty()) return "en attente";
        StringJoiner texte = new StringJoiner(" / ");
        snapshot.forEach((id, points) -> texte.add("J" + id + ":" + points));
        return texte.toString();
    }

    private void verifierCollision() {
        int[] b = positionBot;
        if (b == null || joueur.getVie() <= 0) return;
        double[] p = joueur.getPosition();
        if (versCarteX(p) == b[0] && versCarteY(p) == b[1] &&
                System.currentTimeMillis() - dernierDegat > INVULNERABILITE_MS) {
            dernierDegat = System.currentTimeMillis();
            joueur.verifierEtMettreAJourVie(joueur.getVie() - 1);
            score.perdrePointsVie();
            try {
                enregistrerScore();
            } catch (SQLException ex) {
                System.err.println("Erreur de synchronisation du score : " + ex.getMessage());
            }
            System.out.println("Le bot vous touche ! Vies restantes : " + joueur.getVie());
        }
    }

    private void rafraichirVue() {
        SwingUtilities.invokeLater(() -> {
            if (fermeture.get()) return;
            etat.setText("Niveau " + (carte.getNumeroNiveau() + 1)
                    + " | Partie " + joueur.getIdPartie() + " | " + joueur.getNom()
                    + " | Vie : " + joueur.getVie()
                    + " | " + (hote ? "Hote" : "Invite")
                    + " | Ramassages : " + ramassagesLocaux.size()
                    + " | Score : " + score.getScoreTotal()
                    + " | Scores partie : " + texteScores()
                    + " | " + infoCuisine);
            dessin.repaint();
            double[] p = positionLocale;
            dessin.scrollRectToVisible(new Rectangle(
                    versCarteX(p) * CASE, versCarteY(p) * CASE, CASE, CASE));
        });
    }

    private void touches() {
        touche(KeyEvent.VK_Z, "haut");
        touche(KeyEvent.VK_S, "bas");
        touche(KeyEvent.VK_Q, "gauche");
        touche(KeyEvent.VK_D, "droite");
        dessin.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0), "cuisiner");
        dessin.getActionMap().put("cuisiner", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                reseau.execute(() -> {
                    try {
                        if (!estSurTable()) {
                            System.out.println("Placez votre souris sur la case T pour cuisiner.");
                        } else if (cuisine.commencer(true)) {
                            System.out.println("Cuisson lancee : restez sur la table pendant 10 secondes.");
                        } else {
                            System.out.println("Il faut 1 A, 1 B et 1 C pour cuisiner, ou une cuisson est deja en cours.");
                        }
                    } catch (SQLException ex) {
                        System.err.println("Erreur cuisine : " + ex.getMessage());
                    }
                });
            }
        });
        dessin.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_I, 0), "objets");
        dessin.getActionMap().put("objets", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                String noms = ramassagesLocaux.stream()
                        .map(BroullonTestSourisMultijoueur::nomObjet)
                        .collect(java.util.stream.Collectors.joining(", "));
                JOptionPane.showMessageDialog(BroullonTestSourisMultijoueur.this,
                        "Objets ramasses pendant ce test : " +
                                (noms.isEmpty() ? "aucun" : noms) + "\nStock : " + infoCuisine);
            }
        });
    }

    private void touche(int code, String action) {
        InputMap input = dessin.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actions = dessin.getActionMap();

        input.put(KeyStroke.getKeyStroke(code, 0, false), action + "_appui");
        input.put(KeyStroke.getKeyStroke(code, 0, true), action + "_relachement");

        actions.put(action + "_appui", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (fermeture.get()) return;
                // Les repetitions automatiques du clavier ne modifient pas l'ordre.
                if (touchesEnfoncees.add(code)) {
                    toucheActive = code;
                    if (!minuterieDeplacement.isRunning()) {
                        minuterieDeplacement.start();
                    }
                    deplacerSiMaintenu(); // Premier pas des l'appui.
                }
            }
        });

        actions.put(action + "_relachement", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                touchesEnfoncees.remove(code);
                if (touchesEnfoncees.isEmpty()) {
                    toucheActive = -1;
                    minuterieDeplacement.stop(); // Arret des le relachement.
                } else {
                    int derniere = -1;
                    for (int c : touchesEnfoncees) derniere = c;
                    toucheActive = derniere;
                }
            }
        });
    }

    // Un seul deplacement SQL a la fois pour eviter l'accumulation de requetes.
    private void deplacerSiMaintenu() {
        final int code = toucheActive;
        if (fermeture.get() || code == -1 || !deplacementEnCours.compareAndSet(false, true)) return;

        reseau.execute(() -> {
            try {
                // Si la touche a ete relachee pendant l'attente, ne pas avancer.
                if (fermeture.get() || toucheActive != code) return;

                int dx = 0, dy = 0;
                switch (code) {
                    case KeyEvent.VK_Z: dy = -1; break;
                    case KeyEvent.VK_S: dy = 1; break;
                    case KeyEvent.VK_Q: dx = -1; break;
                    case KeyEvent.VK_D: dx = 1; break;
                    default: return;
                }
                double[] p = joueur.getPosition();
                int nx = versCarteX(p) + dx, ny = versCarteY(p) + dy;
                if (!carte.caseAccessible(nx, ny)) return;
                if (fermeture.get() || toucheActive != code) return;

                switch (code) {
                    case KeyEvent.VK_Z: joueur.deplacerHaut(); break;
                    case KeyEvent.VK_S: joueur.deplacerBas(); break;
                    case KeyEvent.VK_Q: joueur.deplacerGauche(); break;
                    case KeyEvent.VK_D: joueur.deplacerDroite(); break;
                }

                if (cuisine.enCours() && !estSurTable()) {
                    cuisine.annuler();
                    System.out.println("Cuisson annulee : vous avez quitte la table.");
                    actualiserInfoCuisine();
                }
                Set<Integer> avant = objetsRamassesParMoi();
                joueur.ramasserObjets();
                enregistrerRamassagesLocaux(avant);
                lireEtat();
                scoresPartie = lireScores();
                verifierCollision();
                rafraichirVue();
            } catch (SQLException ex) {
                System.err.println("Erreur deplacement/ramassage : " + ex.getMessage());
            } finally {
                deplacementEnCours.set(false);
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
