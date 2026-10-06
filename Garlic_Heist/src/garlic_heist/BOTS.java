/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package garlic_heist;

/**
 *
 * @author achainel
 */
public class BOTS {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }
    private String nom;
    private double[] position = new double[2];
    private double vitesse;
    private int x;
    private int y;
    
        
    public BOTS(String nom, int posX, int posY, double vitesse){
        this.nom = "Tom";
        this.x = 0;
        this.y = 0;
        this.vitesse = 0;
    }
        public double[] getposition (){
        return position;
    
    }
    
    public void déplacement(){
        // si le vampire est en contacte avec une souris il l'attaque automatiquement ( détection des colisions avec une souris)
    }
    
    public void attaque(){
        
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
