package ru.angelika.boutique.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Покупатель.
 * Содержит личные данные, корзину, баланс, учётные данные и список купленных товаров.
 */
@Data
@Entity
@Table(schema = "public", name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;

    /** Имя пользователя. */
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /** Номер телефона (логин). */
    @Column(name = "number", nullable = false, unique = true)
    private String number;

    /** Email пользователя. */
    @Column(name = "email", unique = true)
    private String email;

    /** Корзина пользователя. */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    /** Баланс счёта пользователя (пока не реализовано пополнение на счет кладется 10000 при регистрации). */
    @Column(name = "balance")
    private Double balance = 10000.0;

    /** Учётные данные (логин, пароль, роль) для входа. */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "authentication_id")
    private Authentication authentication;

    /** Список купленных товаров (для отображения истории покупок на старнице пользователя). */
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "user_item",
            inverseJoinColumns = @JoinColumn(name = "item_id"))
    private Set<ItemCard> items = new HashSet<>();
}