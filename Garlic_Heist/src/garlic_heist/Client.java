/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package garlic_heist;

/**
 *
 * @author tbrenand
 */
import java.net.Socket;
import java.io.IOException;

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("192.168.1.25", 12345);

            System.out.println("Connecté au serveur !");

        } catch (IOException e) {
            System.out.println("Connexion impossible : " + e.getMessage());
        }
    }
}
