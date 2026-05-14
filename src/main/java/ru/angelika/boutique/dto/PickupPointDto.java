package ru.angelika.boutique.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PickupPointDto {
    @NotBlank(message = "Address is required")
    @Size(max = 50, message = "Address must be up to 50 characters")
    private String address;
    @NotBlank(message = "City is required")
    @Size(max = 20, message = "City must be up to 20 characters")
    private String city;
    @NotNull
    private Long storageId;
}
