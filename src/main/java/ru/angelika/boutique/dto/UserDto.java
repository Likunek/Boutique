package ru.angelika.boutique.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import ru.angelika.boutique.model.Role;

@Data
@Builder
public class UserDto {
    @NotNull
    private String name;
    @NotNull
    @Size(min = 4, max = 10, message = "password must be from 4 to 10 characters")
    private String password;
    @NotNull
    @Size(min = 11, max = 11, message = "Number must be exactly 8 characters")
    private String number;
    @Email
    private String email;
    private Role role;
}
