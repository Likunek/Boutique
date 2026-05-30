package ru.angelika.boutique.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class ItemsAtStorageDto {
    @NotNull
    private Long itemId;
    @NotNull
    private Long storageId;
    @NotNull
    @Min(1)
    private Long count;
}
