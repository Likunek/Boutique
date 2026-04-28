package ru.angelika.boutique.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Entity
@Table(schema = "public", name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    @Column(name = "password", nullable = false)
    private String password;
    @Column(name = "number", nullable = false, unique = true)
    private String number;
    @Column(name = "email", unique = true)
    private String email = null;
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "cart_id")
    private Cart cart;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_receipt_id")
    private PointReceipt pointReceipt;
    @Column(name = "balance")
    @Min(0)
    private Double balance = 0.0;
}