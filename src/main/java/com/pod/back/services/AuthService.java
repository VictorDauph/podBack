package com.pod.back.services;


import com.pod.back.dto.RegisterRequest;
import com.pod.back.entities.User;
import com.pod.back.repositories.UserRepository;
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

            public AuthService(UserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               EmailService mailSender) {
                this.userRepository = userRepository;
                this.passwordEncoder = passwordEncoder;
                this.mailSender = mailSender;
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
                        .tokenExpiration(LocalDateTime.now().plusMinutes(2))
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

        }