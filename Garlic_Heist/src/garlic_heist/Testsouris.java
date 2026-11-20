/*
- Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
- Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package garlic_heist;

import java.util.Scanner;

/**
 *
 * @author vdaviet
 */
public class Testsouris {

    public static void main(String[] args) {
        // 1. Initialisation de la souris et des objets
        souris maSouris = new souris();
        Objets mesObjets = new Objets();
        
        Scanner scanner = new Scanner(System.in);
        boolean enJeu = true;

        System.out.println("=== TEST DE LA MAP : SOURIS & OBJETS ===");
        System.out.println("Commandes : z (haut), s (bas), q (gauche), d (droite), exit (quitter)");

        // 2. Boucle principale du jeu
        while (enJeu) {
            // --- AFFICHAGE DE LA MAP (Grille de 10x10) ---
            int tailleGrille = 10;
            System.out.println("\n--------------------------------");
            for (int y = tailleGrille - 1; y >= 0; y--) { // On commence par le haut (Y=9) vers le bas (Y=0)
                System.out.print("| ");
                for (int x = 0; x < tailleGrille; x++) {
                    char symbole = '.'; // Case vide par défaut

                    // Vérifier si la souris est sur cette case
                    double[] posSouris = maSouris.getposition();
                    if ((int) posSouris[0] == x && (int) posSouris[1] == y) {
                        symbole = 'M'; // M pour Souris (Mouse)
                    }

                    // Vérifier si un objet/nourriture est sur cette case
                    double[][] toutesLesNourritures = mesObjets.getNourritures();
                    for (int i = 0; i < toutesLesNourritures.length; i++) {
                        double[] posNourriture = toutesLesNourritures[i];
                        // Si l'objet n'a pas été mangé (on évite -1) et qu'il est sur la case
                        if (posNourriture[0] != -1 && (int) posNourriture[0] == x && (int) posNourriture[1] == y) {
                            symbole = 'O'; // O pour Objet / Nourriture
                        }
                    }

                    System.out.print(symbole + " ");
                }
                System.out.println("|");
            }
            System.out.println("--------------------------------");

            // --- SAISIE DU MOUVEMENT ---
            System.out.print("Action (z/s/q/d) : ");
            String action = scanner.nextLine();

            if (action.equalsIgnoreCase("z")) {
                maSouris.deplacementhaut();
            } else if (action.equalsIgnoreCase("s")) {
                maSouris.deplacementbas();
            } else if (action.equalsIgnoreCase("q")) {
                maSouris.deplacementgauche();
            } else if (action.equalsIgnoreCase("d")) {
                maSouris.deplacementdroite();
            } else if (action.equalsIgnoreCase("exit")) {
                enJeu = false;
                System.out.println("Fin du test.");
                break;
            } else {
                System.out.println("Touche non reconnue ! Utilisez z, s, q ou d.");
                continue;
            }

            // --- VÉRIFICATION DE LA RÉCUPÉRATION D'OBJET ---
            maSouris.recuperation_objet(mesObjets);
        }

        scanner.close();
    }
}