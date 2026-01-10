package com.example.TransportScolaire.consumer;

import com.example.TransportScolaire.repository.DatabaseManager;
import java.util.Scanner;
import java.util.Locale;

public class AdminApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        // Force le scanner à utiliser le point pour les décimales (ex: 34.001)
        scanner.useLocale(Locale.US);

        System.out.println("=========================================");
        System.out.println("   🚌 CONSOLE ADMIN - TRANSPORT SCOLAIRE");
        System.out.println("=========================================");

        while (true) {
            System.out.println("\n--- MENU ---");
            System.out.println("1. Ajouter une famille");
            System.out.println("2. Voir la liste (avec pénalités)");
            System.out.println("3. Quitter");
            System.out.print("👉 Votre choix : ");

            String choix = scanner.next(); // Lit le choix
            scanner.nextLine(); // Consomme le retour à la ligne restant

            switch (choix) {
                case "1":
                    System.out.println("\n--- NOUVELLE FAMILLE ---");
                    System.out.print("Nom de la famille (ex: Famille_Dupont) : ");
                    String nom = scanner.nextLine();

                    System.out.print("Latitude (ex: 34.0015) : ");
                    double lat = 0.0;
                    try {
                        lat = scanner.nextDouble();
                    } catch (Exception e) {
                        System.out.println("❌ Erreur : Entrez un chiffre avec une virgule ou un point.");
                        scanner.nextLine(); // Nettoyer
                        continue;
                    }

                    System.out.print("Longitude (ex: -6.7950) : ");
                    double lon = 0.0;
                    try {
                        lon = scanner.nextDouble();
                    } catch (Exception e) {
                        System.out.println("❌ Erreur de format.");
                        scanner.nextLine();
                        continue;
                    }

                    // Appel à la base de données
                    DatabaseManager.ajouterParent(nom, lat, lon);
                    System.out.println("✅ Famille ajoutée avec succès !");
                    break;

                case "2":
                    // Appel de la méthode qui affiche la liste
                    DatabaseManager.afficherTousLesParents();
                    break;

                case "3":
                    System.out.println("👋 Au revoir !");
                    scanner.close();
                    System.exit(0);
                    break;

                default:
                    System.out.println("❌ Choix invalide.");
            }
        }
    }
}