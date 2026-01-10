package com.example.TransportScolaire.producer;

import com.example.TransportScolaire.config.AppConfig;
import com.example.TransportScolaire.model.ParentLocation;
import com.example.TransportScolaire.repository.DatabaseManager;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import java.util.List;
import java.util.Properties;
import java.util.Locale;
import java.util.Random;

public class BusProducer {
    // Récupération Config
    private static final double ECOLE_LAT = AppConfig.getSchoolLat();
    private static final double ECOLE_LON = AppConfig.getSchoolLon();
    private static final int SLEEP_TIME = AppConfig.getSimSpeed();
    private static final double STEP_SIZE = 0.00014;

    public static void main(String[] args) throws InterruptedException {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.getKafkaServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        String topic = AppConfig.getTopicBus();
        String busId = "BUS-01";
        Random random = new Random();

        double currentLat = ECOLE_LAT;
        double currentLon = ECOLE_LON;

        System.out.println("🏫 DÉPART DU DÉPÔT.");

        while (true) {
            DatabaseManager.resetStatuts();
            List<ParentLocation> arrets = DatabaseManager.getParentsLocations();
            int remainingStudents = arrets.size();

            if (remainingStudents == 0) {
                System.out.println("😴 Attente d'élèves...");
                Thread.sleep(5000);
                continue;
            }

            // 1. ALLER
            for (ParentLocation arret : arrets) {
                System.out.println("🚀 Vers : " + arret.nom);
                double dist = calculateDistance(currentLat, currentLon, arret.lat, arret.lon);

                // Déplacement
                while (dist > 0.0002) {
                    if (currentLat < arret.lat) currentLat += 0.0001; else currentLat -= 0.0001;
                    if (currentLon < arret.lon) currentLon += 0.0001; else currentLon -= 0.0001;

                    dist = calculateDistance(currentLat, currentLon, arret.lat, arret.lon);
                    int etaSeconds = (int) ((dist / STEP_SIZE) * (SLEEP_TIME / 1000.0));

                    // Info: status, target, remaining, counter=0, penalty=false, eta
                    envoyerInfo(producer, topic, busId, currentLat, currentLon, "MOVING", arret.nom, remainingStudents, 0, false, etaSeconds);
                    Thread.sleep(SLEEP_TIME);
                }

                // Arrêt
                System.out.println("🛑 ARRÊT chez " + arret.nom);
                currentLat = arret.lat;
                currentLon = arret.lon;

                int tempsMontee = random.nextInt(10) + 1;
                boolean hasPenalty = tempsMontee > 5;

                for (int i = 0; i < 5; i++) {
                    // Info: status, target, remaining, counter=tempsMontee, penalty=hasPenalty, eta=0
                    envoyerInfo(producer, topic, busId, currentLat, currentLon, "STOPPED", arret.nom, remainingStudents, tempsMontee, hasPenalty, 0);
                    Thread.sleep(1000);
                }

                DatabaseManager.updateStatut(arret.nom, "MONTE");
                remainingStudents--;
            }

            // 2. RETOUR
            System.out.println("🔄 Retour École.");
            while (calculateDistance(currentLat, currentLon, ECOLE_LAT, ECOLE_LON) > 0.0002) {
                if (currentLat < ECOLE_LAT) currentLat += 0.0001; else currentLat -= 0.0001;
                if (currentLon < ECOLE_LON) currentLon += 0.0001; else currentLon -= 0.0001;

                double dist = calculateDistance(currentLat, currentLon, ECOLE_LAT, ECOLE_LON);
                int etaSeconds = (int) ((dist / STEP_SIZE) * (SLEEP_TIME / 1000.0));

                // Info: counter=0, penalty=false
                envoyerInfo(producer, topic, busId, currentLat, currentLon, "RETURNING", "ÉCOLE", 0, 0, false, etaSeconds);
                Thread.sleep(300);
            }

            currentLat = ECOLE_LAT;
            currentLon = ECOLE_LON;
            for(int k=0; k<5; k++) {
                // Info: counter=0, penalty=false, eta=0
                envoyerInfo(producer, topic, busId, currentLat, currentLon, "PARKED", "ÉCOLE", 0, 0, false, 0);
                Thread.sleep(1000);
            }
        }
    }

    // Méthode corrigée avec tous les arguments
    private static void envoyerInfo(KafkaProducer<String, String> producer, String topic, String id, double lat, double lon, String status, String target, int remaining, int counter, boolean penalty, int eta) {
        String json = String.format(Locale.US,
                "{\"id\":\"%s\", \"lat\":%.4f, \"lon\":%.4f, \"status\":\"%s\", \"target\":\"%s\", \"remaining\":%d, \"counter\":%d, \"penalty\":%b, \"eta\":%d}",
                id, lat, lon, status, target, remaining, counter, penalty, eta);
        producer.send(new ProducerRecord<>(topic, id, json));
    }

    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        return Math.sqrt(Math.pow(lat2 - lat1, 2) + Math.pow(lon2 - lon1, 2));
    }
}