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
        "------------------------",
        "|..........|...........|",
        "|..........|...........|",
        "|..........|...........|",
        "|..........|...........|",
        "|..........|...........|",
        "------------------------",
        "|..........|...........|",
        "|..........|...........|",
        "|..........|...........|",
        "|..........|...........|",
        "|..........|...........|",
        "------------------------"
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

        // ----- Tuiles simples : lettre, colonne, ligne, bloque le passage ? -----
        definir('.', 13, 3, false);   // sol
        definir('-', 1, 0, true);     // mur horizontale
        definir('|', 7, 3, true);     // mur verticale
        definir('P', 5, 2, true);     // pilier
        definir('D', 6, 3, false);    // tapis, décor

        // ----- Objets multi-tuiles : nom, colonne, ligne, largeur, hauteur, bloquant -----
        definirObjet("table", 10, 2, 2, 2, true);   // EXEMPLE : à ajuster avec OutilTileset

        // ----- Pose des objets : nom, x, y (en cases) -----
        poser("table", 4, 3);
        poser("table", 16, 3);
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
}