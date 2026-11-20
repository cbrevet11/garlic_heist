/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package garlic_heist;

/**
 *
 * @author vdaviet
 */
public class Objets {
    private double[] positionobj = new double[2];
    private double[][] nourritures = new double[3][2];

    public Objets() {
        this.positionobj = new double[2]; 
        this.nourritures[0] = new double[]{5.0, 5.0};   // 1er item
        this.nourritures[1] = new double[]{10.0, 2.0};  // 2ème item
        this.nourritures[2] = new double[]{8.0, 12.0};  // 3ème item
    }

    // Getter pour récupérer les positions de toute la nourriture ghh
    public double[][] getNourritures() {
        return nourritures;
    }
    public double[] getposition() {
        return this.positionobj;
    }
   
}