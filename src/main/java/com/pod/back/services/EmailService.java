package com.pod.back.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${baseUrl}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String userEmail, String token) {
        String confirmationUrl = baseUrl + "/api/auth/confirm?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(userEmail);
        message.setSubject("Activation de votre compte");
        message.setText("Bienvenue ! Veuillez cliquer sur le lien suivant pour activer votre compte :\n" + confirmationUrl);

        mailSender.send(message);
    }
}