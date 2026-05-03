package ru.angelika.boutique.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemCardDto {
    @NotNull
    private Long itemId;
    @NotNull
    private String name;
    private String description;
}
