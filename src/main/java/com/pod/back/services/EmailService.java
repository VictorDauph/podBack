package com.pod.back.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import sendinblue.ApiClient;
import sendinblue.ApiException;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.*;

import java.util.List;

@Service
public class EmailService {


    @Value("${baseUrl:http://localhost:8080}")
    private String baseUrl;

    @Value("${sender.mail}")
    private String senderMail;

    @Value("${mail.api.key}")
    private String apiKey;

    @Value("${sender.name}")
    private String senderName;

    @Async
    public void sendVerificationEmail(String toEmail, String token) {

        //todo: renvoyer vers le front
        String confirmationUrl = baseUrl + "/api/auth/confirm?token=" + token;

        ApiClient defaultClient = Configuration.getDefaultApiClient();
        ApiKeyAuth apiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKeyAuth.setApiKey(apiKey);

        TransactionalEmailsApi apiInstance = new TransactionalEmailsApi();

        SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
        sendSmtpEmail.setSender(new SendSmtpEmailSender().email(senderMail).name(senderName));
        sendSmtpEmail.setTo(List.of(new SendSmtpEmailTo().email(toEmail)));
        sendSmtpEmail.setSubject("Vérification de votre compte");
        sendSmtpEmail.setHtmlContent("""
            <div style="font-family: Arial, sans-serif; padding: 20px; max-width: 600px; margin: auto; border: 1px solid #e0e0e0; border-radius: 8px;">
                <h2 style="color: #333; text-align: center;">Bienvenue sur Print-on-Demand !</h2>
                <p>Merci pour votre inscription. Pour activer votre compte, veuillez cliquer sur le bouton ci-dessous :</p>
                <div style="text-align: center; margin: 30px 0;">
                    <a href="%s" style="background-color: #4F46E5; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block;">Activer mon compte</a>
                </div>
                <p style="color: #666; font-size: 14px;">Si vous n'êtes pas à l'origine de cette demande, vous pouvez ignorer cet e-mail.</p>
                <p style="color: #999; font-size: 12px; text-align: center; margin-top: 30px;">Ce lien est valide pour une durée limitée.</p>
            </div>
            """.formatted(confirmationUrl));

        try {
            CreateSmtpEmail result = apiInstance.sendTransacEmail(sendSmtpEmail);
            System.out.println("Email envoyé avec succès, ID : " + result.getMessageId());
        } catch (ApiException e) {
            System.err.println("--- ERREUR API BREVO ---");
            System.err.println("Code HTTP : " + e.getCode());
            System.err.println("Message d'erreur : " + e.getMessage());
            System.err.println("Réponse du serveur (Body) : " + e.getResponseBody());
        } catch (Exception e) {
            System.err.println("Autre erreur inattendue : " + e.getMessage());
            e.printStackTrace();
        }
    }



}