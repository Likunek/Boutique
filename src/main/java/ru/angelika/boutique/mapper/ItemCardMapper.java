package ru.angelika.boutique.mapper;


import ru.angelika.boutique.dto.ItemCardDto;
import ru.angelika.boutique.model.ItemCard;

public class ItemCardMapper {
    public static ItemCard toItemCard(ItemCardDto itemCardDto) {
        ItemCard itemCard = new ItemCard();
        itemCard.setName(itemCard.getName());
        itemCard.setDescription(itemCardDto.getDescription());
        itemCard.setPrice(itemCardDto.getPrice());
        return itemCard;
    }
}
