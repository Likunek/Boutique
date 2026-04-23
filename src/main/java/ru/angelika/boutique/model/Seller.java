package ru.angelika.boutique.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(schema = "public", name = "sellers")
public class Seller {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Integer id;
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    @Column(name = "number", nullable = false, unique = true)
    private String number;
    @Column(name = "email", unique = true)
    private String email;
}