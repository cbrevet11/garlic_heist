/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package garlic_heist;
import java.util.ArrayList;
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
    
        
    public Bot(String nom, int posX, int posY, double vitesse){
        this.nom = nom;
        this.x = posX;
        this.y = posY;
        this.vitesse = vitesse;
        this.direction = "droite";
    }
    
    
    
    
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
    
    
    
    
    
    public boolean deplacerVers(Carte carte, int destinationX,int destinationY) {

        chemin = carte.trouverChemin(x, y, destinationX, destinationY);
        prochaineEtape = 0;
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
    
    public void attaque(){
        
    }
    
    public void piegeAil(){
       
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