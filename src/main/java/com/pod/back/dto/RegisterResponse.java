package com.pod.back.dto;

public record RegisterResponse(
        Long id,
        String email,
        String name
) {}