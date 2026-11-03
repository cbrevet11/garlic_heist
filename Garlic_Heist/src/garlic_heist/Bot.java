/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package garlic_heist;

/**
 *
 * @author achainel
 */
public class Bot {
    
    private String nom;
    private double[] position = new double[2];
    private int x;
    private int y;
    private double vitesse;
    private String direction;
    
        
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
    public int[] getposition (){
        return new int[]{x,y};
    }
    
    
    public int getX() {
        return x;
    }
    public void setX(x) {
        this.x = x ;   
    public int getY() {
        return y;
    }
    
    public void déplacement(){
        // si le vampire est en contacte avec une souris il l'attaque automatiquement ( détection des colisions avec une souris)
    }
    
    public void attaque(){
        
    }
    
    public void piegeAil(){
       
    }
    
    public boolean detecterDevant(Bot autre) {

    if (direction.equals("haut")) {
        return autre.getX() == x && autre.getY() < y;
    }

    if (direction.equals("bas")) {
        return autre.getX() == x && autre.getY() > y;
    }

    if (direction.equals("gauche")) {
        return autre.getY() == y && autre.getX() < x;
    }

    if (direction.equals("droite")) {
        return autre.getY() == y && autre.getX() > x;
    }

    return false;
}
    public void patrouille(){
        
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