package ru.angelika.boutique.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Учётные данные пользователя (логин + зашифрованный пароль + роль).
 * Используется Spring Security для аутентификации.
 */
@Data
@Entity
@Table(schema = "public", name = "authentication")
public class Authentication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;

    /**
     * Номер телефона пользователя (логин).
     * Используется при аутентификации через Spring Security.
     */
    @Column(name = "number", nullable = false, unique = true)
    private String number;

    /** Зашифрованный пароль (хранится с использованием PasswordEncoder). */
    @Column(name = "password", nullable = false)
    private String password;

    /** Роль пользователя (USER, SELLER, ADMIN), определяет права доступа. */
    @Enumerated(EnumType.STRING)
    private Role role;
}