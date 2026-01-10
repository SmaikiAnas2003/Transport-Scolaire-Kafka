package com.example.TransportScolaire.consumer;

import com.example.TransportScolaire.config.AppConfig;
import com.example.TransportScolaire.repository.DatabaseManager;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class ParentApp {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, AppConfig.getKafkaServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, AppConfig.getGroupParents());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(AppConfig.getTopicBus()));

        System.out.println("🌐 PARENT APP : En écoute sur " + AppConfig.getTopicBus());

        // Setup dossier webapp
        String projectRoot = System.getProperty("user.dir");
        String outputFolderPath = projectRoot + File.separator + "webapp";
        File outputDir = new File(outputFolderPath);
        if (!outputDir.exists()) outputDir.mkdirs();

        // Initialisation fichier parents
        updateParentsFile(outputFolderPath);

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                String jsonBus = record.value();
                // Ecriture temps réel du bus
                writeJson(outputFolderPath + File.separator + "bus.json", jsonBus);

                // Rafraîchissement périodique des statuts parents
                updateParentsFile(outputFolderPath);
            }
        }
    }

    private static void writeJson(String fullPath, String content) {
        try (FileWriter writer = new FileWriter(fullPath)) {
            writer.write(content);
        } catch (IOException e) {
            System.err.println("❌ Erreur I/O : " + e.getMessage());
        }
    }

    private static void updateParentsFile(String folderPath) {
        String jsonParents = DatabaseManager.getParentsAsJson();
        writeJson(folderPath + File.separator + "parents.json", jsonParents);
    }
}