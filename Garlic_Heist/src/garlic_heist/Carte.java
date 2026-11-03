package garlic_heist;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class Carte {

    public static final int TAILLE = 32;   // taille d'une case en pixels

    private char[][] cases;
    private int nbLignes;
    private int nbColonnes;
    private Map<Character, BufferedImage> images = new HashMap<>();

    public Carte(int nbLignes, int nbColonnes) {
        this.nbLignes = nbLignes;
        this.nbColonnes = nbColonnes;
        cases = new char[nbLignes][nbColonnes];

        for (int y = 0; y < nbLignes; y++) {
            for (int x = 0; x < nbColonnes; x++) {
                cases[y][x] = '.';
            }
        }

        // un caractère = une image
        images.put('.', charger("/garlic_heist/sol.png"));
        images.put('#', charger("/garlic_heist/mur.png"));
    }

    private BufferedImage charger(String chemin) {
        try {
            return ImageIO.read(getClass().getResource(chemin));
        } catch (Exception e) {
            System.out.println("Image introuvable : " + chemin);
            return null;
        }
    }

    public void creationObstacle() {
        cases[2][2] = '#';
        cases[3][3] = '#';
        cases[4][4] = '#';
    }

    public boolean isObstacle(int x, int y) {
        if (x < 0 || y < 0 || x >= nbColonnes || y >= nbLignes) {
            return true;   // en dehors de la carte = bloqué
        }
        return cases[y][x] == '#';
    }

    // Dessine toute la carte, case par case
    public void rendu(Graphics2D g) {
        for (int y = 0; y < nbLignes; y++) {
            for (int x = 0; x < nbColonnes; x++) {
                BufferedImage img = images.get(cases[y][x]);
                if (img != null) {
                    g.drawImage(img, x * TAILLE, y * TAILLE, null);
                }
            }
        }
    }

    // Garde votre affichage console (utile pour déboguer), sans fenêtre
    public void affichage() {
        String bordure = "+" + "-".repeat(nbColonnes) + "+";
        System.out.println(bordure);
        for (int i = 0; i < nbLignes; i++) {
            System.out.print("|");
            for (int j = 0; j < nbColonnes; j++) {
                System.out.print(cases[i][j]);
            }
            System.out.println("|");
        }
        System.out.println(bordure);
    }
}