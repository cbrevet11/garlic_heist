import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import javax.imageio.ImageIO;

public class FaireTileset {
    public static void main(String[] args) throws Exception {
        File dossier = new File("src/Tuiles");   // ou "src/garlic_heist/Tuiles" s'il est dans le package          // dossier avec tes images
        int taille = 42;                            // taille d'une tuile
        int colonnes = 8;                           // tuiles par ligne

        File[] fichiers = dossier.listFiles((d, nom) -> nom.endsWith(".png"));
        Arrays.sort(fichiers);                      // ordre alphabétique = numéros

        int lignes = (fichiers.length + colonnes - 1) / colonnes;
        BufferedImage planche = new BufferedImage(
            colonnes * taille, lignes * taille, BufferedImage.TYPE_INT_ARGB);

        for (int i = 0; i < fichiers.length; i++) {
            BufferedImage t = ImageIO.read(fichiers[i]);
            planche.getGraphics().drawImage(
                t, (i % colonnes) * taille, (i / colonnes) * taille,
                taille, taille, null);
            System.out.println(i + " = " + fichiers[i].getName());
        }
        ImageIO.write(planche, "png", new File("src/resources/tileset.png"));
    }
}