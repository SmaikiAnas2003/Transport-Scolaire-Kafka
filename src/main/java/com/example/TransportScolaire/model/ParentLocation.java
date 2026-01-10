package com.example.TransportScolaire.model;

public class ParentLocation {
    public String nom;
    public double lat;
    public double lon;
    public String statut; // "EN_ATTENTE" ou "MONTE"

    public ParentLocation(String nom, double lat, double lon) {
        this.nom = nom;
        this.lat = lat;
        this.lon = lon;
        this.statut = "EN_ATTENTE";
    }
}