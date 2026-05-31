package ru.angelika.boutique.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Связующая сущность для хранения количества товаров (Item) на конкретном складе (Storage).
 */
@Entity
@Data
@Table(schema = "public", name = "items_at_storage")
public class ItemsAtStorage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;

    /** Товар. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    /** Склад, на котором хранится товар. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_id")
    private Storage storage;

    /** Количество единиц товара на данном складе. */
    @Column(name = "count")
    private Long count = 0L;
}