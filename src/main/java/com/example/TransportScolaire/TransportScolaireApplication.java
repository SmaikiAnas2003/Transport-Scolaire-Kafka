package com.example.TransportScolaire;

import com.example.TransportScolaire.config.KafkaTopicInit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TransportScolaireApplication {

	public static void main(String[] args) {
		// 1. Initialiser Kafka
		KafkaTopicInit.createTopic();

		// 2. Lancer le serveur Web (pour l'API et le Controller)
		SpringApplication.run(TransportScolaireApplication.class, args);

		System.out.println("🚀 SYSTEME PRET : http://localhost:8080");
	}
}