package com.pod.back.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Boolean isAvailable;

    private String imageUrl;

    // Attribut facultatif pour démarrer simplement
    @Column(nullable = true)
    private String category;

    // Identifiant du variant Printful (sync variant id) correspondant à ce produit. Nullable tant
    // qu'un produit n'est pas encore relié au catalogue Printful.
    @Column(name = "printful_variant_id")
    private Long printfulVariantId;
}