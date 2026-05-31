package ru.angelika.boutique.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

/**
 * DTO для добавления кол-ва товара (Item) на склад (Storage).
 */
@Data
@Builder
public class ItemsAtStorageDto {

    /** Идентификатор физического товара. */
    @NotNull
    private Long itemId;

    /** Идентификатор склада. */
    @NotNull
    private Long storageId;

    /** Количество единиц товара на складе. Должно быть не менее 1. */
    @NotNull
    @Min(1)
    private Long count;
}