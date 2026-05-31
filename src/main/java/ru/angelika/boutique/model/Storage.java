package ru.angelika.boutique.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Склад.
 * Хранит товары (через ItemsAtStorage) и может быть привязан к пункту выдачи.
 */
@Entity
@Data
@Table(schema = "public", name = "storages")
public class Storage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;

    /** Адрес склада. */
    @Column(name = "address", length = 50)
    private String address;

    /** Город. */
    @Column(name = "city", length = 20)
    private String city;

    /** Максимальная вместимость склада м^2. */
    @Column(name = "max_capacity")
    private Long maxCapacity;

    /** Свободное место на складе м^2 (с учетом товаров, которые уже есть на складе). */
    @Column(name = "current_capacity")
    private Double currentCapacity;
}