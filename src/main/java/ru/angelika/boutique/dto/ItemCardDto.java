package ru.angelika.boutique.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

/**
 * DTO для создания новой товарной карточки (ItemCard) на основе существующего физического товара (Item).
 * Используется продавцом.
 */
@Data
@Builder
public class ItemCardDto {

    /** Идентификатор физического товара (Item), для которого создаётся карточка. */
    @NotNull
    private Long itemId;

    /** Название товара (отображается в каталоге). */
    @NotBlank
    private String name;

    /** Описание товара. */
    private String description;
}