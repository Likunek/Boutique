package ru.angelika.boutique.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

/**
 * DTO для создания или обновления отзыва на товар.
 * Содержит идентификатор пользователя, оценку (1–5) и текст отзыва.
 */
@Data
@Builder
public class FeedbackDto {

    /** Идентификатор пользователя, оставляющего отзыв. */
    @NotNull
    private Long userId;

    /** Оценка товара (от 1 до 5 включительно). */
    @NotNull
    @Range(min = 1, max = 5)
    private Integer rating;

    /** Текст отзыва. Может быть пустым. */
    private String text;
}