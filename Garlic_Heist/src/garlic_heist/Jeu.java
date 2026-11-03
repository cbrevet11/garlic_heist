/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package garlic_heist;

import java.awt.Graphics2D;

public class Jeu {

    private Carte Carte;
     
    public Jeu() {        
        this.Carte = new Carte();
    }

    public void miseAJour() {
        this.Carte.miseAJour();
    }

    public void rendu(Graphics2D contexte) {
        this.Carte.rendu(contexte);
    }

}


