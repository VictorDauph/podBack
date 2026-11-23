package com.pod.back.products;

import com.pod.back.products.dto.CreateProductRequest;
import com.pod.back.products.dto.ProductResponse;
import com.pod.back.products.dto.UpdateProductRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Toutes les actions produit réservées aux ADMIN (voir SecurityConfig : /api/admin/** -> ADMIN).
// Séparé de ProductController qui ne garde que les actions publiques/utilisateur.
@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> listAll() {
        return ResponseEntity.ok(productService.listAll());
    }

    // Renvoie le produit même s'il est désactivé/brouillon (utile pour l'édition), contrairement
    // à ProductController.get() qui est public et filtre sur isAvailable.
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getByIdForAdmin(id));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
