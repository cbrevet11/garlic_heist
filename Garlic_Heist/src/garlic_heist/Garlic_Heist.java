/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package garlic_heist;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.Timer;



public class Garlic_Heist {


    public souris souris;
    public BOTS bots;
    public PlateauDeJeu fond;


    public Garlic_Heist () {
    }
    public void miseAJour() {
        this.souris.deplacementhaut();
        this.souris.deplacementbas();
        this.souris.deplacementdroite();
        this.souris.deplacementgauche();
    }
        
    }