package com.pod.back;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		// 1. Charge le fichier .env
		Dotenv dotenv = Dotenv.configure()
				.directory("./")
				.ignoreIfMissing()
				.load();

		// 2. Injecte les variables dans les propriétés système de Java
		dotenv.entries().forEach(entry -> {
			System.setProperty(entry.getKey(), entry.getValue());
			System.out.println("Variable chargée depuis .env : " + entry.getKey() + " = " + entry.getValue());
		});

		// 3. Demarre Spring Boot
		SpringApplication.run(Application.class, args);
	}
}

/*
Voici le récapitulatif étape par étape pour mettre en place l'inscription, la vérification d'email et l'authentification sécurisée :

---

### Étape 1 : Modèle de données & Dépendances

* **Dépendances `pom.xml` :**
* `spring-boot-starter-security`
* `spring-boot-starter-validation`
* `spring-boot-starter-mail`
* `dotenv-java`


* **Évolution de l'entité `User` :**
* Ajout des champs : `enabled` (boolean, `false` par défaut), `verificationToken` (String), `tokenExpiryDate` (LocalDateTime).



---

### Étape 2 : Inscription (`/api/auth/register`)

1. **DTO `RegisterRequest` :** validation stricte de l'email et du mot de passe (min. 8 caractères).
2. **`AuthService.register()` :**
* Vérification de l'unicité de l'email.
* Hachage du mot de passe avec **BCrypt**.
* Génération d'un token d'activation aléatoire (`UUID`) valide 24 h.
* Sauvegarde de l'utilisateur avec `enabled = false`.
* Envoi de l'email de confirmation via le lien `http://localhost:8080/api/auth/confirm?token=...`.



---

### Étape 3 : Activation du compte (`/api/auth/confirm`)

1. **Endpoint GET `/api/auth/confirm` :**
* Récupération du token via Query Param.
* Contrôle de la validité et de l'expiration.
* Passage de `enabled` à `true` et nettoyage du `verificationToken`.



---

### Étape 4 : Connexion & Génération JWT (`/api/auth/login`)

1. **DTO `LoginRequest` :** email et mot de passe.
2. **`AuthService.login()` :**
* Authentification via `AuthenticationManager` de Spring Security.
* Bloquage automatique de la connexion si `enabled == false`.
* Génération d'un token JWT (expirant en 15–60 min) signé avec une clé secrète stockée dans `.env`.
* Retour du token au client (HTTP `200 OK`).



---

### Étape 5 : Sécurisation de l'API & Filtre JWT

1. **Filtre `JwtAuthenticationFilter` :**
* Interception de chaque requête HTTP entrante.
* Extraction et validation du token dans l'en-tête `Authorization: Bearer <token>`.
* Hydratation du `SecurityContextHolder`.


2. **Configuration `SecurityFilterChain` :**
* Mode `STATELESS` et désactivation du CSRF.
* Accès public (`permitAll()`) sur `/api/auth/**` et `/api/products/**`.
* Protection de toutes les autres routes avec `.authenticated()`.
 */