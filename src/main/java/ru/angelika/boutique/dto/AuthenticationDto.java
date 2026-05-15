package ru.angelika.boutique.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthenticationDto {
    private String number;
    private String oldPassword;
    private String newPassword;
}
