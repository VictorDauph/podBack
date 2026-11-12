package com.pod.back.printful.dto;

import java.util.List;

public record MockupTaskStatusResult(String taskKey, String status, List<MockupItem> mockups) {
}
