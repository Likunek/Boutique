package ru.angelika.boutique.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Заказ, оформленный пользователем.
 * Содержит список товаров (ItemCard), статус, цену, пункт выдачи.
 */
@Data
@Entity
@Table(schema = "public", name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;

    /** Уникальный код заказа. */
    @Column(name = "code", nullable = false)
    private Integer code;

    /** Пользователь, оформивший заказ. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /** Текущий статус заказа (NEW, WAY, DELIVERED, RECEIVED). */
    @Enumerated(EnumType.STRING)
    private Status status = Status.NEW;

    /** Дата и время оформления заказа. */
    @Column(name = "date", nullable = false)
    private LocalDateTime date = LocalDateTime.now();

    /** Список товаров (карточек) в заказе. */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "order_item",
            inverseJoinColumns = @JoinColumn(name = "item_id"))
    private List<ItemCard> items;

    /** Итоговая стоимость заказа. */
    @Column(name = "price", nullable = false)
    private Double price;

    /** Пункт выдачи, куда должен быть доставлен заказ. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_receipt_id")
    private PickupPoint point;
}