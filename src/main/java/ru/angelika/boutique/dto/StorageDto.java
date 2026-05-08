package ru.angelika.boutique.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StorageDto {
    @NotBlank(message = "Address is required")
    @Size(max = 50, message = "Address must be up to 50 characters")
    private String address;
    @NotBlank(message = "City is required")
    @Size(max = 20, message = "City must be up to 20 characters")
    private String city;
    @NotNull(message = "Capacity is required")
    @Min(1000)
    private Long maxCapacity;
}
