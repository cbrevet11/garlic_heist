/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package garlic_heist;
import java.util.Scanner;
/**
 *
 * @author tbrenand
 */
public class TestInteractifSouris {
    public static void main(String[] args) {
        String ipServeur = "127.0.0.1"; // Votre serveur local
        int port = 12345;

        System.out.println("--- TEST INTERACTIF DE LA SOURIS EN LIGNE ---");
        
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Entrez le nom de votre joueur : ");
            String nomJoueur = scanner.nextLine();

            // Connexion au serveur via notre classe JoueurReseau
            Joueur joueur = new Joueur(ipServeur, port, nomJoueur);
            System.out.println("\nConnecté avec succès ! Utilisez les touches du menu pour contrôler votre souris.");

            boolean enCours = true;
            while (enCours) {
                System.out.println("\n============================");
                System.out.println("      MENU DE CONTRÔLE      ");
                System.out.println("============================");
                System.out.println("[Z] : Déplacer vers le HAUT");
                System.out.println("[S] : Déplacer vers le BAS");
                System.out.println("[Q] : Déplacer vers la GAUCHE");
                System.out.println("[D] : Déplacer vers la DROITE");
                System.out.println("[V] : Tester / Modifier les points de vie");
                System.out.println("[I] : Afficher les infos (Nom, Position, Nourriture)");
                System.out.println("[X] : Quitter et fermer la connexion");
                System.out.print("Votre choix : ");

                String choix = scanner.nextLine().trim().toUpperCase();

                switch (choix) {
                    case "Z":
                        joueur.deplacerHaut();
                        System.out.println("-> Action envoyée : HAUT");
                        break;
                    case "S":
                        joueur.deplacerBas();
                        System.out.println("-> Action envoyée : BAS");
                        break;
                    case "Q":
                        joueur.deplacerGauche();
                        System.out.println("-> Action envoyée : GAUCHE");
                        break;
                    case "D":
                        joueur.deplacerDroite();
                        System.out.println("-> Action envoyée : DROITE");
                        break;
                    case "V":
                        System.out.print("Entrez la valeur des points de vie (<= 0 pour tuer la souris) : ");
                        try {
                            int pv = Integer.parseInt(scanner.nextLine());
                            joueur.verifierEtMettreAJourVie(pv);
                        } catch (NumberFormatException e) {
                            System.out.println("Erreur : Veuillez entrer un nombre valide.");
                        }
                        break;
                    case "I":
                        System.out.println("\n--- INFOS ACTUELLES DE LA SOURIS ---");
                        System.out.println("Nom : " + joueur.getNom());
                        double[] pos = joueur.getPosition();
                        System.out.println("Position (taille du tableau) : " + (pos != null ? pos.length : "null"));
                        double[] nour = joueur.getNourriture();
                        System.out.println("Nourriture (taille du tableau) : " + (nour != null ? nour.length : "null"));
                        break;
                    case "X":
                        enCours = false;
                        joueur.fermerConnexion();
                        System.out.println("Fermeture de la connexion réseau. Au revoir !");
                        break;
                    default:
                        System.out.println("Commande inconnue. Tapez Z, S, Q, D, V, I ou X.");
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur durant le test interactif : " + e.getMessage());
        }
    }
}
