package ru.angelika.boutique.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderDto {
    private List<Long> itemCards;
    private Long pointId;

}
