package ru.angelika.boutique.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Отзыв пользователя о товаре.
 * Содержит оценку (1–5), текст и дату создания.
 */
@Data
@Entity
@Table(schema = "public", name = "feedbacks")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;

    /** Пользователь, оставивший отзыв. */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    /** Дата и время написания отзыва (автоматически устанавливается при создании). */
    @Column(name = "date")
    private LocalDateTime date = LocalDateTime.now();

    /** Оценка (от 1 до 5).*/
    @Column(name = "rating", nullable = false)
    private Integer rating;

    /** Текст отзыва. */
    @Column(name = "text")
    private String text;
}