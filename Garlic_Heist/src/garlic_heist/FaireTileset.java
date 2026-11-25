import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import javax.imageio.ImageIO;

public class FaireTileset {
    public static void main(String[] args) throws Exception {

        File dossier = new File("src/Tuiles");
        int taille = 42;
        int colonnes = 8;

        File[] fichiers = dossier.listFiles(
            (d, nom) -> nom.endsWith(".png")
        );

        // Tri du plus ancient au plus récent
        Arrays.sort(fichiers, (f1, f2) ->
        Long.compare(f1.lastModified(), f2.lastModified()));

        int lignes = (fichiers.length + colonnes - 1) / colonnes;

        BufferedImage planche = new BufferedImage(
            colonnes * taille,
            lignes * taille,
            BufferedImage.TYPE_INT_ARGB
        );

        for (int i = 0; i < fichiers.length; i++) {

            BufferedImage t = ImageIO.read(fichiers[i]);

            planche.getGraphics().drawImage(
                t,
                (i % colonnes) * taille,
                (i / colonnes) * taille,
                taille,
                taille,
                null
            );

            System.out.println(i + " = " + fichiers[i].getName()
                + " | " + new java.util.Date(fichiers[i].lastModified()));
        }

        ImageIO.write(
            planche,
            "png",
            new File("src/resources/tileset.png")
        );
    }
}