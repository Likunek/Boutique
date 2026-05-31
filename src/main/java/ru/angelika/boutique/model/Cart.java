package ru.angelika.boutique.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Корзина покупок пользователя.
 * Связана с пользователем (User) и содержит список товаров (ItemCard).
 * Общая стоимость пересчитывается при изменении содержимого.
 */
@Entity
@Data
@Table(schema = "public", name = "carts")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;

    /**
     * Список товарных карточек, добавленных в корзину.
     * Связь многие-ко-многим через промежуточную таблицу cart_item_card.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "cart_item_card",
            inverseJoinColumns = @JoinColumn(name = "item_card_id"))
    private List<ItemCard> itemCards = new ArrayList<>();

    /** Общая стоимость всех товаров в корзине.*/
    @Column(name = "total_price")
    private Double totalPrice = 0.0;
}