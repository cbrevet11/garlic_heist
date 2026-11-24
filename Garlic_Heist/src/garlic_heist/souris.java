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

    public souris() {
        this.vie = 3;
        this.position = new double[]{0.0, 0.0};                                // Initialisation de la position de départ à 0,0
        this.nom = "Souris";
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
    private void appliquerLatence() {
        try {
            Thread.sleep(150);                                                 // Met le programme en pause pendant 300 ms
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();                                // Restaure l'état d'interruption en cas de besoin
        }
    }

    public void deplacementhaut() {
        this.position[1] = this.position[1] + 1;                               // Modifie la coordonnée Y vers le haut
        appliquerLatence();                                                    // Applique la latence de 0.3s
    }

    public void deplacementbas() {
        this.position[1] = this.position[1] - 1;                               // Modifie la coordonnée Y vers le bas
        appliquerLatence();                                                    // Applique la latence de 0.3s
    }

    public void deplacementgauche() {
        this.position[0] = this.position[0] - 1;                               // Modifie la coordonnée X vers la gauche
        appliquerLatence();                                                    // Applique la latence de 0.3s
    }

    public void deplacementdroite() {
        this.position[0] = this.position[0] + 1;                               // Modifie la coordonnée X vers la droite
        appliquerLatence();                                                    // Applique la latence de 0.3s
    }

    public void mort_souris() {
        if (this.vie <= 0) {                                                   // Vérifie si la souris n'a plus de vie
            System.out.println("La souris est morte !");                       // Message dans la console
            this.position[0] = -1;                                             // Fait disparaître la souris de l'axe X (hors de la grille)
            this.position[1] = -1;                                             // Fait disparaître la souris de l'axe Y (hors de la grille)
        }
    }

    public void perte_vie(Bot botEnnemi) {
        double[] posBot = botEnnemi.getposition();
        if (Math.abs(this.position[0] - posBot[0]) + Math.abs(this.position[1] - posBot[1]) == 1) {
            this.vie = this.vie - 1;                                           // Retire un point de vie
            if (this.vie < 0) {
                this.vie = 0;
            }
            
            if (this.vie > 0) {                                                // Si la souris a encore au moins 1 vie
                this.position[0] = 0.0;                                        // Repositionne la souris en X = 0.0
                this.position[1] = 0.0;                                        // Repositionne la souris en Y = 0.0
                System.out.println("Touchée ! Retour en (0,0). Vies restantes : " + this.vie);
            } else {
                mort_souris();                                                 // Sinon, gère la mort définitive
            }
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