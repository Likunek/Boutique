package ru.angelika.boutique.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemDto {
    @NotNull
    private String name;
    @NotNull
    private Double costPrice;
    @NotNull
    @Min(1)
    private Double weight;
    @NotNull
    @Min(1)
    private Integer square;

}
