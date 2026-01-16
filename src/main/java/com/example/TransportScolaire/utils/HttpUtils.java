package com.example.TransportScolaire.utils;

import com.example.TransportScolaire.model.ParentLocation;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Locale;

public class HttpUtils {
    private static final String BASE_URL = "http://localhost:8080/api";
    private static final HttpClient client = HttpClient.newHttpClient();

    public static double getDistance(double lat1, double lon1, double lat2, double lon2) {
        // ... (votre code existant getDistance) ...
        // Je le remets pour mémoire, mais gardez celui d'avant
        try {
            String url = String.format(Locale.US, "%s/distance/calculer?lat1=%f&lon1=%f&lat2=%f&lon2=%f", BASE_URL, lat1, lon1, lat2, lon2);
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            return Double.parseDouble(client.send(request, HttpResponse.BodyHandlers.ofString()).body());
        } catch (Exception e) { return 9999.0; }
    }

    public static void postPenalite(String nom, int temps) {
        // ... (votre code existant postPenalite) ...
        try {
            String url = String.format("%s/penalites/appliquer?nom=%s&tempsArret=%d", BASE_URL, nom.replace(" ", "%20"), temps);
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).POST(HttpRequest.BodyPublishers.noBody()).build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {}
    }

    // --- NOUVEAU : ENVOYER LA LISTE ET RECEVOIR LA DÉCISION ---
    public static ParentLocation demanderProchaineDestination(double lat, double lon, List<ParentLocation> candidats) {
        try {
            // 1. Construction manuelle du JSON (currentLat, currentLon, candidats)
            StringBuilder json = new StringBuilder();
            json.append(String.format(Locale.US, "{\"currentLat\":%f, \"currentLon\":%f, \"candidats\":[", lat, lon));

            for (int i = 0; i < candidats.size(); i++) {
                ParentLocation p = candidats.get(i);
                json.append(String.format(Locale.US, "{\"nom\":\"%s\", \"lat\":%f, \"lon\":%f}", p.nom, p.lat, p.lon));
                if (i < candidats.size() - 1) json.append(",");
            }
            json.append("]}");

            // 2. Envoi POST
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/distance/prochain"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String respBody = response.body();

            // 3. Parsing manuel de la réponse JSON (ex: {"nom":"Temlali", "lat":..., "lon":...})
            String nom = extractValue(respBody, "nom");
            double resLat = Double.parseDouble(extractValue(respBody, "lat"));
            double resLon = Double.parseDouble(extractValue(respBody, "lon"));

            return new ParentLocation(nom, resLat, resLon);

        } catch (Exception e) {
            System.err.println("❌ Erreur API Décision : " + e.getMessage());
            return candidats.get(0); // Fallback: on prend le premier si le serveur plante
        }
    }

    // Petit parser JSON utilitaire
    private static String extractValue(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start == -1) return "0";
        start += search.length();
        if (json.charAt(start) == '"') {
            start++;
            return json.substring(start, json.indexOf("\"", start));
        } else {
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            return json.substring(start, end).trim();
        }
    }
}