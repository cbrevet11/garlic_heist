/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package garlic_heist;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author vdaviet
 */
public class Testsouris {

    public static void main(String[] args) {
        souris maSouris = new souris();
        Objets mesObjets = new Objets();

        // Lettres distinctes pour les 3 objets (index 0, 1, 2)
        char[] lettresObjets = {'A', 'B', 'C'};

        // Inventaire géré uniquement dans le test
        List<String> inventaireTest = new ArrayList<>();

        Scanner scanner = new Scanner(System.in);
        boolean enJeu = true;

        System.out.println("=== TEST DE LA MAP : SOURIS & OBJETS ===");
        System.out.println("Objets : A (0), B (1), C (2) | Souris : M");
        System.out.println("Commandes : z (haut), s (bas), q (gauche), d (droite), exit (quitter)");

        while (enJeu) {
            // --- AFFICHAGE DE LA MAP (Grille de 10x10) ---
            int tailleGrille = 10;
            System.out.println("\n--------------------------------");
            for (int y = tailleGrille - 1; y >= 0; y--) {
                System.out.print("| ");
                for (int x = 0; x < tailleGrille; x++) {
                    char symbole = '.';

                    double[] posSouris = maSouris.getposition();
                    if ((int) posSouris[0] == x && (int) posSouris[1] == y) {
                        symbole = 'M';
                    }

                    double[][] toutesLesNourritures = mesObjets.getNourritures();
                    for (int i = 0; i < toutesLesNourritures.length; i++) {
                        double[] posNourriture = toutesLesNourritures[i];
                        if (posNourriture[0] != -1 && (int) posNourriture[0] == x && (int) posNourriture[1] == y) {
                            symbole = lettresObjets[i];
                        }
                    }

                    System.out.print(symbole + " ");
                }
                System.out.println("|");
            }
            System.out.println("--------------------------------");

            // --- AFFICHAGE DE L'INVENTAIRE : inventaire [A, B] ---
            System.out.println("inventaire [" + String.join(", ", inventaireTest) + "]");

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

            // --- DÉTECTION DE RÉCUPÉRATION ET PLACEMENT ALÉATOIRE ---
            double[][] nourrituresAvant = mesObjets.getNourritures();
            double[][] etatAvant = new double[nourrituresAvant.length][2];
            for (int i = 0; i < nourrituresAvant.length; i++) {
                etatAvant[i][0] = nourrituresAvant[i][0];
                etatAvant[i][1] = nourrituresAvant[i][1];
            }

            maSouris.recuperation_objet(mesObjets);

            double[][] nourrituresApres = mesObjets.getNourritures();
            boolean unObjetRamasse = false;
            for (int i = 0; i < nourrituresApres.length; i++) {
                if (etatAvant[i][0] != -1 && nourrituresApres[i][0] == -1) {
                    inventaireTest.add(String.valueOf(lettresObjets[i]));
                    unObjetRamasse = true;
                }
            }

            if (unObjetRamasse) {
                mesObjets.nourriturePlacementAleatoire();
            }
        }

        scanner.close();
    }
}