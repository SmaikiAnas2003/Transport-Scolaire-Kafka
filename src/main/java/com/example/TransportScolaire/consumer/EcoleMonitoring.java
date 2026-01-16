package com.example.TransportScolaire.consumer;

import com.example.TransportScolaire.config.AppConfig;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class EcoleMonitoring {
    public static void main(String[] args) {
        // 1. Config du CONSUMER (Pour lire les positions)
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.getKafkaServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, AppConfig.getGroupEcole());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        // 2. Config du PRODUCER (Pour écrire les pénalités)
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.getKafkaServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps);

        consumer.subscribe(Collections.singletonList(AppConfig.getTopicBus()));

        System.out.println("👮 MONITORING : Je surveille les positions et je signale les infractions via Kafka.");
        String lastProcessedTarget = "";

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                String val = record.value();

                if (val.contains("\"status\":\"STOPPED\"")) {
                    try {
                        String target = extractValue(val, "target");
                        int counter = 0;
                        try {
                            String c = extractValue(val, "counter");
                            if(!c.isEmpty()) counter = Integer.parseInt(c);
                        } catch(Exception e) { counter = 6; }

                        // Si c'est un nouvel arrêt, on envoie un message dans le topic 'penalites-bus'
                        if (!target.equals(lastProcessedTarget) && !target.equals("ÉCOLE")) {
                            System.out.println("⚠️ INFRACTION DÉTECTÉE : " + target + " (" + counter + "s). Envoi au topic pénalités...");

                            // Création du message d'alerte
                            String jsonPenalite = "{\"nom\":\"" + target + "\", \"duree\":" + counter + "}";

                            // Écriture dans Kafka (Topic Pénalités)
                            producer.send(new ProducerRecord<>(AppConfig.getTopicPenalites(), target, jsonPenalite));

                            lastProcessedTarget = target;
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                } else if (val.contains("\"status\":\"MOVING\"")) {
                    lastProcessedTarget = "";
                }
            }
        }
    }

    private static String extractValue(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start == -1) return "";
        start += search.length();
        if (json.charAt(start) == '"') {
            start++;
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        } else {
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            return json.substring(start, end).trim();
        }
    }
}