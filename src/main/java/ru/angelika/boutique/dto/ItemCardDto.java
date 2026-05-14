package ru.angelika.boutique.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ItemCardDto {
    @NotNull
    private Long itemId;
    @NotNull
    private String name;
    private String description;
}
