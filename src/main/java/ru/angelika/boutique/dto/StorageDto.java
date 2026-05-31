package ru.angelika.boutique.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

/**
 * DTO для создания или обновления склада.
 */
@Data
@Builder
public class StorageDto {

    /** Адрес склада. Не может быть пустым, максимум 50 символов. */
    @NotBlank(message = "Address is required")
    @Size(max = 50, message = "Address must be up to 50 characters")
    private String address;

    /** Город. Не может быть пустым, максимум 20 символов. */
    @NotBlank(message = "City is required")
    @Size(max = 20, message = "City must be up to 20 characters")
    private String city;

    /** Максимальная вместимость склада в м^2. Не может быть null, минимум 1000. */
    @NotNull(message = "Capacity is required")
    @Min(1000)
    private Long maxCapacity;
}