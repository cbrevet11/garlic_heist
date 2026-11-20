
package garlic_heist;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

public class TestRejoindre {

    public static void main(String[] args) {

        Joueur joueur2 = new Joueur("Joueur2", 2);

        System.out.println("Joueur2 connecté à la partie 2 !");

        joueur2.deplacerGauche();

        try {
            Map<Integer, double[]> positions =
                    joueur2.recupererPositions();

            for (Map.Entry<Integer, double[]> entree
                    : positions.entrySet()) {

                System.out.println(
                    "Joueur " + entree.getKey()
                    + " : " + Arrays.toString(entree.getValue())
                );
            }

        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        } finally {
            joueur2.fermerConnexion();
        }
    }
}

