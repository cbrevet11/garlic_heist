/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package garlic_heist;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Cuisine par joueur : 1 A + 1 B + 1 C => 1 plat en 10 secondes.
 * Toutes les methodes doivent etre appelees depuis le meme thread reseau.
 */
public class Cuisine {
    private static final long DUREE_MS = 10_000L;
    private final Connection db;
    private final int partieId;
    private final int joueurId;
    private long debut = -1;

    public Cuisine(Connection db, int partieId, int joueurId) {
        this.db = db;
        this.partieId = partieId;
        this.joueurId = joueurId;
    }

    // Appelee UNE fois apres un ramassage confirme dans objets_partie.
    public void ajouterIngredient(int objetId) throws SQLException {
        if (objetId < 1 || objetId > 3) return;
        String sql = "INSERT INTO ingredients_joueur (partie_id, joueur_id, objet_id, quantite) "
                + "VALUES (?, ?, ?, 1) ON DUPLICATE KEY UPDATE quantite = quantite + 1";
        try (PreparedStatement q = db.prepareStatement(sql)) {
            q.setInt(1, partieId);
            q.setInt(2, joueurId);
            q.setInt(3, objetId);
            q.executeUpdate();
        }
    }

    public int[] stock() throws SQLException {
        int[] stock = new int[3];
        try (PreparedStatement q = db.prepareStatement(
                "SELECT objet_id, quantite FROM ingredients_joueur "
                        + "WHERE partie_id=? AND joueur_id=?")) {
            q.setInt(1, partieId);
            q.setInt(2, joueurId);
            try (ResultSet r = q.executeQuery()) {
                while (r.next()) {
                    int id = r.getInt(1);
                    if (id >= 1 && id <= 3) stock[id - 1] = r.getInt(2);
                }
            }
        }
        return stock;
    }

    public int nombrePlats() throws SQLException {
        try (PreparedStatement q = db.prepareStatement(
                "SELECT quantite FROM plats_joueur WHERE partie_id=? AND joueur_id=?")) {
            q.setInt(1, partieId);
            q.setInt(2, joueurId);
            try (ResultSet r = q.executeQuery()) {
                return r.next() ? r.getInt(1) : 0;
            }
        }
    }

    public boolean commencer(boolean surTable) throws SQLException {
        if (!surTable || debut >= 0) return false;
        int[] s = stock();
        if (s[0] < 1 || s[1] < 1 || s[2] < 1) return false;
        debut = System.nanoTime();
        return true;
    }

    public boolean enCours() {
        return debut >= 0;
    }

    public long secondesRestantes() {
        if (debut < 0) return 0;
        long ecouleesMs = (System.nanoTime() - debut) / 1_000_000L;
        return Math.max(0, (DUREE_MS - ecouleesMs + 999) / 1000);
    }

    public void annuler() {
        debut = -1;
    }

    // Renvoye true uniquement lorsqu'un plat a bien ete fabrique.
    // La transaction garantit la consommation simultanee des 3 ingredients.
    public boolean actualiser(boolean surTable) throws SQLException {
        if (debut < 0) return false;
        if (!surTable) {
            annuler();
            return false;
        }
        if ((System.nanoTime() - debut) < DUREE_MS * 1_000_000L) return false;
        debut = -1;
        return fabriquerPlat();
    }

    private boolean fabriquerPlat() throws SQLException {
        boolean ancienAutoCommit = db.getAutoCommit();
        db.setAutoCommit(false);
        try {
            int[] quantites = new int[3];
            // Verrouiller les lignes des ingredients pendant la transaction.
            try (PreparedStatement q = db.prepareStatement(
                    "SELECT objet_id, quantite FROM ingredients_joueur "
                            + "WHERE partie_id=? AND joueur_id=? ORDER BY objet_id FOR UPDATE")) {
                q.setInt(1, partieId);
                q.setInt(2, joueurId);
                try (ResultSet r = q.executeQuery()) {
                    while (r.next()) {
                        int id = r.getInt(1);
                        if (id >= 1 && id <= 3) quantites[id - 1] = r.getInt(2);
                    }
                }
            }
            for (int qte : quantites) {
                if (qte < 1) {
                    db.rollback();
                    return false;
                }
            }
            try (PreparedStatement q = db.prepareStatement(
                    "UPDATE ingredients_joueur SET quantite=quantite-1 "
                            + "WHERE partie_id=? AND joueur_id=? AND objet_id=? AND quantite>0")) {
                for (int id = 1; id <= 3; id++) {
                    q.setInt(1, partieId);
                    q.setInt(2, joueurId);
                    q.setInt(3, id);
                    if (q.executeUpdate() != 1) throw new SQLException("Ingredient indisponible : " + id);
                }
            }
            try (PreparedStatement q = db.prepareStatement(
                    "INSERT INTO plats_joueur (partie_id,joueur_id,quantite) VALUES (?,?,1) "
                            + "ON DUPLICATE KEY UPDATE quantite=quantite+1")) {
                q.setInt(1, partieId);
                q.setInt(2, joueurId);
                q.executeUpdate();
            }
            db.commit();
            return true;
        } catch (SQLException ex) {
            db.rollback();
            throw ex;
        } finally {
            db.setAutoCommit(ancienAutoCommit);
        }
    }
}
