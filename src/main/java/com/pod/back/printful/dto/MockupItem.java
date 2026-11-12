package com.pod.back.printful.dto;

import java.util.List;

public record MockupItem(String placement, List<Long> variantIds, String mockupUrl, Long generatorMockupId) {
}
