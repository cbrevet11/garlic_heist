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
        this.nourritures[1] = new double[]{5.0, 2.0};   // 2ème item
        this.nourritures[2] = new double[]{8.0, 7.0};   // 3ème item
    }

    // Getter pour récupérer les positions de toute la nourriture
    public double[][] getNourritures() {
        return nourritures;
    }

    public double[] getposition() {
        return this.positionobj;
    }

    public void nourriturePlacementAleatoire(double[] positionSouris) {
        int a = -1;                                                                    // Index de l'objet à replacer
        for (int k = 0; k < this.nourritures.length && a == -1; k++) {                 // Parcourt pour trouver le premier objet inactif (-1) sans break
            if (this.nourritures[k][0] == -1) {                                        
                a = k;                                                                 // Stocke l'index trouvé
            }
        }
        if (a == -1) { return; }                                                       // S'il n'y a aucun objet à replacer, on s'arrête

        int chiffrealeatoire2 = random.nextInt(this.tabPositionpossibleobjet.length);  // Tire une position aléatoire
        double[] nouvellePos = this.tabPositionpossibleobjet[chiffrealeatoire2];         // Récupère les coordonnées correspondantes                                         
        
        if (nouvellePos[0] == positionSouris[0] && nouvellePos[1] == positionSouris[1]) {  // Si la position est sur la souris, on recommence                    
            nourriturePlacementAleatoire(positionSouris); // Relance la méthode (récursion)
            return;                                       // Arrête cette instance
        }
        
        for (int i = 0; i < this.nourritures.length; i++) {  // Parcourt les autres nourritures pour éviter les superpositions
            if (i != a && this.nourritures[i][0] != -1 && 
                this.nourritures[i][0] == nouvellePos[0] && 
                this.nourritures[i][1] == nouvellePos[1]) {
                nourriturePlacementAleatoire(positionSouris); // Relance si la case est prise
                return;                                       // Arrête cette instance
            }
        }
        
        // CORRECTION : On crée un nouveau tableau indépendant pour ne pas corrompre tabPositionpossibleobjet
        this.nourritures[a] = new double[]{ nouvellePos[0], nouvellePos[1] }; // Attribue la position si tout est libre
    }
}