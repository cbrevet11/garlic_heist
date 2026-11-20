
package garlic_heist;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BroullonTestSourisMultijoueur {

    private static final int TAILLE = 10;

    // Actualisation toutes les 500 millisecondes
    private static final int INTERVALLE = 500;

    private static volatile boolean jouer = true;
    private static String derniereGrille = "";

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        Joueur joueur = null;

        ScheduledExecutorService actualisation =
                Executors.newSingleThreadScheduledExecutor();

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
                System.out.println("CODE : " + idPartie);

            } else if (choix.equals("2")) {

                System.out.print("Code de la partie : ");
                idPartie = Integer.parseInt(scanner.nextLine());

            } else {
                System.out.println("Choix invalide.");
                return;
            }

            joueur = new Joueur(nom, idPartie);

            // Initialiser les objets de la partie
            synchronized (joueur) {
                joueur.initialiserObjets();
                joueur.ramasserObjets();
            }

            final Joueur joueurActuel = joueur;

            jouer = true;
            derniereGrille = "";

            // Thread de synchronisation automatique
            actualisation.scheduleWithFixedDelay(() -> {

                if (!jouer) {
                    return;
                }

                try {
                    synchronized (joueurActuel) {
                        afficherGrille(joueurActuel);
                    }

                } catch (SQLException e) {
                    System.out.println(
                        "Erreur de synchronisation : "
                        + e.getMessage()
                    );
                }

            }, 0, INTERVALLE, TimeUnit.MILLISECONDS);

            // Thread principal : commandes du joueur
            while (jouer) {

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

                synchronized (joueurActuel) {

                    double[] position = joueurActuel.getPosition();

                    int x = (int) position[0];
                    int y = (int) position[1];

                    boolean deplacement = false;

                    switch (commande) {

                        case "Z":
                            if (y < TAILLE - 1) {
                                joueurActuel.deplacerHaut();
                                deplacement = true;
                            }
                            break;

                        case "S":
                            if (y > 0) {
                                joueurActuel.deplacerBas();
                                deplacement = true;
                            }
                            break;

                        case "Q":
                            if (x > 0) {
                                joueurActuel.deplacerGauche();
                                deplacement = true;
                            }
                            break;

                        case "D":
                            if (x < TAILLE - 1) {
                                joueurActuel.deplacerDroite();
                                deplacement = true;
                            }
                            break;

                        case "R":
                            // Force l'affichage au prochain cycle
                            derniereGrille = "";
                            break;

                        case "X":
                            jouer = false;
                            break;

                        default:
                            System.out.println("Commande inconnue.");
                    }

                    // Ramasser les objets apres un deplacement
                    if (deplacement) {
                        joueurActuel.ramasserObjets();
                    }
                }
            }

        } catch (SQLException e) {

            System.out.println("Erreur SQL : " + e.getMessage());

        } catch (NumberFormatException e) {

            System.out.println("Code de partie invalide.");

        } finally {

            jouer = false;

            // Arreter le thread d'actualisation
            actualisation.shutdown();

            try {
                if (!actualisation.awaitTermination(
                        5, TimeUnit.SECONDS)) {
                    actualisation.shutdownNow();
                }

            } catch (InterruptedException e) {
                actualisation.shutdownNow();
                Thread.currentThread().interrupt();
            }

            // Quitter la partie apres l'arret du thread
            if (joueur != null) {
                joueur.quitterPartie();
            }

            scanner.close();

            System.out.println("Deconnexion.");
        }
    }

    // Construire et afficher la grille
    private static void afficherGrille(Joueur joueur)
            throws SQLException {

        char[][] grille = new char[TAILLE][TAILLE];

        // Initialiser la grille
        for (int y = 0; y < TAILLE; y++) {
            Arrays.fill(grille[y], '.');
        }

        // Recuperer les objets disponibles
        Map<Integer, double[]> objets =
                joueur.recupererObjetsDisponibles();

        for (double[] position : objets.values()) {

            int x = (int) position[0];
            int y = (int) position[1];

            if (dansGrille(x, y)) {
                grille[y][x] = 'O';
            }
        }

        // Recuperer les souris des autres joueurs
        Map<Integer, double[]> autres =
                joueur.recupererPositions();

        for (double[] position : autres.values()) {

            int x = (int) position[0];
            int y = (int) position[1];

            if (dansGrille(x, y)) {
                grille[y][x] = 'A';
            }
        }

        // Recuperer sa propre souris
        double[] position = joueur.getPosition();

        int x = (int) position[0];
        int y = (int) position[1];

        if (dansGrille(x, y)) {
            grille[y][x] =
                    grille[y][x] == 'A' ? 'X' : 'M';
        }

        StringBuilder affichage = new StringBuilder();

        affichage.append("\n=== PARTIE ")
                .append(joueur.getIdPartie())
                .append(" ===\n");

        // Construire la grille de haut en bas
        for (int ligne = TAILLE - 1; ligne >= 0; ligne--) {

            affichage.append("| ");

            for (int colonne = 0; colonne < TAILLE; colonne++) {

                affichage.append(grille[ligne][colonne])
                        .append(" ");
            }

            affichage.append("|\n");
        }

        affichage.append("\nM = Ma souris");
        affichage.append("\nA = Autre souris");
        affichage.append("\nO = Objet disponible");
        affichage.append("\nX = Deux souris sur la meme case");

        affichage.append("\nMa position : [")
                .append(x)
                .append(", ")
                .append(y)
                .append("]\n");

        String nouvelleGrille = affichage.toString();

        // Afficher seulement si quelque chose change
        if (!nouvelleGrille.equals(derniereGrille)) {

            System.out.println(nouvelleGrille);

            derniereGrille = nouvelleGrille;

            System.out.print("Commande : ");
        }
    }

    private static boolean dansGrille(int x, int y) {

        return x >= 0 && x < TAILLE
                && y >= 0 && y < TAILLE;
    }
}
