package com.pod.back.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Column(nullable = false)
    private String street; // Numéro et nom de rue

    private String complement; // Appartement, bâtiment, etc. (optionnel)

    @Column(nullable = false)
    private String zipCode;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false, length = 2)
    private String countryCode; // Code ISO 2 lettres (ex: "FR", "BE", "LU", "DE")
}