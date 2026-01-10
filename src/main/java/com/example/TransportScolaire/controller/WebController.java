package com.example.TransportScolaire.controller;

import com.example.TransportScolaire.repository.DatabaseManager;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Autorise HTML local
public class WebController {

    @PostMapping("/parents")
    public String ajouterParent(@RequestBody Map<String, String> payload) {
        String nom = payload.get("nom");
        try {
            double lat = Double.parseDouble(payload.get("lat"));
            double lon = Double.parseDouble(payload.get("lon"));

            System.out.println("🌍 API : Ajout demandé -> " + nom);
            DatabaseManager.ajouterParent(nom, lat, lon);

            return "{\"status\": \"success\"}";
        } catch (Exception e) {
            return "{\"status\": \"error\"}";
        }
    }
}