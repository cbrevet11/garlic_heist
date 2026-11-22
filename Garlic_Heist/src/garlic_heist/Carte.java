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

    // Tous les niveaux : une lettre = un type de tuile
    //   .  sol sombre        |  mur            ~  eau
    //   r  sol rouge         b  sol beige       g  grille       p  pics
    private static final String[][] NIVEAUX = {
        { // ----- Niveau 0 -----
            "[[[[[[[[[[[[[[[[[[[[[[[[[[[[[v",
            "<tt....c.|..rrcrr..|......tt>v",
            "<........|..rrrrr..|.c......>v",
            "<.....aa....rrrrr...........>v",
            "<.....................s.....>v",
            "<t.......|..g...g..|........>v",
            "<........|.........|........>v",
            "<]]].]]]]|.........|]]]].]]]>v",
            "<[[[.[[[[|.........|[[[[.[[[>v",
            "<........|.........|........>v",
            "<.aa.....|...~~~...|.aa.....>v",
            "<......c.....~~~...........t>v",
            "<............~~~............>v",
            "<........|...~~~...|.ppppppp>v",
            "<t.....s.|...~~~...|........>v",
            "<........|...~~~...|.......c>v",
            "]]]]]]]]]]]]]]]]]]]]]]]]]]]]]v"
        },
        { // ----- Niveau 1 -----
            "[[[[[[[[[[[[[[[",
            "<ccrrr|..~~~..>",
            "<srrrr|..~~~..>",
            "<rrrrr...~~~..>",
            "<rrrrr|.......>",
            "<]]]]]]]]].]]]>",
            "<[[[[[[[[[.[[[>",
            "<t............>",
            "<t.aa.........>",
            "<....ppp.....t>",
            "]]]]]]]]]]]]]]]"
        }
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

    private int numeroNiveau;
    private char[][] cases;
    private int nbLignes;
    private int nbColonnes;

    private BufferedImage tileset;
    private Map<Character, BufferedImage> images = new HashMap<>();
    private Set<Character> bloquants = new HashSet<>();

    private Map<String, Modele> modeles = new HashMap<>();
    private ArrayList<ObjetPose> objets = new ArrayList<>();
    private boolean[][] occupe;   // cases bloquées par un objet

    // Ancien appel : new Carte()  ->  charge le niveau 0
    public Carte() {
        this(0);
    }

    public Carte(int numeroNiveau) {
        this.numeroNiveau = numeroNiveau;
        String[] plan = NIVEAUX[numeroNiveau];

        nbLignes = plan.length;
        nbColonnes = plan[0].length();
        cases = new char[nbLignes][nbColonnes];
        occupe = new boolean[nbLignes][nbColonnes];
        for (int y = 0; y < nbLignes; y++) {
            if (plan[y].length() != nbColonnes) {
                throw new IllegalArgumentException("Niveau " + numeroNiveau
                    + ", ligne " + y + " : longueur " + plan[y].length()
                    + " au lieu de " + nbColonnes);
            }
            cases[y] = plan[y].toCharArray();
        }

        try {
            tileset = ImageIO.read(
                getClass().getClassLoader().getResource("resources/tileset.png"));
        } catch (Exception e) {
            System.out.println("Impossible de charger le tileset : " + e);
        }

        // ----- Tuiles simples -----
        // Coordonnées : colonne, ligne dans le tileset (8 colonnes x 4 lignes)

        // SOL, MURS ET EAU
        definir('.', 7, 1, false);   // sol château
        definir('|', 6, 0, true);    // mur 
        definir('~', 5, 0, false);   // eau

        // AUTRES SOLS ET PIÈGES
        definir('<', 2, 1, false);   // mur exté gauche
        definir('>', 1, 1, false);   // mur exté droit
        definir('[', 7, 0, false);   // mur exté haut
        definir(']', 0, 1, false);   // mur exté bas
        definir('>', 5, 1, false);   // mur exté droit
        definir('p', 5, 1, false);   // pics
        definir('r', 1, 2, false);   // sol rouge
        definir('b', 0, 2, false);   // sol beige
        definir('v', 2, 2, false);   // sol exté
        definir('g', 4, 1, false);   // grille
        definir('p', 5, 1, false);   // pics
        definir('t', 0, 3, false);   // tonneau
        definir('b', 0, 0, false);   // Boite
        definir('s', 3, 0, false);   // scquelette
        definir('c', 2, 0, false);   // coffre
        definir('a', 6, 2, false);   // table, assiettes
    }

    // Découpe une tuile du tileset et l'associe à une lettre
    private void definir(char lettre, int colonne, int ligne, boolean bloquant) {
        images.put(lettre, tileset.getSubimage(
            colonne * TAILLE, ligne * TAILLE, TAILLE, TAILLE));
        if (bloquant) {
            bloquants.add(lettre);
        }
    }

    
    // Partie de Adrien
    
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

    public int getNumeroNiveau() {
        return numeroNiveau;
    }

    // Nombre de niveaux disponibles
    public static int getNbNiveaux() {
        return NIVEAUX.length;
    }
}