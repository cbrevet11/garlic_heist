/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package garlic_heist;

/**
 *
 * @author tbrenand
 */


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Serveur {

    private static final String URL =
         "jdbc:mariadb://nemrod.ens2m.fr:3306/2026-2027_s1_vs2_tp2_garlic-heist";

    private static final String UTILISATEUR = "etudiant";
    private static final String MOT_DE_PASSE = "YTDTvj9TR3CDYCmP";

    private Connection connexion;

    // Constructeur du serveur
    public Serveur() {
        try {
            connexion = DriverManager.getConnection(
                URL, UTILISATEUR, MOT_DE_PASSE
            );

            System.out.println("Connexion au serveur ENSMM réussie !");

        } catch (SQLException e) {
            System.out.println("Erreur de connexion : " + e.getMessage());
        }
    }

    // Récupérer la connexion à la base de données
    public Connection getConnexion() {
        return connexion;
    }

    // Fermer la connexion
    public void fermerConnexion() {
        try {
            if (connexion != null && !connexion.isClosed()) {
                connexion.close();
                System.out.println("Connexion fermée.");
            }
        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    
    public static void main(String[] args) {

        Serveur serveur = new Serveur();
        Connection connexion = serveur.getConnexion();

        if (connexion != null) {
            try {
                System.out.println("Connexion réussie !");
                System.out.println("Base : " + connexion.getCatalog());

                // Afficher les tables de la base de données
                java.sql.DatabaseMetaData meta = connexion.getMetaData();

                try (java.sql.ResultSet tables = meta.getTables(
                        connexion.getCatalog(),
                        null,
                        "%",
                        new String[]{"TABLE"})) {

                    while (tables.next()) {
                        System.out.println("Table : "
                                + tables.getString("TABLE_NAME"));
                    }
                }

            } catch (SQLException e) {
                System.out.println("Erreur SQL : " + e.getMessage());
            }
        }

        serveur.fermerConnexion();
    }
}


