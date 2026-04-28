package ru.angelika.boutique.model;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
@Table(schema = "public", name = "point_receipts")
public class PointReceipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;
    @Column(name = "address")
    private String address;
    @Column(name = "description")
    private String description;
    @Column(name = "rating")
    private Double rating;
}