package garlic_heist;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import javax.imageio.ImageIO;

public class Carte {

    public static final int TAILLE = 42;

    // Le plan : une lettre = un type de tuile

    private static final String[] PLAN = {
        "#~~~~~~~###########~~~~~~~###",
        "#.......###########.......###",
        "#############################",
        "#....####...........####....#",
        "#....#..#...........#..#....#",
        "#....#..#...........#..#....#",
        "######..#############..######",
        "#....#...................#..#",
        "#....#...................#..#",
        "######....#########....######",
        "..........#.......#..........",
        "..........#..~~~..#..........",
        "###########..~~~..###########",
        "#.........#..~~~..#.........#",
        "#.........#..~~~..#.........#",
        "#.........#..~~~..#.........#",
        "###########..~~~..###########",
        "#############################",
        "#############################"
    };


    // Un modèle d'objet : un rectangle découpé dans le tileset
    private static class Modele {
        BufferedImage image;
        int largeur, hauteur;   // en tuiles
        boolean bloquant;
    }

    // Un objet posé sur la carte
    private static class ObjetPose {
        Modele modele;
        int x, y;               // position en tuiles
    }

    private char[][] cases;
    private int nbLignes;
    private int nbColonnes;

    private BufferedImage tileset;
    private Map<Character, BufferedImage> images = new HashMap<>();
    private Set<Character> bloquants = new HashSet<>();

    private Map<String, Modele> modeles = new HashMap<>();
    private ArrayList<ObjetPose> objets = new ArrayList<>();
    private boolean[][] occupe;   // cases bloquées par un objet

    public Carte() {
        nbLignes = PLAN.length;
        nbColonnes = PLAN[0].length();
        cases = new char[nbLignes][nbColonnes];
        occupe = new boolean[nbLignes][nbColonnes];
        for (int y = 0; y < nbLignes; y++) {
            cases[y] = PLAN[y].toCharArray();
        }

        try {
            tileset = ImageIO.read(
                getClass().getClassLoader().getResource("resources/tileset_42.png"));
        } catch (Exception e) {
            System.out.println("Impossible de charger le tileset : " + e);
        }

        // ----- Tuiles simples -----
    // Coordonnées : colonne, ligne dans le tileset

    // SOL, MURS ET EAU
    definir('.', 1, 3, false);
    definir('#', 7, 5, true);
    definir('~', 0, 5, false);

    // OBJETS DECORATIFS
    definirObjet("coffre", 4, 3, 1, 1, true);
    definirObjet("tonneau", 7, 3, 1, 1, true);
    definirObjet("or", 2, 7, 1, 1, false);
    definirObjet("cle", 6, 8, 1, 1, false);
    definirObjet("crane", 0, 7, 1, 1, false);
    definirObjet("grille", 4, 7, 1, 1, true);
    definirObjet("torche", 7, 8, 1, 1, false);
    definirObjet("banniere", 6, 5, 1, 1, false);
    definirObjet("colonne", 2, 2, 1, 1, true);
    definirObjet("levier", 3, 6, 1, 1, false);

    // SALLE SUPERIEURE GAUCHE
    poser("crane", 1, 7);
    poser("colonne", 2, 4);
    poser("colonne", 4, 4);
    poser("torche", 3, 5);

    // SALLE SUPERIEURE DROITE
    poser("coffre", 23, 7);
    poser("or", 23, 8);
    poser("colonne", 24, 4);
    poser("torche", 25, 5);

    // GRANDE PORTE CENTRALE
    poser("banniere", 11, 3);
    poser("banniere", 17, 3);
    poser("levier", 12, 8);

    // SALLE INFERIEURE GAUCHE
    poser("banniere", 2, 13);
    poser("colonne", 3, 13);
    poser("colonne", 6, 13);
    poser("coffre", 2, 14);
    poser("tonneau", 3, 15);
    poser("cle", 5, 14);
    poser("grille", 2, 16);
    poser("torche", 8, 14);

    // SALLE INFERIEURE DROITE
    poser("banniere", 23, 13);
    poser("colonne", 22, 13);
    poser("colonne", 25, 13);
    poser("tonneau", 25, 15);
    poser("tonneau", 26, 15);
    poser("grille", 20, 16);
    poser("grille", 25, 16);
    poser("or", 23, 14);

    // CENTRE DU DONJON
    poser("grille", 13, 10);
    poser("grille", 14, 10);
    poser("tonneau", 10, 10);
    poser("grille", 12, 16);

    
    }    

    // Découpe une tuile du tileset et l'associe à une lettre
    private void definir(char lettre, int colonne, int ligne, boolean bloquant) {
        images.put(lettre, tileset.getSubimage(
            colonne * TAILLE, ligne * TAILLE, TAILLE, TAILLE));
        if (bloquant) {
            bloquants.add(lettre);
        }
    }

    // colonne, ligne = coin haut-gauche dans le tileset ; largeur, hauteur en tuiles
    private void definirObjet(String nom, int colonne, int ligne,
                              int largeur, int hauteur, boolean bloquant) {
        Modele m = new Modele();
        m.image = tileset.getSubimage(colonne * TAILLE, ligne * TAILLE,
                                      largeur * TAILLE, hauteur * TAILLE);
        m.largeur = largeur;
        m.hauteur = hauteur;
        m.bloquant = bloquant;
        modeles.put(nom, m);
    }

    // Pose un objet sur la carte (x, y = case du coin haut-gauche)
    private void poser(String nom, int x, int y) {
        Modele m = modeles.get(nom);
        ObjetPose o = new ObjetPose();
        o.modele = m;
        o.x = x;
        o.y = y;
        objets.add(o);

        if (m.bloquant) {
            for (int j = y; j < y + m.hauteur; j++) {
                for (int i = x; i < x + m.largeur; i++) {
                    occupe[j][i] = true;
                }
            }
        }
    }

    public void miseAJour() {
    }

    public void rendu(Graphics2D contexte) {
        // 1) le décor de base (sol, murs...)
        for (int y = 0; y < nbLignes; y++) {
            for (int x = 0; x < nbColonnes; x++) {
                BufferedImage image = images.get(cases[y][x]);
                contexte.drawImage(image, x * TAILLE, y * TAILLE, null);
            }
        }
        // 2) les objets par-dessus
        for (ObjetPose o : objets) {
            contexte.drawImage(o.modele.image, o.x * TAILLE, o.y * TAILLE, null);
        }
    }

    public boolean isObstacle(int x, int y) {
        if (x < 0 || y < 0 || x >= nbColonnes || y >= nbLignes) {
            return true;
        }
        return bloquants.contains(cases[y][x]) || occupe[y][x];
    }

    public boolean caseAccessible(int x, int y) {
        return !isObstacle(x, y);
    }

    // Recherche un chemin entre deux positions
    public ArrayList<int[]> trouverChemin(int departX, int departY,
                                          int arriveeX, int arriveeY) {

        Queue<int[]> file = new LinkedList<>();
        boolean[][] visite = new boolean[nbLignes][nbColonnes];
        int[][][] precedent = new int[nbLignes][nbColonnes][2];

        if (!caseAccessible(departX, departY)
                || !caseAccessible(arriveeX, arriveeY)) {
            return new ArrayList<>();
        }

        file.add(new int[]{departX, departY});
        visite[departY][departX] = true;

        int[] dx = {0, 0, -1, 1};
        int[] dy = {-1, 1, 0, 0};

        boolean trouve = false;

        while (!file.isEmpty()) {
            int[] position = file.poll();
            int x = position[0];
            int y = position[1];

            if (x == arriveeX && y == arriveeY) {
                trouve = true;
                break;
            }

            for (int i = 0; i < 4; i++) {
                int nouveauX = x + dx[i];
                int nouveauY = y + dy[i];

                if (caseAccessible(nouveauX, nouveauY)
                        && !visite[nouveauY][nouveauX]) {

                    visite[nouveauY][nouveauX] = true;
                    precedent[nouveauY][nouveauX][0] = x;
                    precedent[nouveauY][nouveauX][1] = y;
                    file.add(new int[]{nouveauX, nouveauY});
                }
            }
        }

        ArrayList<int[]> chemin = new ArrayList<>();

        if (trouve) {
            int x = arriveeX;
            int y = arriveeY;

            while (x != departX || y != departY) {
                chemin.add(0, new int[]{x, y});
                int ancienX = precedent[y][x][0];
                int ancienY = precedent[y][x][1];
                x = ancienX;
                y = ancienY;
            }
        }

        return chemin;
    }

    // Renvoie le caractère présent sur une case
    public char getCase(int x, int y) {
        if (x < 0 || y < 0 || x >= nbColonnes || y >= nbLignes) {
            return '|';
        }
        return cases[y][x];
    }
    
    public int getNbLignes() {
        return nbLignes;
    }
    
    public int getNbColonnes() {
        return nbColonnes;
    }
}