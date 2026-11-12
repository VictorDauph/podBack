package com.pod.back.printful;

import com.pod.back.printful.dto.CreateMockupTaskRequest;
import com.pod.back.printful.dto.CreateOrderRequest;
import com.pod.back.printful.dto.GenerateMockupRequest;
import com.pod.back.printful.dto.MockupFileRequest;
import com.pod.back.printful.dto.MockupPosition;
import com.pod.back.printful.dto.MockupTaskCreationResult;
import com.pod.back.printful.dto.MockupTaskStatusResult;
import com.pod.back.printful.dto.PrintfulOrderRequestBody;
import com.pod.back.printful.dto.PrintfulOrderResponse;
import com.pod.back.printful.dto.PrintfulPrintfile;
import com.pod.back.printful.dto.PrintfulPrintfilesResult;
import com.pod.back.printful.dto.SyncProductDetailResponse;
import com.pod.back.printful.dto.SyncProductSummary;
import com.pod.back.printful.exception.PrintfulApiException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Service
public class PrintfulService {

    private static final int MAX_POLL_ATTEMPTS = 15;
    private static final Duration POLL_INTERVAL = Duration.ofSeconds(2);

    private final PrintfulClient printfulClient;

    public PrintfulService(PrintfulClient printfulClient) {
        this.printfulClient = printfulClient;
    }

    public List<SyncProductSummary> listSyncProducts() {
        return printfulClient.getList("/store/products", SyncProductSummary.class);
    }

    public SyncProductDetailResponse getSyncProduct(long syncProductId) {
        return printfulClient.get("/store/products/" + syncProductId, SyncProductDetailResponse.class);
    }

    public PrintfulOrderResponse createOrder(CreateOrderRequest request) {
        PrintfulOrderRequestBody body = new PrintfulOrderRequestBody(
                request.recipient(),
                request.items(),
                request.externalId()
        );
        String uri = request.confirm() ? "/orders?confirm=1" : "/orders";
        return printfulClient.post(uri, body, PrintfulOrderResponse.class);
    }

    // Produit un mockup à partir du catalogue Printful (pas un store product) : trouve
    // automatiquement les dimensions du printfile pour le variant/placement demandés, crée la
    // tâche de génération (asynchrone côté Printful) puis attend le résultat en bloquant.
    public MockupTaskStatusResult generateMockup(GenerateMockupRequest request) {
        String placement = request.placement() == null || request.placement().isBlank()
                ? "front"
                : request.placement();

        PrintfulPrintfilesResult printfiles = printfulClient.get(
                "/mockup-generator/printfiles/" + request.productId(), PrintfulPrintfilesResult.class);

        Long printfileId = printfiles.variantPrintfiles().stream()
                .filter(vp -> vp.variantId() == request.variantId())
                .map(vp -> vp.placements().get(placement))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aucun printfile pour le variant " + request.variantId() + " et le placement \"" + placement + "\""));

        //dimensions du printfile
        PrintfulPrintfile printfile = printfiles.printfiles().stream()
                .filter(pf -> pf.printfileId() == printfileId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Printfile " + printfileId + " introuvable"));

        MockupPosition position = new MockupPosition(
                printfile.width(), printfile.height(), printfile.width(), printfile.height(), 0, 0);

        CreateMockupTaskRequest taskRequest = new CreateMockupTaskRequest(
                List.of(request.variantId()),
                List.of(new MockupFileRequest(placement, request.imageUrl(), position))
        );

        MockupTaskCreationResult created = printfulClient.post(
                "/mockup-generator/create-task/" + request.productId(), taskRequest, MockupTaskCreationResult.class);

        return pollUntilDone(created.taskKey());
    }

    private MockupTaskStatusResult pollUntilDone(String taskKey) {
        for (int attempt = 0; attempt < MAX_POLL_ATTEMPTS; attempt++) {
            MockupTaskStatusResult result = printfulClient.get(
                    "/mockup-generator/task?task_key=" + taskKey, MockupTaskStatusResult.class);
            if (!"pending".equals(result.status())) {
                return result;
            }
            sleep();
        }
        throw new PrintfulApiException(504, null,
                "Délai dépassé en attendant la génération du mockup (task_key=" + taskKey + ")");
    }

    private void sleep() {
        try {
            Thread.sleep(POLL_INTERVAL.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrompu en attendant le mockup Printful", e);
        }
    }
}
