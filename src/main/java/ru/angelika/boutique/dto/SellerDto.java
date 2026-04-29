package ru.angelika.boutique.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SellerDto {
    @NotNull
    private String name;
    @NotNull
    @Size(min = 11, max = 11, message = "Number must be exactly 8 characters")
    private String number;
    @Email
    private String email;
}