/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package garlic_heist;

/**
 *
 * @author ybelabed
 */
public class Brouille_code {
        public void creationObstacle() {
        cases[2][2] = '#';
        cases[3][3] = '#';
        cases[4][4] = '#';
    }

    public boolean isObstacle(int x, int y) {
        if (x < 0 || y < 0 || x >= nbColonnes || y >= nbLignes) {
            return true;   // en dehors de la carte = bloqué
        }
        return cases[y][x] == '#';
    }
}
