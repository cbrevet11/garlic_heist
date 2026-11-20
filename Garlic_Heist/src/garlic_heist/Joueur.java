/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package garlic_heist;


import java.io.*;
import java.net.*;

public class Joueur {
    private souris maSouris;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    public Joueur(String adresseIp, int port, String nomJoueur) {
        try {
            // Instanciation de la classe souris d'origine
            maSouris = new souris();
            
            // Connexion au serveur
            socket = new Socket(adresseIp, port);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            
            System.out.println("Connexion au serveur établie pour : " + nomJoueur);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    // 1. GESTION DES DÉPLACEMENTS
    // ==========================================
    public void deplacerHaut() {
        maSouris.deplacementhaut();
        envoyerAction("MOUVEMENT:HAUT");
    }

    public void deplacerBas() {
        maSouris.deplacementbas();
        envoyerAction("MOUVEMENT:BAS");
    }

    public void deplacerGauche() {
        maSouris.deplacementgauche();
        envoyerAction("MOUVEMENT:GAUCHE");
    }

    public void deplacerDroite() {
        maSouris.deplacementdroite();
        envoyerAction("MOUVEMENT:DROITE");
    }

    // ==========================================
    // 2. GESTION DE LA VIE ET DE LA MORT
    // ==========================================
    public void verifierEtMettreAJourVie(int pointsDeVie) {
        if (pointsDeVie <= 0) {
            maSouris.mort_souris();
            envoyerAction("ETAT:MORT");
        } else {
            envoyerAction("VIE:" + pointsDeVie);
        }
    }

    // ==========================================
    // 3. GESTION DES ATTRIBUTS (Position, Nourriture, Nom)
    // ==========================================
    public double[] getPosition() {
        return maSouris.position;
    }

    public double[] getNourriture() {
        return maSouris.nourriture;
    }

    public String getNom() {
        return maSouris.nom;
    }

    // ==========================================
    // MÉTHODE GÉNÉRIQUE RÉSEAU
    // ==========================================
    private void envoyerAction(String commande) {
        try {
            out.writeUTF(commande);
            out.flush();
        } catch (IOException e) {
            System.err.println("Erreur d'envoi réseau : " + e.getMessage());
        }
    }

    public void fermerConnexion() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}