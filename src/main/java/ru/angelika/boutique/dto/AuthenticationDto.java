package ru.angelika.boutique.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO для изменения учётных данных (номера телефона и/или пароля).
 * Используется в {@link ru.angelika.boutique.service.AuthenticationService)}.
 */
@Data
@Builder
public class AuthenticationDto {

    /** Новый номер телефона (логин). Если не меняется, должен быть равен старому. */
    private String number;

    /** Текущий пароль — обязателен для проверки перед изменениями. */
    private String oldPassword;

    /** Новый пароль. Если пустая строка, пароль не изменяется. */
    private String newPassword;
}