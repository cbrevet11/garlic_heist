/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package garlic_heist;

/**
 *
 * @author tbrenand
 */

import java.io.*;
import java.net.*;

public class Serveur {
    public static void main(String[] args) {
        int port = 12345;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Le serveur de jeu est démarré sur le port " + port);

            // Boucle pour accepter les connexions des joueurs en continu
            while (true) {
                Socket socketClient = serverSocket.accept();
                System.out.println("Nouveau joueur connecté : " + socketClient.getInetAddress());
                
                // Ici, vous pourriez lancer un thread par joueur pour gérer ses mouvements
            }
        } catch (IOException e) {
            System.out.println("Erreur sur le serveur : " + e.getMessage());
        }
    }
}
