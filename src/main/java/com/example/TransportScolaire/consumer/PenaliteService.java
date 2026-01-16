package com.example.TransportScolaire.consumer;

import com.example.TransportScolaire.config.AppConfig;
import com.example.TransportScolaire.utils.HttpUtils;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class PenaliteService {

    public static void main(String[] args) {
        // --- CONFIGURATION DU CONSUMER ---
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.getKafkaServers());
        // Important : un GroupID unique pour ce service
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "service-traitement-penalites");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, "1000");
        props.put(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, "1000");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        // On s'abonne UNIQUEMENT au topic des pénalités
        consumer.subscribe(Collections.singletonList(AppConfig.getTopicPenalites()));

        System.out.println("⚖️ PENALITE SERVICE : Prêt à traiter les dossiers Kafka.");

        // --- BOUCLE DE TRAITEMENT ---
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

            for (ConsumerRecord<String, String> record : records) {
                String json = record.value();
                System.out.println("📥 Dossier reçu de Kafka : " + json);

                try {
                    // 1. Extraction des données du message Kafka
                    String nom = extractValue(json, "nom");
                    String dureeStr = extractValue(json, "duree");
                    int duree = Integer.parseInt(dureeStr);

                    // 2. APPEL DU JUGE (API)
                    // C'est ici qu'on demande au Controller de vérifier et d'appliquer
                    System.out.println("📞 Appel API pour " + nom + " (" + duree + "s)...");
                    HttpUtils.postPenalite(nom, duree);

                } catch (Exception e) {
                    System.err.println("❌ Erreur traitement dossier : " + e.getMessage());
                }
            }
        }
    }

    // Petit utilitaire JSON local (pour éviter les dépendances lourdes)
    private static String extractValue(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start == -1) return "0"; // Sécurité

        start += search.length();

        // Cas chaîne de caractères (ex: "nom":"Temlali")
        if (json.charAt(start) == '"') {
            start++;
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        }
        // Cas nombre (ex: "duree":8)
        else {
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            return json.substring(start, end).trim();
        }
    }
}