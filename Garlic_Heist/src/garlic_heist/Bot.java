/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package garlic_heist;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
/**
 *
 * @author achainel
 */
public class Bot {
    
    private String nom;
    private int x;
    private int y;
    private double vitesse;
    private String direction;
    private ArrayList<int[]> chemin;
    private int prochaineEtape;
    private boolean cheminTrouve;
    private boolean estBloque = false;
    private long finBlocageTimestamp = 0;
    private int toursHorsChamp = 0;
    private static final int MAX_TOURS_HORS_CHAMP = 3;
    
        
    public Bot(String nom, int posX, int posY, double vitesse){
        this.nom = nom;
        this.x = posX;
        this.y = posY;
        this.vitesse = vitesse;
        this.direction = "droite";
    }
    private int[][] pointsPatrouille = {
        {8, 6},   // Point 1 
        {8, 14},  // Point 2 
        {29, 12}, // Point 3 
        {15, 18},   // Point 4 
        {19, 18},   // Point 5
        {28, 7}   // Point 6
    };
    
    private long finRotationTimestamp = 0;
    private int nombreToursEffectues = 0;
    private int indexPointActuel = 0;
    
   
    public enum EtatBot {
        PATROUILLE,
        POURSUITE,
        RETOUR,
        TOURNE
    }
    
    private EtatBot etatActuel = EtatBot.PATROUILLE;
    private Random random = new Random();
    
    // Pour mémoriser le dernier point avant la poursuite
    private int dernierPointX, dernierPointY;
    
    
    // --------------------------------------------------------
    //              SETTER/GETTER
    // --------------------------------------------------------
    
    // recupere ou change la direction du regard du bot 
    public void setDirection(String direction){
        this.direction = direction;
    }
    public String getDirection(){
        return direction;
    }
    public int[] getPosition (){
        return new int[]{x,y};
    }
    
    
    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x ;
    }
    
    public int getY() {
        return y;
    }
    public void setY(int y) {
        this.y = y;
    }
    
    public double[] getposition(){
        return new double[]{this.x, this.y};
    }
    
    
    // --------------------------------------------------------
    //              DEPLACEMENT DU BOT
    // --------------------------------------------------------
    
    
    public void agir(Carte carte, souris souris) {
        
        piegeAil(carte); // 1. On vérifie d'abord s'il y a un piège à ail sous ses pieds

        if (estBloque) { // 2. S'il est bloqué par l'ail, il ne fait rien de ce tour
            return; 
        }
        // Transition vers la poursuite si on patrouille et qu'on détecte la souris
        if (etatActuel == EtatBot.PATROUILLE && detecterZone(souris.getPositionX(), souris.getPositionY())) {
            etatActuel = EtatBot.POURSUITE;
            dernierPointX = x;
            dernierPointY = y;
        }

        // Gestion des états 
        if (etatActuel == EtatBot.PATROUILLE) {
            if (chemin == null || chemin.isEmpty()) {
                // Premier lancement ou chemin invalide : on choisit directement le premier point de la ronde
                prochaineDestinationPatrouille(carte);
            } 
            else if (prochaineEtape >= chemin.size()) {
                // Arrivé au bout du chemin : on passe en mode TOURNE
                etatActuel = EtatBot.TOURNE;
                nombreToursEffectues = 0;
                finRotationTimestamp = System.currentTimeMillis(); 
            } 
            else {
                // En cours de route vers le point de patrouille
                avancer();
            }
        } 
        else if (etatActuel == EtatBot.TOURNE) {
            // Fait une pause de 300ms entre chaque quart de tour pour faire son 360°
            if (System.currentTimeMillis() >= finRotationTimestamp) {
                tour(); 
                nombreToursEffectues++;
                finRotationTimestamp = System.currentTimeMillis() + 300; 

                // Quand il a fait ses 4 quarts de tour, il repart vers le prochain point de la ronde
                if (nombreToursEffectues >= 4) {
                    etatActuel = EtatBot.PATROUILLE;
                    prochaineDestinationPatrouille(carte);
                }
            }
        }
        else if (etatActuel == EtatBot.POURSUITE) {
        if (detecterZone(souris.getPositionX(), souris.getPositionY())) {
            // Il vous voit : on réinitialise le compteur de perte de vue et on continue
            toursHorsChamp = 0;

            // Si le bot est au contact
            if (Math.abs(souris.getPositionX() - x) <= 1 && Math.abs(souris.getPositionY() - y) <= 1) {
                etatActuel = EtatBot.PATROUILLE;
                prochaineDestinationPatrouille(carte);
            } 
            else {
                boolean cheminTrouveVersSouris = deplacerVers(carte, souris.getPositionX(), souris.getPositionY());
                if (cheminTrouveVersSouris && chemin != null && !chemin.isEmpty()) {
                    avancer();
                } 
                else {
                    etatActuel = EtatBot.PATROUILLE;
                    prochaineDestinationPatrouille(carte);
                }
            }
        } 
        else {
            // Il ne vous voit plus directement, mais on lui laisse un court délai de grâce (mémoire)
            toursHorsChamp++;
            if (toursHorsChamp < MAX_TOURS_HORS_CHAMP) {
                // Il continue de avancer vers votre dernière position connue pendant quelques tours
                boolean cheminTrouveVersSouris = deplacerVers(carte, souris.getPositionX(), souris.getPositionY());
                if (cheminTrouveVersSouris && chemin != null && !chemin.isEmpty()) {
                    avancer();
                }
            } else {
                // Le délai est dépassé : il vous a vraiment perdue de vue, il repart en patrouille
                toursHorsChamp = 0;
                etatActuel = EtatBot.PATROUILLE;
                prochaineDestinationPatrouille(carte);
            }
        }
    }
    }
    
    private void prochaineDestinationPatrouille(Carte carte) {
        if (pointsPatrouille.length == 0){
            return;
        }
            System.out.println("--- Prochain point de patrouille index " + indexPointActuel + " : X=" + pointsPatrouille[indexPointActuel][0] + ", Y=" + pointsPatrouille[indexPointActuel][1]);
            // Récupère les coordonnées X et Y du point actuel dans le tableau
            int destX = pointsPatrouille[indexPointActuel][0];
            int destY = pointsPatrouille[indexPointActuel][1];

            // Passe au point suivant du tableau pour la prochaine fois 
            // (le modulo '%' permet de revenir à 0 automatiquement quand on arrive à la fin du tableau)
            indexPointActuel = (indexPointActuel + 1) % pointsPatrouille.length;

            // Demande au bot de se déplacer vers ce point
            deplacerVers(carte, destX, destY);
    }

    
    public boolean deplacerVers(Carte carte, int destinationX, int destinationY) {
        this.chemin = carte.trouverChemin(x, y, destinationX, destinationY);
        this.prochaineEtape = 0;
        
        // Sécurité : si le chemin est null ou vide alors qu'on n'est pas déjà sur place
        if (this.chemin == null || (this.chemin.isEmpty() && (x != destinationX || y != destinationY))) {
            this.chemin = new ArrayList<>(); // Évite le null
            this.cheminTrouve = false;
            return false;
        }

    this.cheminTrouve = true;
    return true;
}
    public boolean avancer() {

        if (prochaineEtape >= chemin.size()) {
            return false;
        }

        int nouvelleX = chemin.get(prochaineEtape)[0];
        int nouvelleY = chemin.get(prochaineEtape)[1];

        // Oriente le vampire selon son deplacement
        if (nouvelleX > x) {
            direction = "droite";
        }
        else if (nouvelleX < x) {
            direction = "gauche";
        }
        else if (nouvelleY > y) {
            direction = "bas";
        }
        else if (nouvelleY < y) {
            direction = "haut";
        }

        // Change sa position
        x = nouvelleX;
        y = nouvelleY;

        prochaineEtape++;

        return true;
    }
   
    
    public void piegeAil(Carte carte) {
        // Si le bot est déjà bloqué, on vérifie si les 2 secondes sont écoulées
        if (estBloque) {
            if (System.currentTimeMillis() >= finBlocageTimestamp) {
                estBloque = false; // Le blocage est terminé
            }
            return; // Tant qu'il est bloqué, il ne fait rien d'autre
        }

        // Si la case actuelle contient de l'ail ('A')
        if (carte.getCase(x, y) == 'A') {
            estBloque = true;
            finBlocageTimestamp = System.currentTimeMillis() + 2000; // Bloqué pendant 2000 ms (2 secondes)
            System.out.println(nom + " a marché sur de l'ail et est bloqué !");
        }
    }
    
   
    public boolean detecterZone(int sourisX, int sourisY) {
        int distanceX = sourisX - x;
        int distanceY = sourisY - y;

        // Portée équilibrée : 5 en patrouille (pour qu'il ne vous voie pas de trop loin), 7 en poursuite
        int portee = (etatActuel == EtatBot.POURSUITE) ? 7 : 5;
        int porteeArriere = (etatActuel == EtatBot.POURSUITE) ? 3 : 2;

        boolean autour = Math.abs(distanceX) <= 1 && Math.abs(distanceY) <= 1;

        boolean champDeVision = false;

        if (direction.equals("haut")) {
            champDeVision = (distanceY >= -portee && distanceY <= -1) && (Math.abs(distanceX) <= (portee / 2));
        }
        else if (direction.equals("bas")) {
            champDeVision = (distanceY >= 1 && distanceY <= portee) && (Math.abs(distanceX) <= (portee / 2));
        }
        else if (direction.equals("gauche")) {
            champDeVision = (distanceX >= -portee && distanceX <= -1) && (Math.abs(distanceY) <= (portee / 2));
        }
        else if (direction.equals("droite")) {
            champDeVision = (distanceX >= 1 && distanceX <= portee) && (Math.abs(distanceY) <= (portee / 2));
        }

        boolean visionArriere = false;
        if (direction.equals("haut")) {
            visionArriere = (distanceY >= 1 && distanceY <= porteeArriere) && (Math.abs(distanceX) <= 1);
        }
        else if (direction.equals("bas")) {
            visionArriere = (distanceY >= -porteeArriere && distanceY <= -1) && (Math.abs(distanceX) <= 1);
        }
        else if (direction.equals("gauche")) {
            visionArriere = (distanceX >= 1 && distanceX <= porteeArriere) && (Math.abs(distanceY) <= 1);
        }
        else if (direction.equals("droite")) {
            visionArriere = (distanceX >= -porteeArriere && distanceY <= -1) && (Math.abs(distanceX) <= 1);
        }

        return autour || champDeVision || visionArriere;
    }
    
    
    public void tour(){
        if (direction.equals("haut")) {
        direction = "droite";
        }
        else if (direction.equals("droite")) {
            direction = "bas";
        }
        else if (direction.equals("bas")) {
            direction = "gauche";
        }
        else if (direction.equals("gauche")) {
            direction = "haut";
        }

    }
    
    public void detectionSouris(int souris1x, int souris1y, int souris2x, int souris2y, int souris3x, int souris3y, int souris4x, int souris4y ){
        int distanceX1 = souris1x - this.x;
        int distanceY1 = souris1y - this.y;
        
        int distanceX2 = souris2x - x;
        int distanceY2 = souris2y - y;
        
        int distanceX3 = souris3x- x;
        int distanceY3 = souris3y - y;
        
        int distanceX4 = souris4x- x;
        int distanceY4 = souris4y - y;
        
        if (Math.abs(distanceX1) <= 5 && Math.abs(distanceY1) <= 5 || Math.abs(distanceX2) <= 5 && Math.abs(distanceY2) <= 5 || Math.abs(distanceX3) <= 5 && Math.abs(distanceY3) <= 5 || Math.abs(distanceX4) <= 5 && Math.abs(distanceY4) <= 5) {
        }
    }
}