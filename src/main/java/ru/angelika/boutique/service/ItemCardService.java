package ru.angelika.boutique.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.angelika.boutique.dto.ItemCardDto;
import ru.angelika.boutique.dto.ItemCardUpdateDto;
import ru.angelika.boutique.exception.ResourceExistsException;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.mapper.ItemCardMapper;
import ru.angelika.boutique.model.Item;
import ru.angelika.boutique.model.ItemCard;
import ru.angelika.boutique.repository.ItemCardRepository;

import java.util.List;

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

    public Page<ItemCard> getAllItemCard(int page, int size) {
        return itemCardRepository.findAll(PageRequest.of(page, size));
    }

    public void updateItemCard(ItemCardUpdateDto itemCardDto, Long id, String seller) {
        ItemCard itemCard = itemCardRepository.findById(id).orElseThrow(() -> {
            log.error("ItemCard not found for update, id={}", id);
            return new ResourceNotFoundException(ItemCard.class, id);
        });
        List<ItemCard> nameDuplicate = itemCardRepository.findByNameAndSeller(itemCardDto.getName(), seller);
        List<ItemCard> descriptionDuplicate = itemCardRepository
                .findByDescriptionAndSeller(itemCardDto.getDescription(), seller);
        if (nameDuplicate.size() > 0 && descriptionDuplicate.size() > 0) {
            log.error("ItemCard by seller={}, with name={}, description={} already exists",
                    seller, itemCardDto.getName(), itemCardDto.getDescription());
            throw new ResourceExistsException(ItemCard.class, itemCardDto.getName() + " : "+ itemCardDto.getDescription());
        }
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
