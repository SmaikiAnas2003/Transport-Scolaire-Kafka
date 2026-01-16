package com.example.TransportScolaire.controller;

import com.example.TransportScolaire.repository.DatabaseManager;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/penalites")
public class PenaliteController {

    @PostMapping("/appliquer")
    public String appliquerPenalite(@RequestParam String nom, @RequestParam int tempsArret) {

        // --- LA RÈGLE D'OR ---
        if (tempsArret > 5) {
            // Si > 5s, on appelle la base de données pour AUGMENTER le compteur
            DatabaseManager.appliquerPenaliteParNom(nom);
            System.out.println("⚖️ JUGE : Coupable ! " + nom + " a mis " + tempsArret + "s (> 5s). Amende +1.");
            return "✅ Pénalité validée (+1)";
        } else {
            // Si <= 5s, on ne fait RIEN
            System.out.println("⚖️ JUGE : Innocent. " + nom + " a mis " + tempsArret + "s (Tolérance).");
            return "ℹ️ Pas de pénalité (Dans les temps)";
        }
    }
}