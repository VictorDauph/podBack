package com.pod.back.dto;

public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName
) {}