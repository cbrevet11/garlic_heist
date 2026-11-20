
package garlic_heist;

import java.sql.SQLException;
import java.util.Map;
import java.util.Scanner;

public class BroullonTestSourisMultijoueur {

    private static final int TAILLE = 10;

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        Joueur joueur = null;

        try {
            System.out.println("=== GARLIC HEIST MULTIJOUEUR ===");

            System.out.print("Nom du joueur : ");
            String nom = scanner.nextLine();

            System.out.println("1 - Creer une partie");
            System.out.println("2 - Rejoindre une partie");
            System.out.print("Choix : ");

            String choix = scanner.nextLine();
            int idPartie;

            if (choix.equals("1")) {
                idPartie = Joueur.creerPartie();
                System.out.println("Partie creee !");
                System.out.println("Code : " + idPartie);

            } else if (choix.equals("2")) {
                System.out.print("Code de la partie : ");
                idPartie = Integer.parseInt(scanner.nextLine());

            } else {
                System.out.println("Choix invalide.");
                return;
            }

            joueur = new Joueur(nom, idPartie);

            // Initialiser les objets de la partie
            joueur.initialiserObjets();

            // Verifier un objet sur la case de depart
            joueur.ramasserObjets();

            boolean jouer = true;

            while (jouer) {

                afficherGrille(joueur);

                System.out.println("\n=== COMMANDES ===");
                System.out.println("Z : Haut");
                System.out.println("S : Bas");
                System.out.println("Q : Gauche");
                System.out.println("D : Droite");
                System.out.println("R : Actualiser");
                System.out.println("X : Quitter");

                System.out.print("Commande : ");

                String commande = scanner.nextLine()
                        .trim().toUpperCase();

                double[] position = joueur.getPosition();

                int x = (int) position[0];
                int y = (int) position[1];

                boolean deplacement = false;

                switch (commande) {

                    case "Z":
                        if (y < TAILLE - 1) {
                            joueur.deplacerHaut();
                            deplacement = true;
                        }
                        break;

                    case "S":
                        if (y > 0) {
                            joueur.deplacerBas();
                            deplacement = true;
                        }
                        break;

                    case "Q":
                        if (x > 0) {
                            joueur.deplacerGauche();
                            deplacement = true;
                        }
                        break;

                    case "D":
                        if (x < TAILLE - 1) {
                            joueur.deplacerDroite();
                            deplacement = true;
                        }
                        break;

                    case "R":
                        System.out.println("Grille actualisee.");
                        break;

                    case "X":
                        jouer = false;
                        break;

                    default:
                        System.out.println("Commande inconnue.");
                }

                // Verifier le ramassage apres deplacement
                if (deplacement) {
                    joueur.ramasserObjets();
                }
            }

        } catch (SQLException e) {
            System.out.println("Erreur SQL : " + e.getMessage());

        } catch (NumberFormatException e) {
            System.out.println("Code de partie invalide.");

        } finally {
            if (joueur != null) {
                joueur.quitterPartie();
            }
        }
    }

    private static void afficherGrille(Joueur joueur)
            throws SQLException {

        char[][] grille = new char[TAILLE][TAILLE];

        // Initialiser la grille
        for (int y = 0; y < TAILLE; y++) {
            for (int x = 0; x < TAILLE; x++) {
                grille[y][x] = '.';
            }
        }

        // Afficher les objets non ramasses
        Map<Integer, double[]> objets =
                joueur.recupererObjetsDisponibles();

        for (double[] position : objets.values()) {

            int x = (int) position[0];
            int y = (int) position[1];

            if (dansGrille(x, y)) {
                grille[y][x] = 'O';
            }
        }

        // Afficher les autres joueurs
        Map<Integer, double[]> autres =
                joueur.recupererPositions();

        for (double[] position : autres.values()) {

            int x = (int) position[0];
            int y = (int) position[1];

            if (dansGrille(x, y)) {
                grille[y][x] = 'A';
            }
        }

        // Afficher la souris locale
        double[] position = joueur.getPosition();

        int x = (int) position[0];
        int y = (int) position[1];

        if (dansGrille(x, y)) {
            grille[y][x] =
                    grille[y][x] == 'A' ? 'X' : 'M';
        }

        System.out.println("\n=== PARTIE "
                + joueur.getIdPartie() + " ===");

        for (int ligne = TAILLE - 1; ligne >= 0; ligne--) {

            System.out.print("| ");

            for (int colonne = 0; colonne < TAILLE; colonne++) {
                System.out.print(grille[ligne][colonne] + " ");
            }

            System.out.println("|");
        }

        System.out.println("\nM = Ma souris");
        System.out.println("A = Autre souris");
        System.out.println("O = Objet disponible");
        System.out.println("X = Deux souris sur la meme case");

        System.out.println("Ma position : [" + x + ", " + y + "]");
    }

    private static boolean dansGrille(int x, int y) {
        return x >= 0 && x < TAILLE
                && y >= 0 && y < TAILLE;
    }
}
