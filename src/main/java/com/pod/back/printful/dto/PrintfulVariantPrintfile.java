package com.pod.back.printful.dto;

import java.util.Map;

// placements : nom du placement (ex. "front") -> printfile_id
public record PrintfulVariantPrintfile(long variantId, Map<String, Long> placements) {
}
