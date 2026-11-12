package com.pod.back.auth.controllers.dto;

public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName
) {}