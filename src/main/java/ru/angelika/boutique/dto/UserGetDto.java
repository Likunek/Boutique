package ru.angelika.boutique.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserGetDto {
    private Long id;
    private String name;
    private String number;
    private String email;

}
