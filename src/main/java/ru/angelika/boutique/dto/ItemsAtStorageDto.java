package ru.angelika.boutique.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class ItemsAtStorageDto {
    @NotNull
    @Min(1)
    private Long itemId;
    @NotNull
    @Min(1)
    private Long storageId;
    @NotNull
    @Min(1)
    private Long count;
}
