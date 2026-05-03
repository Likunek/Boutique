package ru.angelika.boutique.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.angelika.boutique.dto.ItemCardDto;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.mapper.ItemCardMapper;
import ru.angelika.boutique.model.Item;
import ru.angelika.boutique.model.ItemCard;
import ru.angelika.boutique.repository.ItemCardRepository;

@Service
public class ItemCardService {
    private final ItemCardRepository itemCardRepository;
    private final ItemService itemService;

    @Autowired
    public ItemCardService(ItemCardRepository itemCardRepository, ItemService itemService) {
        this.itemCardRepository = itemCardRepository;
        this.itemService = itemService;
    }

    public void addItemCard(ItemCardDto itemCardDto, String seller) {
        Item item = itemService.getItemById(itemCardDto.getItemId());
        ItemCard itemCard = ItemCardMapper.toItemCard(itemCardDto, item.getCostPrice(), seller);
        item.setItemCard(itemCard);
        itemService.addCardItem(item);
        itemCardRepository.save(itemCard);
    }

    public ItemCard getItemCard(Long id) {
        return itemCardRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(ItemCard.class, id));
    }

    public void updateItemCard(ItemCardDto itemCardDto, Long id, String seller) {
        itemCardRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(ItemCard.class, id));
        Item item = itemService.getItemById(itemCardDto.getItemId());
        itemCardRepository.save(ItemCardMapper.toItemCard(itemCardDto, item.getCostPrice(), seller));
    }
    public void deleteItemCard(Long id) {
        itemCardRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(ItemCard.class, id));
        itemCardRepository.deleteById(id);
    }
}
