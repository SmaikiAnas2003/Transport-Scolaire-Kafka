package com.example.TransportScolaire.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class KafkaTopicInit {
    public static void createTopic() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.getKafkaServers());

        try (AdminClient admin = AdminClient.create(props)) {
            String topicName = AppConfig.getTopicBus();
            boolean exists = admin.listTopics().names().get().contains(topicName);

            if (!exists) {
                System.out.println("⚙️ Initialisation Architecture Kafka...");
                NewTopic newTopic = new NewTopic(topicName, AppConfig.getTopicPartitions(), AppConfig.getTopicReplication());
                admin.createTopics(Collections.singleton(newTopic)).all().get();
                System.out.println("✅ Topic '" + topicName + "' créé (Partitions: " + newTopic.numPartitions() + ", Réplicas: " + newTopic.replicationFactor() + ")");
            } else {
                System.out.println("ℹ️ Topic Kafka déjà opérationnel.");
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erreur Kafka : Vérifiez que Docker tourne !");
        }
    }
}