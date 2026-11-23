package com.pod.back.exceptions;

import com.pod.back.printful.exception.PrintfulApiException;
import com.pod.back.storage.exception.ImageStorageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

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

    // 4b. Ressource introuvable (ex : commande/produit inexistant)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problemDetail.setTitle("Ressource introuvable");
        problemDetail.setType(URI.create("https://api.ecommerce.com/errors/not-found"));
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

    // 6b. Erreurs provenant de l'API externe Printful : toujours remontées en 502, qu'elles
    // soient 4xx ou 5xx côté Printful, pour signaler une panne d'intégration plutôt qu'une
    // mauvaise requête de notre propre appelant.
    @ExceptionHandler(PrintfulApiException.class)
    public ProblemDetail handlePrintfulApiException(PrintfulApiException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY,
                "Le service d'impression à la demande (Printful) est indisponible ou a rejeté la requête"
        );
        problemDetail.setTitle("Erreur Printful");
        problemDetail.setType(URI.create("https://api.ecommerce.com/errors/printful-error"));
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("printfulStatus", ex.getHttpStatus());
        problemDetail.setProperty("printfulReason", ex.getPrintfulReason());
        return problemDetail;
    }

    // 6c. Échec de stockage/récupération d'image sur R2 (upload direct ou réhébergement d'un
    // mockup Printful) : même logique que PrintfulApiException, 502 car c'est un service externe.
    @ExceptionHandler(ImageStorageException.class)
    public ProblemDetail handleImageStorageException(ImageStorageException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY,
                "Le stockage d'images est indisponible ou a rejeté la requête"
        );
        problemDetail.setTitle("Erreur de stockage image");
        problemDetail.setType(URI.create("https://api.ecommerce.com/errors/image-storage-error"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    // 6d. Fichier envoyé trop volumineux (cf. spring.servlet.multipart.max-file-size)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "Le fichier envoyé dépasse la taille maximale autorisée"
        );
        problemDetail.setTitle("Fichier trop volumineux");
        problemDetail.setType(URI.create("https://api.ecommerce.com/errors/payload-too-large"));
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