package garlic_heist;

/**
 *
 * @author vdaviet 
 */
public class souris {

    private String deplacement;
    private int vie;
    private double[] nourriture = new double[1];
    private double[] position = new double[2];
    private Objets[] nourrituresouris = new Objets[1];
    private String nom;
    private Bot bot;
    private Objets nourritures;
    private Objets positionobj;
    private double spawnX;
    private double spawnY;

    public souris() {
        this.vie = 3;
        this.position = new double[]{0.0, 0.0}; // Initialisation propre de la position
        this.nom = "Souris";
        this.spawnX = position[0]; 
        this.spawnY = position[1];
    }

    public void retournerAuSpawn() {
        this.position[0] = spawnX;
        this.position[1] = spawnY;
    }

    public double[] getposition() {
        return position;
    }

    public int getPositionX() {
        return (int) this.position[0];
    }

    public int getPositionY() {
        return (int) this.position[1];
    }

    public String getnom() {
        return nom;
    }

    // Méthode interne pour appliquer la pause de 0.3 seconde (300 ms)
    private void appliquerLatence() {
        try {
            Thread.sleep(300);                                         // Met le programme en pause pendant 300 ms
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();                        // Restaure l'état d'interruption en cas de besoin
        }
    }

    public void deplacementhaut() {
        this.position[1] = this.position[1] + 1;                       // Modifie la coordonnée Y vers le haut
        appliquerLatence();                                            // Applique la latence de 0.3s
    }

    public void deplacementbas() {
        this.position[1] = this.position[1] - 1;                       // Modifie la coordonnée Y vers le bas
        appliquerLatence();                                            // Applique la latence de 0.3s
    }

    public void deplacementgauche() {
        this.position[0] = this.position[0] - 1;                       // Modifie la coordonnée X vers la gauche
        appliquerLatence();                                            // Applique la latence de 0.3s
    }

    public void deplacementdroite() {
        this.position[0] = this.position[0] + 1;                       // Modifie la coordonnée X vers la droite
        appliquerLatence();                                            // Applique la latence de 0.3s
    }

    public void mort_souris() {
        if (this.vie <= 0) {
            System.out.println("la souris est morte");
        }
    }

    public void perte_vie(Bot botEnnemi) {
        double[] posBot = botEnnemi.getposition();
        if (Math.abs(this.position[0] - posBot[0]) + Math.abs(this.position[1] - posBot[1]) == 1) {
            this.vie = this.vie - 1;
            if (this.vie < 0) {
                this.vie = 0;
            }
            mort_souris();
        }
    }

    public void recuperation_objet(Objets mesObjets) {
        // Récupère toutes les nourritures gérées par la classe Objets
        double[][] toutesLesNourritures = mesObjets.getNourritures();

        if (toutesLesNourritures != null) {
            for (int i = 0; i < toutesLesNourritures.length; i++) {
                double[] posobjets = toutesLesNourritures[i];

                // Si l'objet n'est pas déjà ramassé (!= -1) et que la souris est sur sa case
                if (posobjets[0] != -1 && this.position[0] == posobjets[0] && this.position[1] == posobjets[1]) {
                    // On met les coordonnées à -1 pour qu'il disparaisse de la grille
                    posobjets[0] = -1;
                    posobjets[1] = -1;
                    System.out.println("Nourriture récupérée !");
                }
            }
        }
    }
}