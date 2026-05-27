package ru.angelika.boutique.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrderDto {
    @NotEmpty
    private List<Long> itemCards;
    @NotNull
    private Long pointId;

}
