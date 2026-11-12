package com.pod.back.printful.dto;

import java.util.List;
import java.util.Map;

public record PrintfulPrintfilesResult(
        long productId,
        Map<String, String> availablePlacements,
        List<PrintfulPrintfile> printfiles,
        List<PrintfulVariantPrintfile> variantPrintfiles
) {
}
