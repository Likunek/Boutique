package ru.angelika.boutique.mapper;


import ru.angelika.boutique.dto.ItemCardDto;
import ru.angelika.boutique.dto.ItemCardUpdateDto;
import ru.angelika.boutique.model.ItemCard;

public class ItemCardMapper {
    public static ItemCard toItemCard(ItemCardDto itemCardDto, Double price, String sellerName) {
        ItemCard itemCard = new ItemCard();
        itemCard.setName(itemCardDto.getName());
        itemCard.setDescription(itemCardDto.getDescription());
        itemCard.setPrice(price*1.2);
        itemCard.setSeller(sellerName);
        return itemCard;
    }

    public static void toItemCardUpdate(ItemCardUpdateDto itemCardUpdateDto, ItemCard itemCard) {
        itemCard.setName(itemCardUpdateDto.getName());
        itemCard.setDescription(itemCardUpdateDto.getDescription());
    }
}
