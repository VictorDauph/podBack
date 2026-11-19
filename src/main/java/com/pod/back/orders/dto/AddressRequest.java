package com.pod.back.orders.dto;

import com.pod.back.entities.Address;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank String street,
        String complement,
        @NotBlank String zipCode,
        @NotBlank String city,
        @NotBlank @Size(min = 2, max = 2) String countryCode
) {
    public Address toEntity() {
        return Address.builder()
                .street(street)
                .complement(complement)
                .zipCode(zipCode)
                .city(city)
                .countryCode(countryCode)
                .build();
    }
}
