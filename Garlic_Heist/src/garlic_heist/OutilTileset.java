package garlic_heist;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

public class OutilTileset {

    public static void main(String[] args) throws Exception {
        final int taille = 42;
        final int zoom = 2;   // agrandit l'image pour mieux lire

        BufferedImage tileset = ImageIO.read(
            OutilTileset.class.getClassLoader().getResource("resources/tileset.png"));

        BufferedImage vue = new BufferedImage(
            tileset.getWidth() * zoom, tileset.getHeight() * zoom, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = vue.createGraphics();
        g.drawImage(tileset, 0, 0, vue.getWidth(), vue.getHeight(), null);
        g.setFont(new Font("SansSerif", Font.BOLD, 11));

        int nbColonnes = tileset.getWidth() / taille;
        int nbLignes = tileset.getHeight() / taille;
        for (int l = 0; l < nbLignes; l++) {
            for (int c = 0; c < nbColonnes; c++) {
                int px = c * taille * zoom;
                int py = l * taille * zoom;
                g.setColor(new Color(255, 0, 0, 150));
                g.drawRect(px, py, taille * zoom, taille * zoom);
                g.setColor(Color.BLACK);
                g.drawString(c + "," + l, px + 4, py + 15);
                g.setColor(Color.YELLOW);
                g.drawString(c + "," + l, px + 3, py + 14);
            }
        }

        JLabel label = new JLabel(new ImageIcon(vue));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int c = e.getX() / (taille * zoom);
                int l = e.getY() / (taille * zoom);
                System.out.println("colonne = " + c + ", ligne = " + l);
            }
        });

        JFrame fenetre = new JFrame("Tileset");
        fenetre.add(new JScrollPane(label));
        fenetre.setSize(1000, 700);
        fenetre.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        fenetre.setVisible(true);
    }
}