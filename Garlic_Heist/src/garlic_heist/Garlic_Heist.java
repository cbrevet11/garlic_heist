package garlic_heist;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Garlic_Heist extends JFrame implements ActionListener {

    // Taille "logique" du jeu = taille de la carte (29 colonnes x 17 lignes de tuiles).
    // Le jeu dessine toujours dans cette zone, ensuite elle est agrandie ou
    // réduite pour remplir la fenêtre. Garde tous tes niveaux à cette taille.
    public static final int LARGEUR = 38 * Carte.TAILLE;
    public static final int HAUTEUR = 20 * Carte.TAILLE;

    private BufferedImage buffer;
    private Graphics2D contexteBuffer;
    private Jeu jeu;
    private Timer timer;
    private Affichage affichage;

    // Zoom et décalage actuels (utiles pour la souris)
    private double echelle = 1;
    private int decalageX = 0;
    private int decalageY = 0;

    // Panneau qui affiche le buffer à la bonne taille, centré
    private class Affichage extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            // plus grand zoom possible sans déformer l'image
            echelle = Math.min(getWidth() / (double) LARGEUR,
                               getHeight() / (double) HAUTEUR);
            int w = (int) (LARGEUR * echelle);
            int h = (int) (HAUTEUR * echelle);
            decalageX = (getWidth() - w) / 2;
            decalageY = (getHeight() - h) / 2;

            g2.drawImage(buffer, decalageX, decalageY, w, h, null);
        }
    }

    public Garlic_Heist() {
        // initialisation de la fenetre
        this.setTitle("TileMupping");
        this.setResizable(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.affichage = new Affichage();
        this.affichage.setBackground(new java.awt.Color(33, 26, 20));  // couleur du vide autour de la carte
        this.affichage.setPreferredSize(new Dimension(LARGEUR * 3 / 4, HAUTEUR * 3 / 4));  // taille si on réduit la fenêtre
        this.setContentPane(this.affichage);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);  // démarre en plein écran

        // Creation du buffer pour l'affichage du jeu et recuperation du contexte graphique
        this.buffer = new BufferedImage(LARGEUR, HAUTEUR, BufferedImage.TYPE_INT_ARGB);
        this.contexteBuffer = this.buffer.createGraphics();

        // Creation du jeu
        this.jeu = new Jeu();

        // Creation du Timer qui appelle this.actionPerformed() tous les 40 ms
        this.timer = new Timer(40, this);
        this.timer.start();
    }

    // Convertit une position de la fenêtre (clic de souris) en position dans le jeu
    public Point versJeu(Point p) {
        int x = (int) ((p.x - decalageX) / echelle);
        int y = (int) ((p.y - decalageY) / echelle);
        return new Point(x, y);
    }

    // Methode appelee par le timer et qui contient la boucle de jeu
    @Override
    public void actionPerformed(ActionEvent e) {
        this.jeu.miseAJour();
        this.jeu.rendu(contexteBuffer);
        this.affichage.repaint();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        // affichage fenêtre de jeu
        Garlic_Heist fenetre = new Garlic_Heist();
        fenetre.setVisible(true);

        souris s = new souris();
    }
}