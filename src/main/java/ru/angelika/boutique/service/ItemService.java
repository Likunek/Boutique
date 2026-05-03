package ru.angelika.boutique.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    }

    public List<Item> getItemBySellerId(Long id) {
        return itemRepository.findBySellerIdAndVerifyTrue(id);
    }
    public List<Item> getAllItemBySeller( Seller seller) {
        return itemRepository.findBySellerWithStorages(seller);
    }

    public Item getItemById(Long id) {
       return itemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Item.class, id));
    }

    public void addCardItem(Item item) {
        itemRepository.save(item);
    }

    public void updateItem(ItemDto itemDto, Long id) {
        Item item =itemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Item.class, id));
        ItemMapper.toItemUpdate(itemDto, item);
        if (item.getItemCard() != null) {
            ItemCard itemCard = itemCardRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(ItemCard.class, id));
            itemCard.setPrice(itemDto.getCostPrice()*1.2);
            itemCardRepository.save(itemCard);
        }
        itemRepository.save(item);
    }

    public void deleteItem(Long id) {
        itemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Item.class, id));
        itemRepository.deleteById(id);
    }
}
