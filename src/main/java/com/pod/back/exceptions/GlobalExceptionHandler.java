package com.pod.back.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Erreurs de validation des DTOs (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "La requête contient des champs invalides"
        );
        problemDetail.setTitle("Erreur de validation");
        problemDetail.setType(URI.create("https://api.ecommerce.com/errors/validation-error"));
        problemDetail.setProperty("timestamp", Instant.now());

        // Récupération dynamique des champs en erreur
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );
        problemDetail.setProperty("invalidFields", fieldErrors);

        return problemDetail;
    }

    // 2. Identifiants incorrects (Login)
    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Email ou mot de passe incorrect"
        );
        problemDetail.setTitle("Échec d'authentification");
        problemDetail.setType(URI.create("https://api.ecommerce.com/errors/invalid-credentials"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    // 3. Compte non activé
    @ExceptionHandler(DisabledException.class)
    public ProblemDetail handleDisabled(DisabledException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "Veuillez activer votre compte via l'email de confirmation avant de vous connecter"
        );
        problemDetail.setTitle("Compte non activé");
        problemDetail.setType(URI.create("https://api.ecommerce.com/errors/account-disabled"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    // 4. Arguments invalides / Logique métier (ex: email déjà utilisé, token expiré)
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problemDetail.setTitle("Requête invalide");
        problemDetail.setType(URI.create("https://api.ecommerce.com/errors/bad-request"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    // 5. État illégal (ex: compte déjà activé lors de la réexpedition de token)
    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problemDetail.setTitle("Conflit d'état");
        problemDetail.setType(URI.create("https://api.ecommerce.com/errors/conflict"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    // 6. Exception générique (Fallback de sécurité OWASP pour masquer les détails BDD/Stacktraces)
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Une erreur interne inattendue est survenue"
        );
        problemDetail.setTitle("Erreur serveur");
        problemDetail.setType(URI.create("https://api.ecommerce.com/errors/internal-server-error"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}