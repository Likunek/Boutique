package ru.angelika.boutique.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ItemDto {
    @NotNull
    private String name;
    @NotNull
    private Double costPrice;
    @NotNull
    @DecimalMin(value = "0.1", message = "Weight must be at least 0.1 kg")
    @DecimalMax(value = "20", message = "Weight must be no more than 20 kg")
    private Double weight;
    @NotNull
    @DecimalMin(value = "0.01", message = "Square must be at least 0.01 m^2")
    @DecimalMax(value = "4", message = "Square must be no more than 4 m^2")
    private Double square;

}
