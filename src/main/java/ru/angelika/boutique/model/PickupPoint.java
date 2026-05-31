package ru.angelika.boutique.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Пункт выдачи заказов (ПВЗ).
 * Имеет адрес, город, рейтинг и привязанный ближайший склад (где хранятся товары для выдачи).
 */
@Entity
@Data
@Table(schema = "public", name = "point_receipts")
public class PickupPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;

    /** Адрес пункта выдачи. */
    @Column(name = "address")
    private String address;

    /** Город, в котором находится пункт. */
    @Column(name = "city")
    private String city;

    /** Рейтинг пункта выдачи. */
    @Column(name = "rating")
    private Double rating = 0.0;

    /** Склад, обслуживающий этот пункт выдачи. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_id")
    private Storage storage;
}