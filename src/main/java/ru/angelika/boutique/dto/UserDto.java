package ru.angelika.boutique.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import ru.angelika.boutique.model.Role;

@Data
@Builder
public class UserDto {
    @NotBlank
    private String name;

    @Size(min = 4, max = 10, message = "password must be from 4 to 10 characters")
    private String password;

    @Size(min = 11, max = 11, message = "Number must be exactly 11 characters")
    private String number;
    @Email
    private String email;
    @NotNull
    private Role role;
}
