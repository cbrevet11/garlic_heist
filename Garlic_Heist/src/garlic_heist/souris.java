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
    private Objets[] nourrituresouris = new Objets [1];
    private String nom;
    private Bot bot;
    private Objets nourritures;
    private Objets positionobj;        
          
    
    
    public souris() {
        this.deplacement = deplacement;
        this.vie = 3;
        this.position = position;
        this.nom = "Souris";
    }
    public double[] getposition(){
        return position;
    }
    public String getnom(){
        return nom;
    }
    public void deplacementhaut() {
        this.position[1] = this.position[1] + 1; 
    }

    public void deplacementbas() {
        this.position[1] = this.position[1] - 1; 
    }

    public void deplacementgauche() {
        this.position[0] = this.position[0] - 1; 
    }

    public void deplacementdroite() {
        this.position[0] = this.position[0] + 1; 
    }

    public void mort_souris() {
        if (this.vie <= 0) {
            System.out.println("la souris est morte");
        }
    }

    public void perte_vie(Bot botEnnemi) {
        double[] posBot = botEnnemi.getposition(); 
        if (Math.abs(this.position[0] - posBot[0]) + Math.abs(this.position[1] - posBot[1]) == 1) {
            this.vie = this.vie - 1;
            if (this.vie < 0) {
                this.vie = 0; 
            }
            mort_souris(); 
        }
    }
  
    public void recuperation_objet(Objets nourriture) {
        double[][] toutesLesNourritures = nourriture.getNourritures();
        for (int i = 0; i < toutesLesNourritures.length; i++) {
            double[] posobjets = toutesLesNourritures[i];           
            if (this.position[0] == posobjets[0] && this.position[1] == posobjets[1]) {
                this.nourrituresouris[0] = nourriture;            
                posobjets[0] = -1; 
                posobjets[1] = -1;  
            }
        }
    }
}