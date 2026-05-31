package ru.angelika.boutique.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

/**
 * DTO для добавления нового физического товара (Item) продавцом.
 * Содержит характеристики товара: название, стоимость, вес, площадь.
 */
@Data
@Builder
public class ItemDto {

    /** Название товара. */
    @NotBlank
    private String name;

    /** Стоимость, которую ставит продавец. */
    @Min(1)
    private Double costPrice;

    /** Вес товара в кг. Допустимый диапазон: 0.1 – 20 кг. */
    @DecimalMin(value = "0.1", message = "Weight must be at least 0.1 kg")
    @DecimalMax(value = "20", message = "Weight must be no more than 20 kg")
    private Double weight;

    /** Площадь, занимаемая товаром. Допустимый диапазон: 0.01 – 4 м². */
    @DecimalMin(value = "0.01", message = "Square must be at least 0.01 m^2")
    @DecimalMax(value = "4", message = "Square must be no more than 4 m^2")
    private Double square;
}