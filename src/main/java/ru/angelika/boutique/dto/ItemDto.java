package ru.angelika.boutique.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemDto {
    @NotBlank
    private String name;
    @Min(1)
    private Double costPrice;

    @DecimalMin(value = "0.1", message = "Weight must be at least 0.1 kg")
    @DecimalMax(value = "20", message = "Weight must be no more than 20 kg")
    private Double weight;

    @DecimalMin(value = "0.01", message = "Square must be at least 0.01 m^2")
    @DecimalMax(value = "4", message = "Square must be no more than 4 m^2")
    private Double square;

}
