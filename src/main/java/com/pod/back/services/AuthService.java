package com.pod.back.services;


import com.pod.back.dto.AuthResponse;
import com.pod.back.dto.LoginRequest;
import com.pod.back.dto.RegisterRequest;
import com.pod.back.entities.User;
import com.pod.back.repositories.UserRepository;
import com.pod.back.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService mailSender;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${tokenExpiration}")
    private Integer  tokenExpiration;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService mailSender,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public void register(RegisterRequest request) {
        // 1. Vérification de l'unicité de l'email
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Cet email est déjà utilisé.");
        }

        // 2. Génération du token de vérification (valide 24h)
        String token = UUID.randomUUID().toString();

        // 3. Création de l'utilisateur (inactif par défaut)
        User user = User.builder()
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .password(passwordEncoder.encode(request.password()))
                .enabled(false)
                .verificationToken(token)
                .tokenExpiration(LocalDateTime.now().plusMinutes(tokenExpiration))
                .build();

        userRepository.save(user);

        // 4. Envoi de l'email
        mailSender.sendVerificationEmail(user.getEmail(), token);
    }

    @Transactional
    public void confirmAccount(String token) {
        // 1. Recherche de l'utilisateur par son token
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token de vérification invalide."));

        // 2. Vérification de la date d'expiration
        if (user.getTokenExpiration().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Le token de vérification a expiré.");
        }

        // 3. Activation du compte et nettoyage du token
        user.setEnabled(true);
        user.setVerificationToken(null);
        user.setTokenExpiration(null);

        userRepository.save(user);
    }

    @Transactional
    public void resendConfirmationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Aucun compte associé à cet email"));

        if (user.getEnabled()) {
            throw new IllegalStateException("Ce compte est déjà activé");
        }

        // Supprime l'ancien token s'il existe pour éviter les doublons

        // Génère un nouveau token et enregistre
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setTokenExpiration(LocalDateTime.now().plusMinutes(tokenExpiration));

        // Envoie le nouvel email
        mailSender.sendVerificationEmail(user.getEmail(), token);
    }

    public AuthResponse login(LoginRequest request) {
        // 1. Déclenche l'authentification Spring Security (vérifie l'email/mot de passe et le statut 'enabled')
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        // 2. Charger l'utilisateur depuis la DB
        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        // 3. Générer le jeton JWT
        String jwtToken = jwtService.generateToken(user);

        // 4. Renvoyer le DTO
        return new AuthResponse(jwtToken);
    }
}
