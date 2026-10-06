package garlic_heist;

public class Plateau {
    private char[][] cases;
    private int nbLignes;
    private int nbColonnes;

    public Plateau(int nbLignes, int nbColonnes) {
        this.nbLignes = nbLignes;
        this.nbColonnes = nbColonnes;
        cases = new char[nbLignes][nbColonnes];

        for (int y = 0; y < nbLignes; y++) {
            for (int x = 0; x < nbColonnes; x++) {
                cases[i][j] = '.';
            }
        }
    }

    public void affichage() {
        String bordure = "+" + "-".repeat(nbColonnes) + "+";
        System.out.println(bordure);

        for (int i = 0; i < nbLignes; i++) {
            System.out.print("|");
            for (int j = 0; j < nbColonnes; j++) {
                System.out.print(cases[i][j]);
            }
            System.out.println("|");
        }

        System.out.println(bordure);
    }

    public void creationObstacle() {
        cases[2][2] = '#';
        cases[3][3] = '#';
        cases[4][4] = '#';
    }

    public boolean isObstacle(int x, int y) {
        return cases[y][x] == '#';
    }
}