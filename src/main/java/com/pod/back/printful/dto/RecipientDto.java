package com.pod.back.printful.dto;

import jakarta.validation.constraints.NotBlank;

public record RecipientDto(
        @NotBlank String name,
        @NotBlank String address1,
        String address2,
        @NotBlank String city,
        String stateCode,
        @NotBlank String countryCode,
        @NotBlank String zip,
        String phone,
        String email
) {
}
