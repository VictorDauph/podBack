package com.pod.back.enums;

public enum OrderStatus {
    PENDING,    // En attente de paiement
    PAID,       // Payé, prêt pour l'envoi au fournisseur POD
    PROCESSING, // En cours d'impression chez le fournisseur POD
    SHIPPED,    // Expédié avec numéro de suivi
    CANCELLED   // Annulé
}