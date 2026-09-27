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