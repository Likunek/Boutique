package ru.angelika.boutique.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PointReceiptDto {
    @NotNull
    @NotBlank
    private String address;
    @NotNull
    private String description;
}
