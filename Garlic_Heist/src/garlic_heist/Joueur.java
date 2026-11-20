
package garlic_heist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class Joueur {

    private souris maSouris;
    private Serveur serveur;
    private Connection connexion;

    private int idJoueur;
    private int idPartie;
    private String nom;
    private int vie = 3;

    // Constructeur : connecter un joueur a une partie
    public Joueur(String nom, int idPartie) {

        this.nom = nom;
        this.idPartie = idPartie;
        this.maSouris = new souris();

        serveur = new Serveur();
        connexion = serveur.getConnexion();

        if (connexion == null) {
            throw new IllegalStateException(
                "Connexion a MariaDB impossible"
            );
        }

        try {
            ajouterJoueur();

            System.out.println(
                nom + " a rejoint la partie " + idPartie
            );

        } catch (SQLException e) {
            serveur.fermerConnexion();

            throw new IllegalStateException(
                "Impossible de rejoindre la partie", e
            );
        }
    }

    // Creer une nouvelle partie dans MariaDB
    public static int creerPartie() throws SQLException {

        Serveur serveur = new Serveur();

        try {
            Connection connexion = serveur.getConnexion();

            if (connexion == null) {
                throw new SQLException(
                    "Connexion a MariaDB impossible"
                );
            }

            String sql =
                "INSERT INTO parties (statut) VALUES ('attente')";

            try (PreparedStatement requete =
                    connexion.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {

                requete.executeUpdate();

                try (ResultSet resultat =
                        requete.getGeneratedKeys()) {

                    if (resultat.next()) {
                        return resultat.getInt(1);
                    }
                }
            }

            throw new SQLException(
                "Impossible de recuperer le code de partie"
            );

        } finally {
            serveur.fermerConnexion();
        }
    }

    // Ajouter le joueur dans la base
    private void ajouterJoueur() throws SQLException {

        String sql = "INSERT INTO joueurs "
                + "(pseudo, partie_id, position_x, position_y, vie) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement requete =
                connexion.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS)) {

            double[] position = maSouris.getposition();

            requete.setString(1, nom);
            requete.setInt(2, idPartie);
            requete.setDouble(3, position[0]);
            requete.setDouble(4, position[1]);
            requete.setInt(5, vie);

            requete.executeUpdate();

            try (ResultSet resultat =
                    requete.getGeneratedKeys()) {

                if (resultat.next()) {
                    idJoueur = resultat.getInt(1);
                } else {
                    throw new SQLException(
                        "ID joueur introuvable"
                    );
                }
            }
        }
    }

    // Enregistrer la position de la souris dans MariaDB
    private void envoyerPosition() {

        String sql = "UPDATE joueurs "
                + "SET position_x = ?, position_y = ? "
                + "WHERE id = ? AND partie_id = ?";

        try (PreparedStatement requete =
                connexion.prepareStatement(sql)) {

            double[] position = maSouris.getposition();

            requete.setDouble(1, position[0]);
            requete.setDouble(2, position[1]);
            requete.setInt(3, idJoueur);
            requete.setInt(4, idPartie);

            requete.executeUpdate();

        } catch (SQLException e) {
            System.out.println(
                "Erreur de synchronisation : "
                + e.getMessage()
            );
        }
    }

    // Deplacements de la souris
    public void deplacerHaut() {
        maSouris.deplacementhaut();
        envoyerPosition();
    }

    public void deplacerBas() {
        maSouris.deplacementbas();
        envoyerPosition();
    }

    public void deplacerGauche() {
        maSouris.deplacementgauche();
        envoyerPosition();
    }

    public void deplacerDroite() {
        maSouris.deplacementdroite();
        envoyerPosition();
    }

    // Recuperer les positions des autres souris
    public Map<Integer, double[]> recupererPositions()
            throws SQLException {

        Map<Integer, double[]> positions = new HashMap<>();

        String sql = "SELECT id, position_x, position_y "
                + "FROM joueurs "
                + "WHERE partie_id = ? AND id <> ?";

        try (PreparedStatement requete =
                connexion.prepareStatement(sql)) {

            requete.setInt(1, idPartie);
            requete.setInt(2, idJoueur);

            try (ResultSet resultat =
                    requete.executeQuery()) {

                while (resultat.next()) {

                    int id = resultat.getInt("id");
                    double x = resultat.getDouble("position_x");
                    double y = resultat.getDouble("position_y");

                    positions.put(
                        id,
                        new double[]{x, y}
                    );
                }
            }
        }

        return positions;
    }

    // Initialiser les objets de la partie
    public void initialiserObjets() throws SQLException {

        Objets objets = new Objets();
        double[][] positions = objets.getNourritures();

        String sql = "INSERT IGNORE INTO objets_partie "
                + "(partie_id, objet_id, position_x, position_y) "
                + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement requete =
                connexion.prepareStatement(sql)) {

            for (int i = 0; i < positions.length; i++) {

                int x = (int) positions[i][0];
                int y = (int) positions[i][1];

                // Ignorer les objets en dehors de la grille
                if (x < 0 || x >= 10 ||
                    y < 0 || y >= 10) {
                    continue;
                }

                requete.setInt(1, idPartie);
                requete.setInt(2, i + 1);
                requete.setInt(3, x);
                requete.setInt(4, y);

                requete.executeUpdate();
            }
        }
    }

    // Recuperer les objets encore disponibles
    public Map<Integer, double[]> recupererObjetsDisponibles()
            throws SQLException {

        Map<Integer, double[]> objets = new HashMap<>();

        String sql = "SELECT objet_id, position_x, position_y "
                + "FROM objets_partie "
                + "WHERE partie_id = ? AND ramasse_par IS NULL";

        try (PreparedStatement requete =
                connexion.prepareStatement(sql)) {

            requete.setInt(1, idPartie);

            try (ResultSet resultat =
                    requete.executeQuery()) {

                while (resultat.next()) {

                    int id = resultat.getInt("objet_id");
                    double x = resultat.getDouble("position_x");
                    double y = resultat.getDouble("position_y");

                    objets.put(id, new double[]{x, y});
                }
            }
        }

        return objets;
    }

    // Ramasser un objet situe sur la meme case
    public void ramasserObjets() throws SQLException {

        double[] position = maSouris.getposition();

        Map<Integer, double[]> objets =
                recupererObjetsDisponibles();

        String sql = "UPDATE objets_partie "
                + "SET ramasse_par = ? "
                + "WHERE partie_id = ? "
                + "AND objet_id = ? "
                + "AND position_x = ? "
                + "AND position_y = ? "
                + "AND ramasse_par IS NULL";

        for (Map.Entry<Integer, double[]> entree
                : objets.entrySet()) {

            double[] posObjet = entree.getValue();

            if (position[0] == posObjet[0]
                    && position[1] == posObjet[1]) {

                try (PreparedStatement requete =
                        connexion.prepareStatement(sql)) {

                    requete.setInt(1, idJoueur);
                    requete.setInt(2, idPartie);
                    requete.setInt(3, entree.getKey());
                    requete.setDouble(4, position[0]);
                    requete.setDouble(5, position[1]);

                    int resultat = requete.executeUpdate();

                    if (resultat == 1) {
                        System.out.println(
                            nom + " a recupere l'objet "
                            + entree.getKey()
                        );
                    }
                }
            }
        }
    }

    // Mettre a jour la vie du joueur
    public void verifierEtMettreAJourVie(int nouvelleVie) {

        vie = Math.max(0, nouvelleVie);

        String sql = "UPDATE joueurs "
                + "SET vie = ? "
                + "WHERE id = ? AND partie_id = ?";

        try (PreparedStatement requete =
                connexion.prepareStatement(sql)) {

            requete.setInt(1, vie);
            requete.setInt(2, idJoueur);
            requete.setInt(3, idPartie);

            requete.executeUpdate();

        } catch (SQLException e) {
            System.out.println(
                "Erreur de mise a jour de la vie : "
                + e.getMessage()
            );
        }
    }

    // Getters
    public double[] getPosition() {
        return maSouris.getposition().clone();
    }

    public String getNom() {
        return nom;
    }

    public int getIdJoueur() {
        return idJoueur;
    }

    public int getIdPartie() {
        return idPartie;
    }

    public int getVie() {
        return vie;
    }

    // Quitter la partie
    public void quitterPartie() {

        String sql = "DELETE FROM joueurs "
                + "WHERE id = ? AND partie_id = ?";

        try (PreparedStatement requete =
                connexion.prepareStatement(sql)) {

            requete.setInt(1, idJoueur);
            requete.setInt(2, idPartie);

            requete.executeUpdate();

        } catch (SQLException e) {
            System.out.println(
                "Erreur pour quitter la partie : "
                + e.getMessage()
            );

        } finally {
            fermerConnexion();
        }
    }

    // Fermer la connexion
    public void fermerConnexion() {
        serveur.fermerConnexion();
    }
}
