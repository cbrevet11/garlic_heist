/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package garlic_heist;

/**
 *
 * @author tbrenand
 */
public class TestCompletSouris {
    public static void main(String[] args) {
        String ipServeur = "127.0.0.1";
        int port = 12345;

        try {
            System.out.println("--- DÉBUT DU TEST GLOBAL DE LA SOURIS ---");
            Joueur joueur = new Joueur(ipServeur, port, "TesteurGlobal");

            // 1. Tester les déplacements
            joueur.deplacerHaut();
            joueur.deplacerDroite();

            // 2. Vérifier l'accès aux variables internes
            double[] pos = joueur.getPosition();
            double[] nour = joueur.getNourriture();
            System.out.println("Nom de la souris récupéré : " + joueur.getNom());
            System.out.println("Taille du tableau position : " + (pos != null ? pos.length : "null"));
            System.out.println("Taille du tableau nourriture : " + (nour != null ? nour.length : "null"));

            // 3. Tester la vie et la mort de la souris
            System.out.println("\nSimulation de dégâts (PV = 0)...");
            joueur.verifierEtMettreAJourVie(0);

            // Fermeture
            joueur.fermerConnexion();
            System.out.println("--- FIN DU TEST GLOBAL ---");

        } catch (Exception e) {
            System.out.println("Erreur (vérifiez que le serveur est lancé) : " + e.getMessage());
        }
    }
}