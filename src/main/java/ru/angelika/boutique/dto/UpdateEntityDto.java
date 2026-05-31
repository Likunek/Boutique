package ru.angelika.boutique.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

/**
 * Универсальный DTO для обновления профиля пользователя или продавца.
 * Позволяет изменить имя, номер телефона, пароль и email.
 */
@Data
@Builder
public class UpdateEntityDto {

    /** Новое имя. Не может быть пустым. */
    @NotBlank
    private String name;

    /** Новый номер телефона (логин). Должен содержать ровно 11 символов. */
    @Size(min = 11, max = 11, message = "Number must be exactly 11 characters")
    private String number;

    /** Текущий пароль — обязателен для проверки перед изменениями. */
    @NotBlank
    private String oldPassword;

    /** Новый пароль. Может быть пустым (тогда пароль не меняется), иначе длина от 4 до 10 символов. */
    @Pattern(regexp = "^(|.{4,10})$", message = "password must be from 4 to 10 characters or empty")
    private String newPassword;

    /** Новый email (должен быть корректным). */
    @Email
    private String email;
}