package ru.angelika.boutique.mapper;

import ru.angelika.boutique.dto.ItemDto;
import ru.angelika.boutique.model.Item;

public class ItemMapper {
    public static Item toItem(ItemDto itemDto) {
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setCostPrice(itemDto.getCostPrice());
        item.setWeight(itemDto.getWeight());
        return item;
    }
}
