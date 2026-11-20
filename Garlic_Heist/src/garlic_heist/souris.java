/*
- Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
- Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
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
    private Objets[] nourrituresouris = new Objets[1];
    private String nom;
    private Bot bot;
    private Objets nourritures;
    private Objets positionobj;        
          
    public souris() {
        this.vie = 3;
        this.position = new double[]{0.0, 0.0}; // Correction : initialisation propre de la position
        this.nom = "Souris";
    }

    public double[] getposition() {
        return position;
    }

    public String getnom() {
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
  
    public void recuperation_objet(Objets mesObjets) {
        // Récupère toutes les nourritures gérées par la classe Objets
        double[][] toutesLesNourritures = mesObjets.getNourritures();
        
        if (toutesLesNourritures != null) {
            for (int i = 0; i < toutesLesNourritures.length; i++) {
                double[] posobjets = toutesLesNourritures[i];
                
                // Si l'objet n'est pas déjà ramassé (!= -1) et que la souris est sur sa case
                if (posobjets[0] != -1 && this.position[0] == posobjets[0] && this.position[1] == posobjets[1]) {
                    // On met les coordonnées à -1 pour qu'il disparaisse de la grille
                    posobjets[0] = -1; 
                    posobjets[1] = -1;  
                    System.out.println("Nourriture récupérée !");
                }
            }
        }
    }
}