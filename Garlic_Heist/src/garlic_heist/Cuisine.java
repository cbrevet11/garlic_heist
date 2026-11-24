package garlic_heist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Cuisine : utilise les ingredients definis dans Objets.
 * Un exemplaire de chaque nourriture est necessaire pour un plat (10 s sur la table).
 * Les stocks et les plats sont conserves dans MariaDB.
 */
public class Cuisine {

    private static final long DUREE_MS = 5_000L;

    private final Connection db;
    private final int partieId;
    private final int joueurId;
    private final Objets objets;
    private final int nombreIngredients;
    private long debut = -1L;

    public Cuisine(Connection db, int partieId, int joueurId) {
        if (db == null) {
            throw new IllegalArgumentException("Connexion MariaDB absente");
        }
        this.db = db;
        this.partieId = partieId;
        this.joueurId = joueurId;

        // La classe Objets est la reference pour le nombre d'ingredients.
        this.objets = new Objets();
        double[][] nourritures = objets.getNourritures();
        if (nourritures == null || nourritures.length == 0) {
            throw new IllegalStateException("Aucun ingredient defini dans Objets");
        }
        this.nombreIngredients = nourritures.length;
    }

    /** Identifiants SQL : objet 1 correspond a nourritures[0], etc. */
    public int getNombreIngredients() {
        return nombreIngredients;
    }

    /** A appeler uniquement apres un ramassage confirme par MariaDB. */
    public void ajouterIngredient(int objetId) throws SQLException {
        if (objetId < 1 || objetId > nombreIngredients) {
            throw new IllegalArgumentException("Objet inconnu dans Objets : " + objetId);
        }
        String sql = "INSERT INTO ingredients_joueur "
                + "(partie_id, joueur_id, objet_id, quantite) VALUES (?, ?, ?, 1) "
                + "ON DUPLICATE KEY UPDATE quantite = quantite + 1";
        try (PreparedStatement q = db.prepareStatement(sql)) {
            q.setInt(1, partieId);
            q.setInt(2, joueurId);
            q.setInt(3, objetId);
            q.executeUpdate();
        }
    }

    /** stock()[0] = objet 1 (A), stock()[1] = objet 2 (B), etc. */
    public int[] stock() throws SQLException {
        int[] resultat = new int[nombreIngredients];
        String sql = "SELECT objet_id, quantite FROM ingredients_joueur "
                + "WHERE partie_id=? AND joueur_id=?";
        try (PreparedStatement q = db.prepareStatement(sql)) {
            q.setInt(1, partieId);
            q.setInt(2, joueurId);
            try (ResultSet r = q.executeQuery()) {
                while (r.next()) {
                    int id = r.getInt("objet_id");
                    if (id >= 1 && id <= nombreIngredients) {
                        resultat[id - 1] = r.getInt("quantite");
                    }
                }
            }
        }
        return resultat;
    }

    public int nombrePlats() throws SQLException {
        String sql = "SELECT quantite FROM plats_joueur "
                + "WHERE partie_id=? AND joueur_id=?";
        try (PreparedStatement q = db.prepareStatement(sql)) {
            q.setInt(1, partieId);
            q.setInt(2, joueurId);
            try (ResultSet r = q.executeQuery()) {
                return r.next() ? r.getInt(1) : 0;
            }
        }
    }

    public boolean possedeTousLesIngredients() throws SQLException {
        for (int quantite : stock()) {
            if (quantite < 1) return false;
        }
        return true;
    }

    public boolean commencer(boolean surTable) throws SQLException {
        if (!surTable || enCours() || !possedeTousLesIngredients()) return false;
        debut = System.nanoTime();
        return true;
    }

    public boolean enCours() {
        return debut >= 0;
    }

    public long secondesRestantes() {
        if (!enCours()) return 0;
        long ecouleesMs = (System.nanoTime() - debut) / 1_000_000L;
        return Math.max(0, (DUREE_MS - ecouleesMs + 999L) / 1000L);
    }

    public void annuler() {
        debut = -1L;
    }

    /** Renvoye true seulement quand un plat a effectivement ete fabrique. */
    public boolean actualiser(boolean surTable) throws SQLException {
        if (!enCours()) return false;
        if (!surTable) {
            annuler();
            return false;
        }
        if (System.nanoTime() - debut < DUREE_MS * 1_000_000L) return false;
        debut = -1L;
        return fabriquerPlat();
    }

    private boolean fabriquerPlat() throws SQLException {
        boolean autoCommitInitial = db.getAutoCommit();
        db.setAutoCommit(false);
        try {
            // Bloquer les stocks du joueur pour empecher une double consommation.
            int[] quantites = new int[nombreIngredients];
            String lecture = "SELECT objet_id, quantite FROM ingredients_joueur "
                    + "WHERE partie_id=? AND joueur_id=? ORDER BY objet_id FOR UPDATE";
            try (PreparedStatement q = db.prepareStatement(lecture)) {
                q.setInt(1, partieId);
                q.setInt(2, joueurId);
                try (ResultSet r = q.executeQuery()) {
                    while (r.next()) {
                        int id = r.getInt("objet_id");
                        if (id >= 1 && id <= nombreIngredients) {
                            quantites[id - 1] = r.getInt("quantite");
                        }
                    }
                }
            }
            for (int quantite : quantites) {
                if (quantite < 1) {
                    db.rollback();
                    return false;
                }
            }

            String retirer = "UPDATE ingredients_joueur SET quantite = quantite - 1 "
                    + "WHERE partie_id=? AND joueur_id=? AND objet_id=? AND quantite>0";
            try (PreparedStatement q = db.prepareStatement(retirer)) {
                for (int id = 1; id <= nombreIngredients; id++) {
                    q.setInt(1, partieId);
                    q.setInt(2, joueurId);
                    q.setInt(3, id);
                    if (q.executeUpdate() != 1) {
                        throw new SQLException("Ingredient indisponible : " + id);
                    }
                }
            }

            String produire = "INSERT INTO plats_joueur (partie_id, joueur_id, quantite) "
                    + "VALUES (?, ?, 1) ON DUPLICATE KEY UPDATE quantite = quantite + 1";
            try (PreparedStatement q = db.prepareStatement(produire)) {
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
            db.setAutoCommit(autoCommitInitial);
        }
    }
}
