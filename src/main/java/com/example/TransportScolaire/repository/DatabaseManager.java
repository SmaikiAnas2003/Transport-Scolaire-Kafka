package com.example.TransportScolaire.repository;

import com.example.TransportScolaire.config.AppConfig;
import com.example.TransportScolaire.model.ParentLocation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DatabaseManager {

    // --- CONNEXION ---
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(AppConfig.getDbUrl(), AppConfig.getDbUser(), AppConfig.getDbPassword());
    }

    // --- 1. AJOUTER UN PARENT (API & ADMIN) ---
    public static void ajouterParent(String nom, double lat, double lon) {
        String sql = "INSERT INTO parents (nom, latitude, longitude, penalites, statut) VALUES (?, ?, ?, 0, 'EN_ATTENTE')";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nom);
            pstmt.setDouble(2, lat);
            pstmt.setDouble(3, lon);
            pstmt.executeUpdate();
            System.out.println("✅ BDD : Parent ajouté -> " + nom);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- 2. RECUPERER LES POSITIONS (POUR LE BUS) ---
    public static List<ParentLocation> getParentsLocations() {
        List<ParentLocation> list = new ArrayList<>();
        String sql = "SELECT nom, latitude, longitude FROM parents";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new ParentLocation(
                        rs.getString("nom"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // --- 3. METTRE A JOUR LE STATUT (QUAND LE BUS PASSE) ---
    public static void updateStatut(String nom, String statut) {
        String sql = "UPDATE parents SET statut = ? WHERE nom = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, statut);
            pstmt.setString(2, nom);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- 4. APPLIQUER UNE PENALITE (SI RETARD) ---
    public static void appliquerPenaliteParNom(String nom) {
        String sql = "UPDATE parents SET penalites = penalites + 1 WHERE nom = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nom);
            pstmt.executeUpdate();
            System.out.println("⚖️ BDD : Pénalité appliquée à " + nom);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- 5. EXPORT JSON COMPLET (POUR LE FRONTEND) ---
    public static String getParentsAsJson() {
        StringBuilder json = new StringBuilder("[");
        // On récupère TOUT : nom, pos, statut ET penalites
        String sql = "SELECT nom, latitude, longitude, statut, penalites FROM parents";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");

                String st = rs.getString("statut");
                if (st == null) st = "EN_ATTENTE"; // Protection anti-null

                // Formatage JSON précis
                json.append(String.format(Locale.US,
                        "{\"nom\":\"%s\", \"lat\":%.4f, \"lon\":%.4f, \"statut\":\"%s\", \"penalites\":%d}",
                        rs.getString("nom"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        st,
                        rs.getInt("penalites")
                ));
                first = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        json.append("]");
        return json.toString();
    }

    // --- 6. RESET DEBUT DE JOURNEE (POUR LA SIMULATION) ---
    public static void resetStatuts() {
        // On remet seulement les statuts à zéro, on garde les pénalités (historique)
        String sql = "UPDATE parents SET statut = 'EN_ATTENTE'";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("🔄 BDD : Statuts réinitialisés pour nouvelle tournée.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- 7. AFFICHAGE CONSOLE (POUR ADMIN APP) ---
    public static void afficherTousLesParents() {
        String sql = "SELECT * FROM parents";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\n--- 📋 LISTE DES FAMILLES ---");
            while (rs.next()) {
                System.out.printf(Locale.US, "ID %d | %-15s | Statut: %-10s | Amendes: %d%n",
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("statut"),
                        rs.getInt("penalites"));
            }
            System.out.println("-----------------------------");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}