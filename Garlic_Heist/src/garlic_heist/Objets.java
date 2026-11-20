package garlic_heist;

import java.util.Random;

/**
 *
 * @author vdaviet
 */
public class Objets {
    private double[] positionobj = new double[2];
    private double[][] nourritures = new double[3][2];
    private Random random = new Random();
    private double[][] tabPositionpossibleobjet = new double[10][2];

    public Objets() {
        this.positionobj = new double[2];

        this.tabPositionpossibleobjet = new double[][]{
            {0.0, 0.0},
            {1.0, 2.0},
            {2.0, 0.0},
            {3.0, 5.0},
            {4.0, 4.0},
            {5.0, 1.0},
            {6.0, 7.0},
            {7.0, 3.0},
            {8.0, 6.0},
            {9.0, 9.0}
        };

        this.nourritures[0] = new double[]{5.0, 5.0};   // 1er item
        this.nourritures[1] = new double[]{5.0, 2.0};  // 2ème item
        this.nourritures[2] = new double[]{8.0, 7.0};   // 3ème item
    }

    // Getter pour récupérer les positions de toute la nourriture
    public double[][] getNourritures() {
        return nourritures;
    }

    public double[] getposition() {
        return this.positionobj;
    }

    public void nourriturePlacementAleatoire() {
        int a = random.nextInt(this.nourritures.length); // Choisit quelle nourriture (0, 1 ou 2) fait pop
        int chiffrealeatoire2 = random.nextInt(this.tabPositionpossibleobjet.length); // Tire un index de 0 à 9
        this.nourritures[a] = this.tabPositionpossibleobjet[chiffrealeatoire2];
    }
}