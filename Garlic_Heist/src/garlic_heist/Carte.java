package garlic_heist;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

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
            BufferedImage tileset = ImageIO.read(getClass().getClassLoader().getResource("resources/tileset_chateau.png"));
            uneTuile = tileset.getSubimage(50, 0, tailleTuile, tailleTuile);
        } catch (IOException ex) {
            Logger.getLogger(Carte.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void miseAJour() {

    }

    public void rendu(Graphics2D contexte) {
        
        for (int x = 0; x<largeur;x++ ){
            for (int y = 0; y<hauteur; y++){
                contexte.drawImage(uneTuile, x, y, null);
            }
        }
    }
    
    
    public boolean caseAccessible(int x, int y) {

    // Vérifie que la case est bien dans les limites de la carte
    if (x < 0 || y < 0 || x >= nbColonnes || y >= nbLignes) {
        return false;
    }

    // Une case est accessible si ce n'est pas un mur
    return cases[y][x] == '.';
}


// Recherche un chemin entre deux positions
public ArrayList<int[]> trouverChemin(int departX, int departY,
                                     int arriveeX, int arriveeY) {

    // Liste qui va contenir les cases à explorer
    Queue<int[]> file = new LinkedList<>();

    // Tableau pour savoir quelles cases ont déjà été visitées
    boolean[][] visite = new boolean[nbLignes][nbColonnes];

    // Tableau qui mémorise la case précédente pour chaque case
    int[][][] precedent = new int[nbLignes][nbColonnes][2];

    // Vérifie que le départ et l'arrivée sont accessibles
    if (!caseAccessible(departX, departY)
            || !caseAccessible(arriveeX, arriveeY)) {
        return new ArrayList<>();
    }

    // Ajoute la position de départ dans la file
    file.add(new int[]{departX, departY});
    visite[departY][departX] = true;

    // Déplacements possibles : haut, bas, gauche, droite
    int[] dx = {0, 0, -1, 1};
    int[] dy = {-1, 1, 0, 0};

    boolean trouve = false;

    // Explore les cases tant qu'il en reste à visiter
    while (!file.isEmpty()) {

        // Récupère la première position de la file
        int[] position = file.poll();

        int x = position[0];
        int y = position[1];

        // Vérifie si on est arrivé à destination
        if (x == arriveeX && y == arriveeY) {
            trouve = true;
            break;
        }

        // Teste les 4 directions
        for (int i = 0; i < 4; i++) {

            int nouveauX = x + dx[i];
            int nouveauY = y + dy[i];

            // Vérifie que la nouvelle case est accessible et non visitée
            if (caseAccessible(nouveauX, nouveauY)
                    && !visite[nouveauY][nouveauX]) {

                visite[nouveauY][nouveauX] = true;

                // Mémorise la case d'où l'on vient
                precedent[nouveauY][nouveauX][0] = x;
                precedent[nouveauY][nouveauX][1] = y;

                // Ajoute cette nouvelle position à explorer
                file.add(new int[]{nouveauX, nouveauY});
            }
        }
    }

    // Liste qui contiendra le chemin final
    ArrayList<int[]> chemin = new ArrayList<>();

    // Si une route a été trouvée, on la reconstitue
    if (trouve) {

        int x = arriveeX;
        int y = arriveeY;

        // Remonte depuis l'arrivée jusqu'au départ
        while (x != departX || y != departY) {

            // Ajoute la position au début du chemin
            chemin.add(0, new int[]{x, y});

            // Récupère la position précédente
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
        return '#';
    }

    return cases[y][x];
}
    
}