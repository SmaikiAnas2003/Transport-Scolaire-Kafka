package com.example.TransportScolaire.model;

import java.util.List;

/**
 * Cette classe sert de "valise" pour transporter les données
 * du Bus vers le Serveur (Position actuelle + Liste des élèves).
 */
public class RouteRequest {
    private double currentLat;
    private double currentLon;
    private List<ParentLocation> candidats;

    // Constructeur vide (nécessaire pour Spring/Jackson)
    public RouteRequest() {}

    // Constructeur complet (optionnel mais pratique)
    public RouteRequest(double currentLat, double currentLon, List<ParentLocation> candidats) {
        this.currentLat = currentLat;
        this.currentLon = currentLon;
        this.candidats = candidats;
    }

    // --- GETTERS ET SETTERS ---
    public double getCurrentLat() {
        return currentLat;
    }

    public void setCurrentLat(double currentLat) {
        this.currentLat = currentLat;
    }

    public double getCurrentLon() {
        return currentLon;
    }

    public void setCurrentLon(double currentLon) {
        this.currentLon = currentLon;
    }

    public List<ParentLocation> getCandidats() {
        return candidats;
    }

    public void setCandidats(List<ParentLocation> candidats) {
        this.candidats = candidats;
    }
}