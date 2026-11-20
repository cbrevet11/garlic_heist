
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

    // Connexion du joueur à une partie existante
    public Joueur(String nom, int idPartie) {

        this.nom = nom;
        this.idPartie = idPartie;
        this.maSouris = new souris();

        serveur = new Serveur();
        connexion = serveur.getConnexion();

        if (connexion == null) {
            throw new IllegalStateException(
                "Connexion à MariaDB impossible"
            );
        }

        try {
            ajouterJoueur();
            System.out.println(nom + " a rejoint la partie " + idPartie);

        } catch (SQLException e) {
            serveur.fermerConnexion();
            throw new IllegalStateException(
                "Impossible de rejoindre la partie", e
            );
        }
    }

    // Créer une nouvelle partie et récupérer son ID
    public static int creerPartie() throws SQLException {

        Serveur serveur = new Serveur();

        try {
            Connection connexion = serveur.getConnexion();

            if (connexion == null) {
                throw new SQLException("Connexion MariaDB impossible");
            }

            String sql = "INSERT INTO parties (statut) VALUES ('attente')";

            try (PreparedStatement requete =
                    connexion.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {

                requete.executeUpdate();

                try (ResultSet resultat = requete.getGeneratedKeys()) {

                    if (resultat.next()) {
                        return resultat.getInt(1);
                    }
                }
            }

            throw new SQLException("ID de partie introuvable");

        } finally {
            serveur.fermerConnexion();
        }
    }

    // Enregistrer le joueur dans la base de données
    private void ajouterJoueur() throws SQLException {

        String sql = "INSERT INTO joueurs "
                   + "(pseudo, partie_id, position_x, position_y, vie) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement requete =
                connexion.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS)) {

            requete.setString(1, nom);
            requete.setInt(2, idPartie);
            requete.setDouble(3, maSouris.getposition()[0]);
            requete.setDouble(4, maSouris.getposition()[1]);
            requete.setInt(5, vie);

            requete.executeUpdate();

            try (ResultSet resultat = requete.getGeneratedKeys()) {
                if (resultat.next()) {
                    idJoueur = resultat.getInt(1);
                } else {
                    throw new SQLException(
                        "Impossible de récupérer l'ID du joueur"
                    );
                }
            }
        }
    }

    // Enregistrer la position du joueur
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
                "Erreur de synchronisation : " + e.getMessage()
            );
        }
    }

    // Déplacements
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

    // Mettre à jour la vie du joueur
    public void verifierEtMettreAJourVie(int nouvelleVie) {

        vie = Math.max(0, nouvelleVie);

        if (vie == 0) {
            maSouris.mort_souris();
        }

        String sql = "UPDATE joueurs SET vie = ? "
                   + "WHERE id = ? AND partie_id = ?";

        try (PreparedStatement requete =
                connexion.prepareStatement(sql)) {

            requete.setInt(1, vie);
            requete.setInt(2, idJoueur);
            requete.setInt(3, idPartie);
            requete.executeUpdate();

        } catch (SQLException e) {
            System.out.println(
                "Erreur de mise à jour de la vie : " + e.getMessage()
            );
        }
    }

    // Récupérer les positions des autres joueurs
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

            try (ResultSet resultat = requete.executeQuery()) {

                while (resultat.next()) {

                    int id = resultat.getInt("id");
                    double x = resultat.getDouble("position_x");
                    double y = resultat.getDouble("position_y");

                    positions.put(id, new double[]{x, y});
                }
            }
        }

        return positions;
    }

    // Récupérer la position de sa propre souris
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
                "Erreur pour quitter la partie : " + e.getMessage()
            );
        } finally {
            fermerConnexion();
        }
    }

    // Fermer la connexion MariaDB
    public void fermerConnexion() {
        serveur.fermerConnexion();
    }
}
