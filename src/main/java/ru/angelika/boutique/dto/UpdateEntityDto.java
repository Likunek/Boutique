package ru.angelika.boutique.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateEntityDto {
    @NotBlank
    private String name;
    @Size(min = 11, max = 11, message = "Number must be exactly 11 characters")
    private String number;
    @NotBlank
    private String oldPassword;
    @Pattern(regexp = "^(|.{4,10})$", message = "password must be from 4 to 10 characters or empty")
    private String newPassword;
    @Email
    private String email;
}