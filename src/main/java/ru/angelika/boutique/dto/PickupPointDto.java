package ru.angelika.boutique.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

/**
 * DTO для создания или обновления пункта выдачи заказов (ПВЗ).
 */
@Data
@Builder
public class PickupPointDto {

    /** Адрес пункта выдачи. Не может быть пустым, максимум 50 символов. */
    @NotBlank(message = "Address is required")
    @Size(max = 50, message = "Address must be up to 50 characters")
    private String address;

    /** Город. Не может быть пустым, максимум 20 символов. */
    @NotBlank(message = "City is required")
    @Size(max = 20, message = "City must be up to 20 characters")
    private String city;

    /** Идентификатор склада, обслуживающего этот ПВЗ. */
    @NotNull
    private Long storageId;
}