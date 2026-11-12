package com.pod.back.auth.controllers.dto;

public record RegisterResponse(
        Long id,
        String email,
        String name
) {}