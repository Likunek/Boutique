package ru.angelika.boutique.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.angelika.boutique.dto.ItemCardDto;
import ru.angelika.boutique.dto.ItemCardUpdateDto;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.mapper.ItemCardMapper;
import ru.angelika.boutique.model.Item;
import ru.angelika.boutique.model.ItemCard;
import ru.angelika.boutique.repository.ItemCardRepository;
@Slf4j
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
        ItemCard itemCard = itemCardRepository.save(ItemCardMapper.toItemCard(itemCardDto, item.getCostPrice(), seller));
        item.setItemCard(itemCard);
        itemService.addCardItem(item);
        log.info("Add new ItemCard: name={}, description={}, itemId={}",
                itemCardDto.getName(), itemCardDto.getDescription(), itemCardDto.getItemId());
    }

    public ItemCard getItemCard(Long id) {
        log.debug("Get itemCard by id={}", id);
        return itemCardRepository.findById(id).orElseThrow(() -> {
            log.error("ItemCard not found for get, id={}", id);
            return new ResourceNotFoundException(ItemCard.class, id);
        });
    }

    public void updateItemCard(ItemCardUpdateDto itemCardDto, Long id) {
        ItemCard itemCard = itemCardRepository.findById(id).orElseThrow(() -> {
            log.error("ItemCard not found for update, id={}", id);
            return new ResourceNotFoundException(ItemCard.class, id);
        });
        ItemCardMapper.toItemCardUpdate(itemCardDto, itemCard);
        itemCardRepository.save(itemCard);
        log.info("Update itemCard by id={}", id);
    }
    public void deleteItemCard(Long id) {
        itemCardRepository.findById(id).orElseThrow(() -> {
            log.error("ItemCard not found for delete, id={}", id);
            return new ResourceNotFoundException(ItemCard.class, id);
        });
        itemCardRepository.deleteById(id);
        log.info("Delete itemCard by id={}", id);
    }
}
