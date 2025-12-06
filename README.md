# Pay My Buddy – Projet Java/Spring

Application web « Pay My Buddy » permettant :
- d’ajouter des relations (amis),
- de leur envoyer de l’argent,
- de consulter l’historique des transactions,
- de gérer un profil utilisateur.

---

## 1. Stack technique

- Java 17
- Spring Boot (Web, Thymeleaf)
- Spring Security
- Spring Data JPA / Hibernate
- Flyway (migrations SQL)
- MySQL 8
- Maven

---

## 2. Prérequis

- JDK 17 installé
- MySQL en local
- Maven installé
- Une base de données créée (ex. `paymybuddy`)

Configurer la connexion dans `src/main/resources/application.properties`
(en utilisant les variables d’environnement de type `DB_USER`, `DB_PASSWORD`, etc. selon ton fichier).

---

## 3. Lancer l’application

```bash
# Cloner le projet
git clone https://github.com/FabienP133/papaiconomou-fabien-paymybuddy-Java.git
cd papaiconomou-fabien-paymybuddy-Java

# Lancer Spring Boot
mvn spring-boot:run
```

---

## 4. Modèle physique de données

TABLE users
-----------
- id : BIGINT PK
- email : VARCHAR(255) UNIQUE NOT NULL
- password_hash: VARCHAR(255) NOT NULL
- first_name : VARCHAR(100)
- last_name : VARCHAR(100)
- balance : DECIMAL(19,2) NOT NULL
- created_at : TIMESTAMP
- updated_at : TIMESTAMP

TABLE transactions
------------------
- id : BIGINT PK
- sender_id : BIGINT FK -> users.id
- receiver_id : BIGINT FK -> users.id
- amount : DECIMAL(19,2) NOT NULL
- fee : DECIMAL(19,2) NOT NULL
- description : VARCHAR(255)
- created_at : TIMESTAMP

TABLE user_connections
----------------------
- owner_id : BIGINT FK -> users.id
- friend_id : BIGINT FK -> users.id
- PRIMARY KEY : (owner_id, friend_id)
-- (table de relation many-to-many entre utilisateurs)