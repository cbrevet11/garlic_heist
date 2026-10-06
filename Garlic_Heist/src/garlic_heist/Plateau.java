/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package garlic_heist;

/**
 *
 * @author ybelabed
 */
public class Plateau {
    private char[][] cases;
    private int nbLignes;
    private int nbColonnes;
    private String affichagex;
    
    
    public Plateau(int nbLignes, int nbColonnes) {
        this.nbLignes = nbLignes;
        this.nbColonnes = nbColonnes;
        cases = new char[nbLignes][nbColonnes];
    }
    
    public void affichage(){
        
    }
}
