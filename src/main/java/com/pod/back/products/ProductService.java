package com.pod.back.products;

import com.pod.back.entities.Product;
import com.pod.back.exceptions.ResourceNotFoundException;
import com.pod.back.printful.PrintfulService;
import com.pod.back.printful.dto.GenerateMockupRequest;
import com.pod.back.printful.dto.MockupItem;
import com.pod.back.printful.dto.MockupTaskStatusResult;
import com.pod.back.products.dto.CreateProductRequest;
import com.pod.back.products.dto.ProductResponse;
import com.pod.back.products.dto.UpdateProductRequest;
import com.pod.back.repositories.ProductRepository;
import com.pod.back.storage.ImageStorageService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ProductService {

    private static final String DEFAULT_PLACEMENT = "front";
    private static final String MOCKUP_KEY_PREFIX = "mockups";

    private final ProductRepository productRepository;
    private final PrintfulService printfulService;
    private final ImageStorageService imageStorageService;

    public ProductService(ProductRepository productRepository, PrintfulService printfulService,
                           ImageStorageService imageStorageService) {
        this.productRepository = productRepository;
        this.printfulService = printfulService;
        this.imageStorageService = imageStorageService;
    }

    public List<ProductResponse> listAvailable() {
        return productRepository.findByIsAvailableTrue().stream()
                .map(ProductResponse::from)
                .toList();
    }

    public ProductResponse getById(Long id) {
        return ProductResponse.from(findOrThrow(id));
    }

    public List<ProductResponse> listAll() {
        return productRepository.findAll().stream()
                .map(ProductResponse::from)
                .toList();
    }

    public ProductResponse create(CreateProductRequest request) {
        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .isAvailable(request.isAvailable())
                .mockupUrl(request.mockupUrl())
                .category(request.category())
                .printfulVariantId(request.printfulVariantId())
                .printfulCatalogProductId(request.printfulCatalogProductId())
                .printfulCatalogVariantId(request.printfulCatalogVariantId())
                .designImageUrl(request.designImageUrl())
                .mockupPlacement(request.mockupPlacement())
                .build();

        if (hasMockupInputs(product)) {
            product.setMockupUrl(generateMockupUrl(product));
        }

        return ProductResponse.from(productRepository.save(product));
    }

    public ProductResponse update(Long id, UpdateProductRequest request) {
        Product product = findOrThrow(id);
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setIsAvailable(request.isAvailable());
        product.setMockupUrl(request.mockupUrl());
        product.setCategory(request.category());
        product.setPrintfulVariantId(request.printfulVariantId());

        boolean mockupInputsChanged =
                !Objects.equals(product.getPrintfulCatalogProductId(), request.printfulCatalogProductId())
                        || !Objects.equals(product.getPrintfulCatalogVariantId(), request.printfulCatalogVariantId())
                        || !Objects.equals(product.getDesignImageUrl(), request.designImageUrl())
                        || !Objects.equals(product.getMockupPlacement(), request.mockupPlacement());

        product.setPrintfulCatalogProductId(request.printfulCatalogProductId());
        product.setPrintfulCatalogVariantId(request.printfulCatalogVariantId());
        product.setDesignImageUrl(request.designImageUrl());
        product.setMockupPlacement(request.mockupPlacement());

        // Ne rappelle Printful (bloquant, ~jusqu'à 30s) que si les champs liés au mockup ont
        // réellement changé -- sinon la valeur de mockupUrl envoyée dans la requête (manuelle ou
        // simplement renvoyée telle quelle par le client) est conservée.
        if (mockupInputsChanged && hasMockupInputs(product)) {
            product.setMockupUrl(generateMockupUrl(product));
        }

        return ProductResponse.from(productRepository.save(product));
    }

    public void delete(Long id) {
        Product product = findOrThrow(id);
        try {
            productRepository.delete(product);
            productRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException(
                    "Impossible de supprimer un produit référencé par des commandes existantes");
        }
    }

    private boolean hasMockupInputs(Product product) {
        return product.getPrintfulCatalogProductId() != null
                && product.getPrintfulCatalogVariantId() != null
                && product.getDesignImageUrl() != null
                && !product.getDesignImageUrl().isBlank();
    }

    private String generateMockupUrl(Product product) {
        String placement = product.getMockupPlacement() == null || product.getMockupPlacement().isBlank()
                ? DEFAULT_PLACEMENT
                : product.getMockupPlacement();

        GenerateMockupRequest request = new GenerateMockupRequest(
                product.getPrintfulCatalogProductId(),
                product.getPrintfulCatalogVariantId(),
                product.getDesignImageUrl(),
                placement
        );

        MockupTaskStatusResult result = printfulService.generateMockup(request);
        String printfulTemporaryUrl = result.mockups().stream()
                .findFirst()
                .map(MockupItem::mockupUrl)
                .orElseThrow(() -> new IllegalStateException(
                        "Printful n'a renvoyé aucun mockup pour ce produit"));

        // L'URL renvoyée par Printful expire sous 24-72h (non documenté de façon cohérente côté
        // Printful) -- on la réhéberge immédiatement sur R2 pour obtenir une URL stable.
        return imageStorageService.reupload(printfulTemporaryUrl, MOCKUP_KEY_PREFIX);
    }

    private Product findOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable (id=" + id + ")"));
    }
}
