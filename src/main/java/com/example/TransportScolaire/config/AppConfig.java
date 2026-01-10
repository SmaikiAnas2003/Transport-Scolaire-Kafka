package com.example.TransportScolaire.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) System.err.println("❌ application.properties introuvable");
            else properties.load(input);
        } catch (IOException ex) { ex.printStackTrace(); }
    }

    public static String getDbUrl() { return properties.getProperty("db.url"); }
    public static String getDbUser() { return properties.getProperty("db.username"); }
    public static String getDbPassword() { return properties.getProperty("db.password"); }
    public static String getKafkaServers() { return properties.getProperty("kafka.bootstrap.servers"); }
    public static String getTopicBus() { return properties.getProperty("kafka.topic.bus"); }
    public static int getTopicPartitions() { return Integer.parseInt(properties.getProperty("kafka.topic.partitions", "3")); }
    public static short getTopicReplication() { return Short.parseShort(properties.getProperty("kafka.topic.replication", "3")); }
    public static String getGroupParents() { return properties.getProperty("kafka.group.parents"); }
    public static String getGroupEcole() { return properties.getProperty("kafka.group.ecole"); }
    public static double getSchoolLat() { return Double.parseDouble(properties.getProperty("school.lat")); }
    public static double getSchoolLon() { return Double.parseDouble(properties.getProperty("school.lon")); }
    public static int getSimSpeed() { return Integer.parseInt(properties.getProperty("sim.speed")); }
}