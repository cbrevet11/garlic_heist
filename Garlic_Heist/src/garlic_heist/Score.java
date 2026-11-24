package garlic_heist;

/**
 *
 * @author vdaviet
 */
public class Score {

    private int scoreTotal;                                                    // Stocke le score total du joueur

    public Score() {
        this.scoreTotal = 0;                                                   // Initialisation du score à 0 au début de la partie
    }

    // Getter pour récupérer la valeur actuelle du score
    public int getScoreTotal() {
        return scoreTotal;
    }

    // Ajoute 20 points à chaque fois qu'un objet est récupéré
    public void ajouterPointsObjet() {
        this.scoreTotal = this.scoreTotal + 20;                                // +20 points par objet
        System.out.println("+20 points ! Score actuel : " + this.scoreTotal);
    }

    // Fait perdre 5 points à chaque fois qu'une vie est perdue
    public void perdrePointsVie() {
        this.scoreTotal = this.scoreTotal - 5;                                 // -5 points par vie perdue
        if (this.scoreTotal < 0) {
            this.scoreTotal = 0;                                               // Empêche le score d'être négatif
        }
        System.out.println("-5 points ! Score actuel : " + this.scoreTotal);
    }

    // Fait gagner 50 points lorsque la méthode de cuisine du plat est validée
    public void fabriquerPlat() {
        this.scoreTotal = this.scoreTotal + 50;                                // +50 points pour le plat validé
        System.out.println("Plat validé ! +50 points ! Score actuel : " + this.scoreTotal);
    }
}