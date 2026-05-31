package ru.angelika.boutique.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Товарная карточка — публичное представление товара для покупателя.
 * Содержит название, описание, цену, рейтинг и отзывы.
 * Не привязана напрямую к физическому экземпляру, связывается через {@link Item}.
 */
@Entity
@Data
@Table(schema = "public", name = "item_cards")
public class ItemCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;

    /** Название товара. */
    @Column(name = "name", nullable = false)
    private String name;

    /** Подробное описание товара. */
    @Column(name = "description")
    private String description;

    /** Цена для покупателя (выше на 20% от стоимости продавца). */
    @Column(name = "price", nullable = false)
    private Double price = 0.0;

    /** Имя продавца. */
    @Column(name = "seller", nullable = false)
    private String seller;

    /** Средний рейтинг товара (рассчитывается на основе отзывов). */
    @Column(name = "rating")
    private Double rating = 0.0;

    /** Список отзывов на товар. Сортировка по дате (сначала новые). */
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("date DESC")
    @JoinTable(name = "item_feedback",
            inverseJoinColumns = @JoinColumn(name = "feedback_id"))
    private List<Feedback> feedbacks = new ArrayList<>();
}