package com.example.TransportScolaire.config;

import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            } else {
                System.err.println("⚠️ Impossible de trouver application.properties");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- KAFKA ---
    public static String getKafkaServers() {
        return properties.getProperty("kafka.bootstrap.servers", "localhost:9092");
    }

    public static String getTopicBus() {
        return properties.getProperty("kafka.topic.bus", "positions-bus");
    }

    // MODIFICATION ICI : On lit la propriété, sinon valeur par défaut
    public static String getTopicPenalites() {
        return properties.getProperty("kafka.topic.penalites", "penalites-bus");
    }

    public static String getGroupParents() {
        return properties.getProperty("kafka.group.parents", "groupe-parents");
    }

    public static String getGroupEcole() {
        return properties.getProperty("kafka.group.ecole", "groupe-ecole");
    }

    public static int getTopicPartitions() {
        return Integer.parseInt(properties.getProperty("kafka.topic.partitions", "1"));
    }

    public static short getTopicReplication() {
        return Short.parseShort(properties.getProperty("kafka.topic.replication", "1"));
    }

    // --- DATABASE ---
    public static String getDbUrl() {
        return properties.getProperty("db.url");
    }
    public static String getDbUser() {
        return properties.getProperty("db.username");
    }
    public static String getDbPassword() {
        return properties.getProperty("db.password");
    }

    // --- SIMULATION ---
    public static double getSchoolLat() {
        return Double.parseDouble(properties.getProperty("school.lat", "34.0000"));
    }
    public static double getSchoolLon() {
        return Double.parseDouble(properties.getProperty("school.lon", "-6.8000"));
    }
    public static int getSimSpeed() {
        return Integer.parseInt(properties.getProperty("sim.speed", "1000"));
    }
}