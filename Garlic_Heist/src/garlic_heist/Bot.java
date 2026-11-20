/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package garlic_heist;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
/**
 *
 * @author achainel
 */
public class Bot {
    
    private String nom;
    private int x;
    private int y;
    private double vitesse;
    private String direction;
    private ArrayList<int[]> chemin;
    private int prochaineEtape;
    private boolean cheminTrouve;
    private boolean estBloque = false;
    private long finBlocageTimestamp = 0;
    
        
    public Bot(String nom, int posX, int posY, double vitesse){
        this.nom = nom;
        this.x = posX;
        this.y = posY;
        this.vitesse = vitesse;
        this.direction = "droite";
    }
    
    public enum EtatBot {
        PATROUILLE,
        POURSUITE,
        RETOUR,
        TOURNE
    }
    
    private EtatBot etatActuel = EtatBot.PATROUILLE;
    private Random random = new Random();
    
    // Pour mémoriser le dernier point avant la poursuite
    private int dernierPointX, dernierPointY;
    
    
    // --------------------------------------------------------
    //              SETTER/GETTER
    // --------------------------------------------------------
    
    // recupere ou change la direction du regard du bot 
    public void setDirection(String direction){
        this.direction = direction;
    }
    public String getDirection(){
        return direction;
    }
    public int[] getPosition (){
        return new int[]{x,y};
    }
    
    
    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x ;
    }
    
    public int getY() {
        return y;
    }
    public void setY(int y) {
        this.y = y;
    }
    
    public double[] getposition(){
        return new double[]{this.x, this.y};
    }
    
    
    // --------------------------------------------------------
    //              DEPLACEMENT DU BOT
    // --------------------------------------------------------
    
    
    public void agir(Carte carte, souris souris) {
        
        piegeAil(carte);// 1. On vérifie d'abord s'il y a un piège à ail sous ses pieds

        
        if (estBloque) {// 2. S'il est bloqué par l'ail, il ne fait rien de ce tour
            return; 
        }
        // Transition vers la poursuite si on patrouille et qu'on détecte la souris
        if (etatActuel == EtatBot.PATROUILLE && detecterZone(souris.getPositionX(), souris.getPositionY())) {
            etatActuel = EtatBot.POURSUITE;
            dernierPointX = x;
            dernierPointY = y;
        }

        // Gestion des états 
        if (etatActuel == EtatBot.PATROUILLE) {
            if (chemin == null || prochaineEtape >= chemin.size()) {
                newDestinationAleatoire(carte);
            } else {
                avancer();
            }
        } 
        else if (etatActuel == EtatBot.POURSUITE) {
            if (detecterZone(souris.getPositionX(), souris.getPositionY())) {
                // Si le bot l'a attrapée
                if (Math.abs(souris.getPositionX() - x) <= 1 && Math.abs(souris.getPositionY() - y) <= 1) {
                    attaque(souris); 
                    etatActuel = EtatBot.RETOUR; 
                    deplacerVers(carte, dernierPointX, dernierPointY); 
                } 
                else {
                    // Poursuit la souris en temps réel
                    deplacerVers(carte, souris.getPositionX(), souris.getPositionY());
                    avancer();
                }
            } 
            else {
                // Souris perdue de vue, on retourne au point de départ
                etatActuel = EtatBot.RETOUR;
                deplacerVers(carte, dernierPointX, dernierPointY);
            }
        } 
        else if (etatActuel == EtatBot.RETOUR) {
            if (chemin == null || prochaineEtape >= chemin.size()) {
                etatActuel = EtatBot.PATROUILLE;
            } 
            else {
                avancer();
            }
        }
    }
    private void newDestinationAleatoire(Carte carte) {
    int maxEssais = 50; 
    int destX, destY;

    do {
        // Utilise les dimensions réelles de la carte (24x14)
        destX = random.nextInt(carte.getNbColonnes()); 
        destY = random.nextInt(carte.getNbLignes());
        
        // Si la case est accessible, on valide cette destination
        if (carte.caseAccessible(destX, destY)) {
            deplacerVers(carte, destX, destY);
            return;
        }
        maxEssais--;
    } while (maxEssais > 0);
}
    
    
    
    public boolean deplacerVers(Carte carte, int destinationX, int destinationY) {
        this.chemin = carte.trouverChemin(x, y, destinationX, destinationY);
        this.prochaineEtape = 0;

        // Sécurité : si le chemin est null ou vide alors qu'on n'est pas déjà sur place
        if (this.chemin == null || (this.chemin.isEmpty() && (x != destinationX || y != destinationY))) {
            this.chemin = new ArrayList<>(); // Évite le null
            this.cheminTrouve = false;
            return false;
        }

    this.cheminTrouve = true;
    return true;
}
    public boolean avancer() {

        if (prochaineEtape >= chemin.size()) {
            return false;
        }

        int nouvelleX = chemin.get(prochaineEtape)[0];
        int nouvelleY = chemin.get(prochaineEtape)[1];

        // Oriente le vampire selon son deplacement
        if (nouvelleX > x) {
            direction = "droite";
        }
        else if (nouvelleX < x) {
            direction = "gauche";
        }
        else if (nouvelleY > y) {
            direction = "bas";
        }
        else if (nouvelleY < y) {
            direction = "haut";
        }

        // Change sa position
        x = nouvelleX;
        y = nouvelleY;

        prochaineEtape++;

        return true;
    }
    
    public void attaque(souris souris){
        souris.retournerAuSpawn();
    }
    
    public void piegeAil(Carte carte) {
        // Si le bot est déjà bloqué, on vérifie si les 2 secondes sont écoulées
        if (estBloque) {
            if (System.currentTimeMillis() >= finBlocageTimestamp) {
                estBloque = false; // Le blocage est terminé
            }
            return; // Tant qu'il est bloqué, il ne fait rien d'autre
        }

        // Si la case actuelle contient de l'ail ('A')
        if (carte.getCase(x, y) == 'A') {
            estBloque = true;
            finBlocageTimestamp = System.currentTimeMillis() + 2000; // Bloqué pendant 2000 ms (2 secondes)
            System.out.println(nom + " a marché sur de l'ail et est bloqué !");
        }
    }
    
   
    public boolean detecterZone(int sourisX, int sourisY) {
        
        int distanceX = sourisX - x;
        int distanceY = sourisY - y;
                
        boolean autour = Math.abs(distanceX) <= 1 && Math.abs(distanceY) <= 1 && (distanceX != 0 || distanceY != 0);
        
        boolean devant = false;

        if (direction.equals("haut")) {
            devant = distanceY >= -5 && distanceY <= -1 && Math.abs(distanceX) <= 1;
        }
        else if (direction.equals("bas")) {
            devant = distanceY >= 1 && distanceY <= 5 && Math.abs(distanceX) <= 1;
        }
        else if (direction.equals("gauche")) {
            devant = distanceX >= -5 && distanceX <= -1 && Math.abs(distanceY) <= 1;
        }
        else if (direction.equals("droite")) {
            devant = distanceX >= 1 && distanceX <= 5 && Math.abs(distanceY) <= 1;
        }

        return autour || devant;
}
    public void tour(){
        if (direction.equals("haut")) {
        direction = "droite";
        }
        else if (direction.equals("droite")) {
            direction = "bas";
        }
        else if (direction.equals("bas")) {
            direction = "gauche";
        }
        else if (direction.equals("gauche")) {
            direction = "haut";
        }

    }
    
    public void detectionSouris(int souris1x, int souris1y, int souris2x, int souris2y, int souris3x, int souris3y, int souris4x, int souris4y ){
        int distanceX1 = souris1x - this.x;
        int distanceY1 = souris1y - this.y;
        
        int distanceX2 = souris2x - x;
        int distanceY2 = souris2y - y;
        
        int distanceX3 = souris3x- x;
        int distanceY3 = souris3y - y;
        
        int distanceX4 = souris4x- x;
        int distanceY4 = souris4y - y;
        
        if (Math.abs(distanceX1) <= 5 && Math.abs(distanceY1) <= 5 || Math.abs(distanceX2) <= 5 && Math.abs(distanceY2) <= 5 || Math.abs(distanceX3) <= 5 && Math.abs(distanceY3) <= 5 || Math.abs(distanceX4) <= 5 && Math.abs(distanceY4) <= 5) {
        }
    }
}