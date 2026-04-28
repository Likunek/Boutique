package ru.angelika.boutique.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StorageDto {
    @NotNull
    @NotBlank
    @Max(50)
    private String address;
    @NotNull
    @NotBlank
    @Max(20)
    private String city;
    @NotNull
    @Min(1000)
    private Long maxCapacity;
}
