package ru.angelika.boutique.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.angelika.boutique.dto.ItemDto;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.mapper.ItemMapper;
import ru.angelika.boutique.model.Item;
import ru.angelika.boutique.model.ItemCard;
import ru.angelika.boutique.model.Seller;
import ru.angelika.boutique.repository.ItemCardRepository;
import ru.angelika.boutique.repository.ItemRepository;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final ItemCardRepository itemCardRepository;

    @Autowired
    public ItemService(ItemRepository itemRepository, ItemCardRepository itemCardRepository) {
        this.itemRepository = itemRepository;
        this.itemCardRepository = itemCardRepository;
    }

    public void addItem(ItemDto itemDto, Seller seller) {
        itemRepository.save(ItemMapper.toItem(itemDto, seller));
        log.info("Add new item: name={}, price={}, weight={}, square ={}, sellerId={}",
                itemDto.getName(), itemDto.getCostPrice(), itemDto.getWeight(), itemDto.getSquare(), seller.getId());
    }

    public List<Item> getItemBySellerId(Long id) {
        return itemRepository.findBySellerIdAndVerifyTrue(id);
    }
    public List<Item> getAllItemBySeller( Seller seller) {
        return itemRepository.findBySellerWithStorages(seller);
    }

    public Item getItemById(Long id) {
        log.debug("Get item by id={}", id);
       return itemRepository.findById(id)
               .orElseThrow(() -> {
                   log.error("Item not found for get, id={}", id);
                   return new ResourceNotFoundException(Item.class, id);
               });
    }

    public void addCardItem(Item item) {
        log.info("Update Item's ItemCard field: itemId={}, itemCardId={}",
                item.getId(), item.getItemCard().getId());
        itemRepository.save(item);
    }

    public void updateItem(ItemDto itemDto, Long id) {
        Item item =itemRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Item not found for update, id={}", id);
                    return new ResourceNotFoundException(Item.class, id);
                });
        ItemMapper.toItemUpdate(itemDto, item);
        if (item.getItemCard() != null) {
            ItemCard itemCard = itemCardRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("ItemCard not found for update price, id={}", id);
                        return new ResourceNotFoundException(Item.class, id);
                    });
            itemCard.setPrice(itemDto.getCostPrice()*1.2);
            itemCardRepository.save(itemCard);
            log.debug("Update price itemCard by id={}", itemCard.getId());
        }
        itemRepository.save(item);
        log.info("Update item by id={}", id);
    }

    public void deleteItem(Long id) {
        itemRepository.findById(id).orElseThrow(() -> {
            log.error("Item not found for delete, id={}", id);
            return new ResourceNotFoundException(Item.class, id);
        });
        itemRepository.deleteById(id);
        log.info("Delete item by id={}", id);
    }

    public void deleteBySellerId(Long id) {
        itemRepository.findBySellerId(id).forEach(item -> deleteItem(item.getId()));
    }

    public void deleteItemCard(Long itemCardId) {
        Item item = itemRepository.findByItemCardId(itemCardId);
        item.setItemCard(null);
        itemRepository.save(item);
    }
}
