package com.pod.back.printful.dto;

import java.util.List;

public record SyncProductDetailResponse(
        SyncProductSummary syncProduct,
        List<SyncVariant> syncVariants
) {
}
