package com.pod.back.printful.dto;

import java.util.List;

// Corps envoyé à POST /mockup-generator/create-task/{productId}.
public record CreateMockupTaskRequest(List<Long> variantIds, List<MockupFileRequest> files) {
}
