package ru.angelika.boutique.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Поставка товаров со склада в пункты выдачи.
 * Содержит список физических товаров (Item), общий вес, статус и дату.
 */
@Data
@Entity
@Table(schema = "public", name = "supplies")
public class Supply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;

    /** Список товаров, включённых в поставку. */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "supply_item",
            inverseJoinColumns = @JoinColumn(name = "item_id"))
    private List<Item> items = new ArrayList<>();

    /** Общий вес поставки. */
    @Column(name = "weight")
    private Double weight = 0.0;

    /** Статус движения поставки. */
    @Enumerated(EnumType.STRING)
    private Status status = Status.WAY;

    /** Дата и время создания поставки. */
    @Column(name = "date")
    private LocalDateTime date = LocalDateTime.now();

    /** Склад откуда везут товары. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_id")
    private Storage storage;

    /** Пункт выдачи куда везут товары. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_receipt_id")
    private PickupPoint point;
}