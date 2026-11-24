package garlic_heist;

/**
 *
 * @author vdavietttt
 */
public class Findejeu {

    // Méthode pour vérifier et arrêter le jeu si plus aucun joueur n'est en vie, sans aucun booléen
    public static void verifierEtArreterJeu(int[] viesJoueurs) {
        int joueursVivants = 0;                                                // Compteur des joueurs encore en vie
        
        for (int i = 0; i < viesJoueurs.length; i++) {                         // Parcourt le tableau des vies de chaque joueur
            if (viesJoueurs[i] > 0) {                                          // Si le joueur a encore de la vie
                joueursVivants = joueursVivants + 1;                           // On incrémente le nombre de survivants
            }
        }
        
        if (joueursVivants == 0) {                                             // Si le compteur de survivants tombe à zéro
            System.out.println("=== FIN DE LA PARTIE : Game Over ===");        // Affiche le message de fin dans la console
            System.exit(0);                                                    // Stoppe complètement l'application Java
        }
    }
}  /////111111