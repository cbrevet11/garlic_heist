/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package garlic_heist;

/**
 *
 * @author tbrenand
 */
public class TestJoueur {
    public static void main(String[] args) {
        System.out.println("Début du test de la classe JoueurReseau...");

        // Adresse et port de test (assurez-vous d'avoir un serveur en écoute si vous testez la vraie connexion)
        String ipServeur = "127.0.0.1";
        int port = 12345;

        try {
            // Note: Si aucun serveur n'est lancé sur le port 12345, 
            // cette ligne lancera une IOException (Connexion refusée), ce qui est normal.
            Joueur joueur = new Joueur(ipServeur, port, "SuperSouris");

            System.out.println("Test des déplacements :");
            joueur.deplacerHaut();
            joueur.deplacerDroite();
            joueur.deplacerBas();
            joueur.deplacerGauche();

            System.out.println("Test de l'état de vie (PV à 0 pour tester la mort) :");
            //joueur.verifierEtatVie(0); // Doit déclencher la méthode de mort de la souris d'origine

            // Fermeture propre
            joueur.fermerConnexion();
            System.out.println("Test terminé avec succès.");

        } catch (Exception e) {
            System.out.println("Erreur durant le test réseau (prévue si le serveur n'est pas démarré) : " + e.getMessage());
        }
    }
}
    

