package ru.angelika.boutique.mapper;

import ru.angelika.boutique.dto.ItemDto;
import ru.angelika.boutique.model.Item;
import ru.angelika.boutique.model.Seller;

/**
 * Маппер для преобразования DTO в сущность {@link Item} и обновления существующего товара.
 */
public class ItemMapper {

    /**
     * Создаёт новый физический товар (Item) на основе DTO и продавца.
     *
     * @param itemDto DTO с характеристиками товара (имя, стоимость, вес, площадь)
     * @param seller  продавец, которому принадлежит товар
     * @return заполненный объект {@link Item}
     */
    public static Item toItem(ItemDto itemDto, Seller seller) {
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setCostPrice(itemDto.getCostPrice());
        item.setWeight(itemDto.getWeight());
        item.setSquare(itemDto.getSquare());
        item.setSeller(seller);
        return item;
    }

    /**
     * Обновляет существующий товар данными из DTO.
     *
     * @param itemDto DTO с новыми характеристиками
     * @param item    целевая сущность, которая будет изменена
     */
    public static void toItemUpdate(ItemDto itemDto, Item item) {
        item.setName(itemDto.getName());
        item.setCostPrice(itemDto.getCostPrice());
        item.setWeight(itemDto.getWeight());
        item.setSquare(itemDto.getSquare());
    }
}