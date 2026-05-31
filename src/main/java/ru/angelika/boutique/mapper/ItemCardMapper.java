package ru.angelika.boutique.mapper;

import ru.angelika.boutique.dto.ItemCardDto;
import ru.angelika.boutique.dto.ItemCardUpdateDto;
import ru.angelika.boutique.model.ItemCard;

/**
 * Маппер для преобразования DTO в сущность {@link ItemCard} и обновления существующей карточки.
 */
public class ItemCardMapper {

    /**
     * Создаёт новую товарную карточку на основе DTO, цены продавца и имени продавца.
     * <p>Цена для покупателя устанавливается как цена продавца * 1.2 (наценка 20%).</p>
     *
     * @param itemCardDto DTO с названием и описанием
     * @param price       цена продавца (costPrice)
     * @param sellerName  имя продавца
     * @return готовая сущность {@link ItemCard} с рассчитанной ценой
     */
    public static ItemCard toItemCard(ItemCardDto itemCardDto, Double price, String sellerName) {
        ItemCard itemCard = new ItemCard();
        itemCard.setName(itemCardDto.getName());
        itemCard.setDescription(itemCardDto.getDescription());
        itemCard.setPrice(price * 1.2);
        itemCard.setSeller(sellerName);
        return itemCard;
    }

    /**
     * Обновляет существующую товарную карточку данными из DTO.
     *
     * @param itemCardUpdateDto DTO с новым названием и описанием
     * @param itemCard          целевая сущность, которая будет изменена
     */
    public static void toItemCardUpdate(ItemCardUpdateDto itemCardUpdateDto, ItemCard itemCard) {
        itemCard.setName(itemCardUpdateDto.getName());
        itemCard.setDescription(itemCardUpdateDto.getDescription());
    }
}