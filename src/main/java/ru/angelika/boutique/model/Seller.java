package ru.angelika.boutique.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Продавец — лицо, размещающее товары в системе.
 * Содержит контактные данные и привязку к учётной записи (Authentication).
 */
@Data
@Entity
@Table(schema = "public", name = "sellers")
public class Seller {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;

    /** Название магазина / имя продавца. Уникально. */
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /** Номер телефона (логин). */
    @Column(name = "number", nullable = false, unique = true)
    private String number;

    /** Email продавца. */
    @Column(name = "email", unique = true)
    private String email;

    /** Учётные данные (логин, пароль, роль) для входа в систему. */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "authentication_id")
    private Authentication authentication;
}