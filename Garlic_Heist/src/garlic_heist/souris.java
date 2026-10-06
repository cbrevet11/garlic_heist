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
    private double[] nourriture = new double[1];
    private double[] position = new double[2];
    private String nom;
    
    public souris(){
        this.deplacement= deplacement;
        this.nourriture= nourriture;
        this.vie= 3;
        this.position=position;
        this.nom=nom
        
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
    public void mort_souris(){
        if(this.vie==0){
            System.out.println("la souris est morte"); 
    }
    public void perte_vie(){
    if (Math.abs(this.position[0] - position.BOTS[0]) 
            + Math.abs(this.position[1] - position.BOTS[1]) == 1){
        this.vie=this.vie-1;
    }
    

    }

     /*
    public void recuperation_objet(){
       if(len(nourriture=[])=0) and position.nourriture[]=position.souris
            this.nourriture[1]=nourriture
    }
    */
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
    }
    
}

