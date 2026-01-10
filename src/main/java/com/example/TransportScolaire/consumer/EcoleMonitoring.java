package com.example.TransportScolaire.consumer;

import com.example.TransportScolaire.config.AppConfig;
import com.example.TransportScolaire.repository.DatabaseManager;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class EcoleMonitoring {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.getKafkaServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, AppConfig.getGroupEcole());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(AppConfig.getTopicBus()));

        System.out.println("🏫 MONITORING : En écoute des pénalités.");
        String lastProcessedTarget = "";

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                String val = record.value();

                // Si arrêt et pénalité (compteur > 5)
                if (val.contains("\"status\":\"STOPPED\"") && val.contains("\"penalty\":true")) {
                    try {
                        String target = extractValue(val, "target");
                        if (!target.equals(lastProcessedTarget) && !target.equals("ÉCOLE")) {
                            System.out.println("⚠️ RETARD DÉTECTÉ : Pénalité pour " + target);
                            DatabaseManager.appliquerPenaliteParNom(target);
                            lastProcessedTarget = target;
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                }
                else if (val.contains("\"status\":\"MOVING\"")) {
                    lastProcessedTarget = "";
                }
            }
        }
    }

    private static String extractValue(String json, String key) {
        int start = json.indexOf("\"" + key + "\":\"");
        if (start == -1) return "";
        start += key.length() + 4;
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
}