package com.pod.back.printful;

import com.pod.back.printful.dto.CreateOrderRequest;
import com.pod.back.printful.dto.GenerateMockupRequest;
import com.pod.back.printful.dto.MockupTaskStatusResult;
import com.pod.back.printful.dto.PrintfulOrderResponse;
import com.pod.back.printful.dto.SyncProductDetailResponse;
import com.pod.back.printful.dto.SyncProductSummary;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/printful")
public class PrintfulController {

    private final PrintfulService printfulService;

    public PrintfulController(PrintfulService printfulService) {
        this.printfulService = printfulService;
    }

    @GetMapping("/products")
    public ResponseEntity<List<SyncProductSummary>> listProducts() {
        return ResponseEntity.ok(printfulService.listSyncProducts());
    }

    @GetMapping("/products/{syncProductId}")
    public ResponseEntity<SyncProductDetailResponse> getProduct(@PathVariable long syncProductId) {
        return ResponseEntity.ok(printfulService.getSyncProduct(syncProductId));
    }

    @PostMapping("/orders")
    public ResponseEntity<PrintfulOrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(printfulService.createOrder(request));
    }

    // Génère un mockup à partir d'une image publique (ex: productId=279/variantId=9063 pour le
    // sac à dos "All-Over Print Backpack"). Appel bloquant : attend la fin de la génération
    // asynchrone côté Printful avant de répondre (jusqu'à ~30s).
    @PostMapping("/mockups")
    public ResponseEntity<MockupTaskStatusResult> generateMockup(@Valid @RequestBody GenerateMockupRequest request) {
        return ResponseEntity.ok(printfulService.generateMockup(request));
    }
}
