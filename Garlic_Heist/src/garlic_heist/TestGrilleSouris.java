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
public class TestGrilleSouris {
    public static void main(String[] args) {
        String ipServeur = "127.0.0.1";
        int port = 12345;

        System.out.println("--- INTERFACE GRILLE DE LA SOURIS EN LIGNE ---");

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Entrez le nom de votre joueur : ");
            String nomJoueur = scanner.nextLine();

            // Connexion au serveur via notre classe réseau
            Joueur joueur = new Joueur(ipServeur, port, nomJoueur);

            // Définition de la taille de la grille (10x10)
            int largeur = 10;
            int hauteur = 10;
            
            // Position initiale de la souris au centre de la grille
            int posX = 5;
            int posY = 5;

            boolean enCours = true;
            while (enCours) {
                // --- AFFICHAGE DE LA GRILLE DANS LA CONSOLE ---
                System.out.println("\n--------------------------------");
                for (int y = 0; y < hauteur; y++) {
                    System.out.print("| ");
                    for (int x = 0; x < largeur; x++) {
                        if (x == posX && y == posY) {
                            System.out.print("M "); // 'M' représente la Souris
                        } else {
                            System.out.print(". "); // '.' représente une case vide
                        }
                    }
                    System.out.println("|");
                }
                System.out.println("--------------------------------");
                System.out.println("Coordonnées -> X: " + posX + " | Y: " + posY);
                System.out.println("Commandes : [Z] Haut | [S] Bas | [Q] Gauche | [D] Droite | [X] Quitter");
                System.out.print("Votre choix : ");

                String choix = scanner.nextLine().trim().toUpperCase();

                switch (choix) {
                    case "Z": // Haut
                        if (posY > 0) {
                            posY--;
                            joueur.deplacerHaut(); // Appel de la méthode d'origine
                        } else {
                            System.out.println("-> Oups ! La souris atteint le bord supérieur.");
                        }
                        break;
                    case "S": // Bas
                        if (posY < hauteur - 1) {
                            posY++;
                            joueur.deplacerBas(); // Appel de la méthode d'origine[cite: 1]
                        } else {
                            System.out.println("-> Oups ! La souris atteint le bord inférieur.");
                        }
                        break;
                    case "Q": // Gauche
                        if (posX > 0) {
                            posX--;
                            joueur.deplacerGauche(); // Appel de la méthode d'origine[cite: 1]
                        } else {
                            System.out.println("-> Oups ! La souris atteint le bord gauche.");
                        }
                        break;
                    case "D": // Droite
                        if (posX < largeur - 1) {
                            posX++;
                            joueur.deplacerDroite(); // Appel de la méthode d'origine[cite: 1]
                        } else {
                            System.out.println("-> Oups ! La souris atteint le bord droit.");
                        }
                        break;
                    case "X":
                        enCours = false;
                        joueur.fermerConnexion();
                        System.out.println("Fermeture du test. Au revoir !");
                        break;
                    default:
                        System.out.println("Touche invalide. Utilisez Z, S, Q, D ou X.");
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur durant le test : " + e.getMessage());
        }
    }
}
