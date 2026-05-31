package ru.angelika.boutique.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import ru.angelika.boutique.model.Role;

/**
 * DTO для регистрации нового пользователя.
 * Содержит все необходимые данные для создания учётной записи.
 */
@Data
@Builder
public class UserDto {

    /** Имя пользователя (логин, отображаемое). Не может быть пустым. */
    @NotBlank
    private String name;

    /** Пароль. Длина от 4 до 10 символов. */
    @Size(min = 4, max = 10, message = "password must be from 4 to 10 characters")
    private String password;

    /** Номер телефона (используется как логин для входа). Ровно 11 символов. */
    @Size(min = 11, max = 11, message = "Number must be exactly 11 characters")
    private String number;

    /** Email пользователя (должен быть валидным). */
    @Email
    private String email;

    /** Роль пользователя (USER, SELLER, ADMIN). */
    @NotNull
    private Role role;
}