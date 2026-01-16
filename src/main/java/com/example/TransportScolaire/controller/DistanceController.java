package com.example.TransportScolaire.controller;

import com.example.TransportScolaire.model.ParentLocation;
import com.example.TransportScolaire.model.RouteRequest; // <--- L'IMPORT EST ICI
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/distance")
public class DistanceController {

    // Endpoint simple (pour les déplacements pas à pas)
    @GetMapping("/calculer")
    public double calculerDistance(@RequestParam double lat1, @RequestParam double lon1, @RequestParam double lat2, @RequestParam double lon2) {
        // Formule de distance (Pythagore simplifié ici pour l'exemple)
        return Math.sqrt(Math.pow(lat2 - lat1, 2) + Math.pow(lon2 - lon1, 2));
    }

    // --- LE CERVEAU DÉCISIONNEL ---
    @PostMapping("/prochain")
    public ParentLocation trouverProchainArret(@RequestBody RouteRequest request) {
        ParentLocation meilleur = null;
        double minDistance = Double.MAX_VALUE;

        // On vérifie que la liste n'est pas vide pour éviter un crash
        if (request.getCandidats() == null || request.getCandidats().isEmpty()) {
            return null;
        }

        // Algorithme du plus proche voisin
        for (ParentLocation candidat : request.getCandidats()) {
            double dist = Math.sqrt(Math.pow(candidat.lat - request.getCurrentLat(), 2) +
                    Math.pow(candidat.lon - request.getCurrentLon(), 2));

            if (dist < minDistance) {
                minDistance = dist;
                meilleur = candidat;
            }
        }
        return meilleur; // Le serveur renvoie l'objet ParentLocation gagnant
    }
}