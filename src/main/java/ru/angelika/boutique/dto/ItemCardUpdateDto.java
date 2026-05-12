package ru.angelika.boutique.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ItemCardUpdateDto {
    @NotNull
    private String name;
    private String description;
}
