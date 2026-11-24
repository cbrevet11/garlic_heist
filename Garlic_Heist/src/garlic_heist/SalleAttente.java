/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package garlic_heist;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class SalleAttente extends JFrame {

    public SalleAttente() {

        setTitle("Garlic Heist - Salle d'attente");

        setSize(900, 600);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLocationRelativeTo(null);

        JLabel titre = new JLabel(
                "GARLIC HEIST - SALLE D'ATTENTE",
                SwingConstants.CENTER
        );

        add(titre);

        setVisible(true);
    }

    public static void main(String[] args) {

        new SalleAttente();
    }
}
