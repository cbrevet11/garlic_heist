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
    private double x;
    private double y;
    
        
    public BOTS(String nom, int posX, int posY, double vitesse){
        this.nom = "Tom";
        this.x = 0;
        this.y = 0;
        this.vitesse = 0;
    }
        public double[] getposition (){
        return position;
    
    }
    
    public déplacement(){
        // si le vampire est en contacte avec une souris il l'attaque automatiquement ( détection des colisions avec une souris)
    }
    
    public attaque(){
        
    }
    
    public patrouille(){
        
    }
    
    public void detectionSouris(double souris1,double souris2,double souris3, ){
        int distanceX1 = souris1 - this.x;
        int distanceY1 = souris1 - this.y;
        
        int distanceX2 = souris2 - x;
        int distanceY2 = souris2 - y;
        
        int distanceX3 = souris3 - x;
        int distanceY3 = souris3 - y;
        
        if (Math.abs(distanceX1) <= 5 && Math.abs(distanceY1) <= 5 || Math.abs(distanceX2) <= 5 && Math.abs(distanceY2) <= 5 || Math.abs(distanceX3) <= 5 && Math.abs(distanceY3) <= 5) {
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
}
