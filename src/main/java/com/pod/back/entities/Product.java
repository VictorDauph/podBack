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

    // Attribut facultatif pour démarrer simplement
    @Column(nullable = true)
    private String category;

    // Identifiant du variant Printful (sync variant id) correspondant à ce produit. Nullable tant
    // qu'un produit n'est pas encore relié au catalogue Printful. Utilisé uniquement pour pousser
    // les commandes (PrintfulService.createOrder) -- différent du catalogue Printful ci-dessous.
    @Column(name = "printful_variant_id")
    private Long printfulVariantId;

    // Produit/variant du CATALOGUE Printful (espace d'identifiants différent du sync variant
    // ci-dessus) utilisés pour générer le mockup via PrintfulService.generateMockup.
    @Column(name = "printful_catalog_product_id")
    private Long printfulCatalogProductId;

    @Column(name = "printful_catalog_variant_id")
    private Long printfulCatalogVariantId;

    // Image de design source envoyée à Printful pour générer le mockup.
    @Column(name = "design_image_url")
    private String designImageUrl;

    // Emplacement d'impression (ex: "front"), "front" par défaut si non renseigné.
    @Column(name = "mockup_placement")
    private String mockupPlacement;

    // URL de l'aperçu généré par Printful (mockup-generator). Null tant qu'aucun mockup n'a été
    // généré, ou si les champs ci-dessus sont incomplets.
    @Column(name = "mockup_url")
    private String mockupUrl;
}