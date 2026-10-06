/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package garlic_heist;

/**
 *
 * @author vdaviet
 */
public class souris {
    private String deplacement;
    private int vie;
    private int nourriture;
    private double[] position = new double[2];
    
    
    public souris(){
        this.deplacement= deplacement;
        this.nourriture= nourriture;
        this.vie= vie;
        this.position=position;
        
    }
    public void deplacementhaut(){
        this.position[1]=this.position[1]+1;
    }
    public void deplacementbas(){
        this.position[1]=this.position[1]-1;
    }
    public void deplacementgauche(){
        this.position[1]=this.position[0]+1;
    }
    public void deplacementdroite(){
        this.position[1]=this.position[0]-1;
    }
    public int reaparition
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
    }
    
}
