package ru.angelika.boutique.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemCardDto {
    @NotNull
    private String name;
    private String description;
    @NotNull
    @Min(1)
    private Double price;
}
