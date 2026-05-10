package ru.angelika.boutique.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrderDto {
    private List<Long> itemCards;
    private Long pointId;

}
