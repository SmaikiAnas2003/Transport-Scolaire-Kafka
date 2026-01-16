# 🚌 Système de Supervision de Transport Scolaire Distribué

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Leaflet](https://img.shields.io/badge/Leaflet-199900?style=for-the-badge&logo=Leaflet&logoColor=white)

**Projet académique réalisé à l'École Mohammadia d'Ingénieurs (EMI).**

Ce projet met en œuvre une architecture **distribuée et événementielle (Event-Driven)** pour la supervision en temps réel d'une flotte de transport scolaire. Contrairement aux solutions monolithiques, il utilise **Apache Kafka** pour assurer un découplage fort entre les composants et une résilience aux pannes.

---

## 🏗️ Architecture Technique

### 1. Décentralisation & "Thin Client"
L'architecture respecte strictement le principe de **Séparation des Responsabilités (SoC)** :
* **BusProducer (Client Léger) :** Simule le déplacement physique. Il ne possède *aucune intelligence métier*. Il se contente d'envoyer sa télémétrie et d'exécuter les ordres.
* **Backend Spring Boot (Cerveau Central) :** Centralise les algorithmes complexes (calcul de distance, optimisation de tournée).

### 2. Infrastructure Kafka (Dockerisé)
Le système repose sur un cluster Kafka déployé via Docker Compose (Zookeeper + Broker). La communication est structurée autour de deux topics distincts :

| Topic | Type | Producteur | Consommateur | Rôle |
| :--- | :--- | :--- | :--- | :--- |
| `positions-bus` | **Streaming** | BusProducer | Frontend / Monitoring | Flux continu de télémétrie GPS (Haute fréquence). |
| `penalites-bus` | **Événement** | EcoleMonitoring | PenaliteService | Gestion critique des infractions (Assure la persistance). |

---

## 🚀 Fonctionnalités Clés & Implémentation

### 🗺️ Routing Dynamique (Nearest Neighbor)
Le bus ne suit pas une liste statique. À chaque arrêt, il interroge l'API pour recalculer sa route en fonction :
1.  De sa position actuelle.
2.  De la liste des élèves restants (mise à jour en temps réel).
3.  L'algorithme du **Plus Proche Voisin** détermine la prochaine destination optimale.

### ⚡ Ajout de Passagers en Temps Réel
L'interface permet d'injecter de nouvelles données pendant la simulation.
* L'administrateur ajoute une famille sur la carte.
* Le Backend met à jour la BDD.
* Le Bus prend en compte ce nouvel arrêt **immédiatement** lors du prochain calcul de route.

### ⚖️ Gestion des Pénalités (Chaîne de Responsabilité)
1.  **Détection :** Le service de monitoring analyse le temps d'arrêt.
2.  **Streaming :** Si `Temps > 5s`, un événement est publié dans Kafka (`penalites-bus`).
3.  **Traitement :** Un consommateur dédié (`PenaliteService`) lit le message et applique l'amende en base de données de manière asynchrone.

---

## 📸 Scénarios de Démonstration

### 1. Prédiction et Routing Intelligent
Le système notifie l'opérateur de la prochaine destination choisie par l'algorithme serveur.
![Notification Destination](./frontend_destination.png)

### 2. Ajout Dynamique (Temps Réel)
Injection d'une nouvelle famille dans le système sans interruption de service.
![Ajout Dynamique](./ajout_dynamique.png)

### 3. Succès du Ramassage (Cas Nominal)
Validation visuelle lorsque l'élève monte dans les temps (< 5s).
![Succès](./frontend_succes.png)

### 4. Gestion des Infractions (Pénalité)
Alerte critique rouge générée par le flux Kafka lors d'un retard (> 5s).
![Retard](./frontend_retard.png)

---

## 🛠️ Installation et Démarrage

### Pré-requis
* Java 17+
* Docker & Docker Compose
* Maven

### 1. Lancer l'infrastructure Kafka
```bash
docker-compose up -d
