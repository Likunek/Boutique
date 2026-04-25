package ru.angelika.boutique.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(schema = "public", name = "storages")
public class Storage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;
    @Column(name = "address", length = 50)
    private String address;
    @Column(name = "city", length = 20)
    private String city;
    @Column(name = "max_capacity")
    private Long maxCapacity;
}