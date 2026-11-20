
package garlic_heist;

import java.sql.SQLException;

public class TestMultijoueur {

    public static void main(String[] args) {

        try {
            // Créer une nouvelle partie
            int idPartie = Joueur.creerPartie();

            System.out.println("Partie créée !");
            System.out.println("Code de la partie : " + idPartie);

            // Ajouter le premier joueur
            Joueur joueur1 = new Joueur("Joueur1", idPartie);

            // Tester les déplacements
            joueur1.deplacerDroite();
            joueur1.deplacerHaut();

            System.out.println("Position du joueur : "
                    + java.util.Arrays.toString(
                        joueur1.getPosition()));

            joueur1.fermerConnexion();

        } catch (SQLException e) {
            System.out.println("Erreur SQL : " + e.getMessage());
        }
    }
}
