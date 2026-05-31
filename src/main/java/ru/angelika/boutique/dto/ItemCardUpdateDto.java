package ru.angelika.boutique.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

/**
 * DTO для обновления существующей товарной карточки (изменение названия и описания).
 */
@Data
@Builder
public class ItemCardUpdateDto {
    /** Новое название товара. */
    @NotNull
    private String name;

    /** Новое описание товара (может быть null). */
    private String description;
}