/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package garlic_heist;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
/**
 *
 * @author achainel
 */
public class Garlic_Heist {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        //Plateau Newgame = new Plateau(5,5);
        //Newgame.affichage();
        System.out.println(new java.io.File("images/plateau.png").getAbsolutePath());
        System.out.println(new java.io.File("images/plateau.png").exists());
        ImageIcon image = new ImageIcon("images/plateau.png");

        JFrame fenetre = new JFrame("Garlic Heist");
        fenetre.add(new JLabel(image));
        fenetre.pack();
        fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fenetre.setVisible(true);
    }

}
