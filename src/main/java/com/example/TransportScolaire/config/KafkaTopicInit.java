package com.example.TransportScolaire.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.errors.TopicExistsException;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class KafkaTopicInit {

    public static void createTopic() {
        Properties props = new Properties();
        // Connexion au broker (défini dans AppConfig)
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.getKafkaServers());
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "5000"); // 5s timeout
        props.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, "5000");

        System.out.println("🔧 Initialisation Kafka sur : " + AppConfig.getKafkaServers());

        int maxRetries = 5;
        for (int i = 0; i < maxRetries; i++) {
            try (AdminClient admin = AdminClient.create(props)) {

                // 1. On récupère la liste des topics qui existent déjà dans Kafka
                Set<String> existingTopics = admin.listTopics().names().get();
                List<NewTopic> newTopics = new ArrayList<>();

                // 2. Vérification du Topic BUS (Positions)
                String topicBus = AppConfig.getTopicBus();
                if (!existingTopics.contains(topicBus)) {
                    // (Nom, Partitions, Réplication) -> Réplication à 1 car 1 seul broker actif
                    newTopics.add(new NewTopic(topicBus, 1, (short) 1));
                    System.out.println("⚙️ Préparation création topic : " + topicBus);
                }

                // 3. Vérification du Topic PÉNALITÉS (Nouveau)
                String topicPenalites = AppConfig.getTopicPenalites();
                if (!existingTopics.contains(topicPenalites)) {
                    newTopics.add(new NewTopic(topicPenalites, 1, (short) 1));
                    System.out.println("⚙️ Préparation création topic : " + topicPenalites);
                }

                // 4. Création effective si nécessaire
                if (!newTopics.isEmpty()) {
                    try {
                        admin.createTopics(newTopics).all().get();
                        System.out.println("✅ Topics Kafka créés avec succès !");
                    } catch (ExecutionException e) {
                        if (e.getCause() instanceof TopicExistsException) {
                            System.out.println("ℹ️ Un des topics a été créé par un autre processus.");
                        } else {
                            throw e;
                        }
                    }
                } else {
                    System.out.println("ℹ️ Tous les topics Kafka (Positions & Pénalités) sont déjà opérationnels.");
                }

                return; // Sortie succès

            } catch (Exception e) {
                System.err.println("⚠️ Echec connexion Kafka (Tentative " + (i + 1) + "/" + maxRetries + ") : " + e.getMessage());
                try {
                    Thread.sleep(2000); // Attendre 2s avant de réessayer
                } catch (InterruptedException ignored) {}
            }
        }
        System.err.println("❌ ABANDON : Impossible de contacter Kafka après 5 tentatives.");
    }
}