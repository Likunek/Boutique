package ru.angelika.boutique.mapper;

import ru.angelika.boutique.dto.ItemDto;
import ru.angelika.boutique.model.Item;
import ru.angelika.boutique.model.Seller;

public class ItemMapper {
    public static Item toItem(ItemDto itemDto, Seller seller) {
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setCostPrice(itemDto.getCostPrice());
        item.setWeight(itemDto.getWeight());
        item.setSquare(itemDto.getSquare());
        item.setSeller(seller);
        return item;
    }

    public static void toItemUpdate(ItemDto itemDto, Item item) {
        item.setName(itemDto.getName());
        item.setCostPrice(itemDto.getCostPrice());
        item.setWeight(itemDto.getWeight());
        item.setSquare(itemDto.getSquare());
    }

}
