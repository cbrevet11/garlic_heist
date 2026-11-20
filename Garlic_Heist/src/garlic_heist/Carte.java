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

    public static final int TAILLE = 32;

    // Le plan : une lettre = un type de tuile
    private static final String[] PLAN = {
        "############",
        "#..........#",
        "#..TT......#",
        "#..........#",
        "#......PP..#",
        "#....D.....#",
        "############"
    };

    private char[][] cases;
    private int nbLignes;
    private int nbColonnes;

    private BufferedImage tileset;
    private Map<Character, BufferedImage> images = new HashMap<>();
    private Set<Character> bloquants = new HashSet<>();

    public Carte() {
        nbLignes = PLAN.length;
        nbColonnes = PLAN[0].length();
        cases = new char[nbLignes][nbColonnes];
        for (int y = 0; y < nbLignes; y++) {
            cases[y] = PLAN[y].toCharArray();
        }

        try {
            tileset = ImageIO.read(
                getClass().getClassLoader().getResource("resources/tileset_chateau.png"));
        } catch (Exception e) {
            System.out.println("Impossible de charger le tileset : " + e);
        }

        // lettre, colonne dans le tileset, ligne dans le tileset, bloque le passage ?
        definir('.', 8, 4, false);   // sol
        definir('#', 0, 0, true);    // mur
        definir('T', 3, 1, true);    // table
        definir('P', 5, 2, true);    // pilier
        definir('D', 6, 3, false);   // tapis, décor
        // Pour ajouter une tuile : une ligne ici, puis la lettre dans le PLAN
    }

    // Découpe une tuile du tileset et l'associe à une lettre
    private void definir(char lettre, int colonne, int ligne, boolean bloquant) {
        images.put(lettre, tileset.getSubimage(
            colonne * TAILLE, ligne * TAILLE, TAILLE, TAILLE));
        if (bloquant) {
            bloquants.add(lettre);
        }
    }

    public void miseAJour() {
    }

    public void rendu(Graphics2D contexte) {
        for (int y = 0; y < nbLignes; y++) {
            for (int x = 0; x < nbColonnes; x++) {
                BufferedImage image = images.get(cases[y][x]);
                contexte.drawImage(image, x * TAILLE, y * TAILLE, null);
            }
        }
    }

    public boolean isObstacle(int x, int y) {
        if (x < 0 || y < 0 || x >= nbColonnes || y >= nbLignes) {
            return true;
        }
        return bloquants.contains(cases[y][x]);
    }

    public boolean caseAccessible(int x, int y) {
        return !isObstacle(x, y);
    }

    // trouverChemin et getCase : inchangés
}