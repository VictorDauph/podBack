package com.pod.back.printful.dto;

public record SyncProductSummary(
        long id,
        String externalId,
        String name,
        int variants,
        int synced,
        String thumbnailUrl
) {
}
