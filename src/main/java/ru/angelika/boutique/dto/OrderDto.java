package ru.angelika.boutique.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO для оформления нового заказа.
 * Содержит список идентификаторов товарных карточек (ItemCard) и идентификатор пункта выдачи.
 */
@Data
@Builder
public class OrderDto {
    /** Список ID товарных карточек, которые пользователь хочет купить. Не может быть пустым. */
    @NotEmpty
    private List<Long> itemCards;

    /** Идентификатор пункта выдачи заказа (PickupPoint). */
    @NotNull
    private Long pointId;
}