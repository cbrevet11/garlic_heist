package garlic_heist;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    
    
    private int largeur = 12;
    private int hauteur = 9;
    private int tailleTuile = 32;
    
    private BufferedImage uneTuile;

    public Carte() {
        try {
            BufferedImage tileset = ImageIO.read(getClass().getResource("images/tileSetMinecraft32x32.png"));
            uneTuile = tileset.getSubimage(0, 0, tailleTuile, tailleTuile);
        } catch (IOException ex) {
            Logger.getLogger(Carte.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void miseAJour() {

    }

    public void rendu(Graphics2D contexte) {
        contexte.drawImage(uneTuile, 0, 0, null);
    }
}