package com.example.TransportScolaire.producer;

import com.example.TransportScolaire.config.AppConfig;
import com.example.TransportScolaire.model.ParentLocation;
import com.example.TransportScolaire.repository.DatabaseManager;
import com.example.TransportScolaire.utils.HttpUtils;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.ArrayList;
import java.util.HashSet; // <--- AJOUT IMPORTANT
import java.util.List;
import java.util.Properties;
import java.util.Set;       // <--- AJOUT IMPORTANT
import java.util.Locale;
import java.util.Random;

public class BusProducer {
    // --- CONFIGURATION ---
    private static final double ECOLE_LAT = AppConfig.getSchoolLat();
    private static final double ECOLE_LON = AppConfig.getSchoolLon();
    private static final int SLEEP_TIME = AppConfig.getSimSpeed();
    private static final double STEP_SIZE = 0.00014;

    public static void main(String[] args) throws InterruptedException {
        // Config Kafka (Identique)
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.getKafkaServers());
        props.put("acks", "1");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, "1000");
        props.put(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, "1000");

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        String topic = AppConfig.getTopicBus();
        String busId = "BUS-01";
        Random random = new Random();

        double currentLat = ECOLE_LAT;
        double currentLon = ECOLE_LON;

        System.out.println("🏫 DÉPART DU DÉPÔT.");
        System.out.println("⚡ Mode DYNAMIQUE : Le bus détecte les ajouts en temps réel.");

        while (true) {
            // A. DÉBUT DE TOURNÉE
            DatabaseManager.resetStatuts();

            // Au lieu d'une liste à visiter, on garde une mémoire de qui on a DÉJÀ VU.
            Set<String> visitedParents = new HashSet<>();

            // On fait une première vérif pour voir s'il y a des élèves
            if (DatabaseManager.getParentsLocations().isEmpty()) {
                System.out.println("😴 Aucun élève inscrit. Attente...");
                Thread.sleep(5000);
                continue;
            }

            // B. BOUCLE DE RAMASSAGE
            while (true) {
                // 1. MISE A JOUR TEMPS RÉEL
                // On recharge la liste complète depuis la BDD à chaque arrêt !
                List<ParentLocation> allParentsInDb = DatabaseManager.getParentsLocations();

                // On filtre : On ne garde que ceux qu'on n'a PAS ENCORE visités
                List<ParentLocation> toVisit = new ArrayList<>();
                for (ParentLocation p : allParentsInDb) {
                    if (!visitedParents.contains(p.nom)) {
                        toVisit.add(p);
                    }
                }

                // S'il n'y a plus personne à visiter, la tournée est finie
                if (toVisit.isEmpty()) {
                    break;
                }

                int remainingStudents = toVisit.size();

                // 2. DÉCISION (Sur la base de la liste mise à jour)
                ParentLocation nextStop = HttpUtils.demanderProchaineDestination(currentLat, currentLon, toVisit);

                // !!! IMPORTANT !!! On ajoute immédiatement ce nom à la liste des visités
                visitedParents.add(nextStop.nom);

                System.out.println("\n---------------------------------------------------------------");
                System.out.println("🔔 CAP SUR : " + nextStop.nom + " (Liste mise à jour, restants: " + remainingStudents + ")");
                System.out.println("---------------------------------------------------------------\n");

                // 3. DÉPLACEMENT
                double dist = HttpUtils.getDistance(currentLat, currentLon, nextStop.lat, nextStop.lon);

                while (dist > 0.0002) {
                    if (currentLat < nextStop.lat) currentLat += 0.0001; else currentLat -= 0.0001;
                    if (currentLon < nextStop.lon) currentLon += 0.0001; else currentLon -= 0.0001;

                    dist = HttpUtils.getDistance(currentLat, currentLon, nextStop.lat, nextStop.lon);
                    int etaSeconds = (int) ((dist / STEP_SIZE) * (SLEEP_TIME / 1000.0));

                    envoyerInfo(producer, topic, busId, currentLat, currentLon, "MOVING", nextStop.nom, remainingStudents, 0, false, etaSeconds);
                    Thread.sleep(SLEEP_TIME);
                }

                // 4. ARRÊT
                System.out.println("🛑 ARRÊT chez " + nextStop.nom);
                currentLat = nextStop.lat;
                currentLon = nextStop.lon;

                int tempsMontee = random.nextInt(10) + 1;
                boolean potentialPenalty = tempsMontee > 5;

                for (int i = 0; i < 5; i++) {
                    envoyerInfo(producer, topic, busId, currentLat, currentLon, "STOPPED", nextStop.nom, remainingStudents, tempsMontee, potentialPenalty, 0);
                    Thread.sleep(1000);
                }
                DatabaseManager.updateStatut(nextStop.nom, "MONTE");
            }

            // C. RETOUR
            System.out.println("🔄 Tournée terminée. Retour au dépôt.");
            double distRetour = HttpUtils.getDistance(currentLat, currentLon, ECOLE_LAT, ECOLE_LON);
            while (distRetour > 0.0002) {
                if (currentLat < ECOLE_LAT) currentLat += 0.0001; else currentLat -= 0.0001;
                if (currentLon < ECOLE_LON) currentLon += 0.0001; else currentLon -= 0.0001;
                distRetour = HttpUtils.getDistance(currentLat, currentLon, ECOLE_LAT, ECOLE_LON);
                int etaSeconds = (int) ((distRetour / STEP_SIZE) * (SLEEP_TIME / 1000.0));
                envoyerInfo(producer, topic, busId, currentLat, currentLon, "RETURNING", "ÉCOLE", 0, 0, false, etaSeconds);
                Thread.sleep(300);
            }
            currentLat = ECOLE_LAT; currentLon = ECOLE_LON;
            for(int k=0; k<5; k++) { envoyerInfo(producer, topic, busId, currentLat, currentLon, "PARKED", "ÉCOLE", 0, 0, false, 0); Thread.sleep(1000); }
        }
    }

    private static void envoyerInfo(KafkaProducer<String, String> producer, String topic, String id, double lat, double lon, String status, String target, int remaining, int counter, boolean penalty, int eta) {
        String json = String.format(Locale.US,
                "{\"id\":\"%s\", \"lat\":%.4f, \"lon\":%.4f, \"status\":\"%s\", \"target\":\"%s\", \"remaining\":%d, \"counter\":%d, \"penalty\":%b, \"eta\":%d}",
                id, lat, lon, status, target, remaining, counter, penalty, eta);
        producer.send(new ProducerRecord<>(topic, id, json));
    }
}