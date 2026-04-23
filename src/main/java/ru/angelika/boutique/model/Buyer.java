package ru.angelika.boutique.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Entity
@Table(schema = "public", name = "buyers")
public class Buyer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Integer id;
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    @Column(name = "password", nullable = false, unique = true)
    private String password;
    @Column(name = "number", nullable = false, unique = true)
    private String number;
    @Column(name = "email", unique = true)
    private String email;
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "cart_id")
    private Cart cart;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_receipt_id")
    private PointReceipt pointReceipt;
    @Column(name = "balance")
    @Min(1)
    private Double balance = 0.0;
}