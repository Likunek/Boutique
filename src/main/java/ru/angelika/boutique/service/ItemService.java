package ru.angelika.boutique.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.angelika.boutique.dto.ItemDto;
import ru.angelika.boutique.exception.ResourceExistsException;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.mapper.ItemMapper;
import ru.angelika.boutique.model.Item;
import ru.angelika.boutique.model.ItemCard;
import ru.angelika.boutique.model.Seller;
import ru.angelika.boutique.repository.ItemCardRepository;
import ru.angelika.boutique.repository.ItemRepository;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final ItemCardRepository itemCardRepository;

    public void add(ItemDto itemDto, Seller seller) {
        checkDuplicate(seller.getId(), itemDto.getName(), null);
        itemRepository.save(ItemMapper.toItem(itemDto, seller));
        log.info("Add new item: name={}, price={}, weight={}, square ={}, sellerId={}",
                itemDto.getName(), itemDto.getCostPrice(), itemDto.getWeight(), itemDto.getSquare(), seller.getId());
    }

    public List<Item> getAll() {
        return itemRepository.findAll();
    }

    public List<Item> getAllVerifyFalse() {
        return itemRepository.findByVerifyFalse();
    }

    public List<Item> getBySellerIdItemCardNull(Long id) {
        return itemRepository.findBySellerIdAndVerifyTrueAndItemCardIsNull(id);
    }

    public List<Item> getBySellerId(Long id) {
        return itemRepository.findBySellerIdAndVerifyTrue(id);
    }

    public List<Item> getAllBySellerWithStorages(Seller seller) {
        return itemRepository.findBySellerWithStorages(seller);
    }

    public Item getById(Long id) {
        log.debug("Get item by id={}", id);
        return itemRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Item not found for get, id={}", id);
                    return new ResourceNotFoundException(Item.class, id);
                });
    }

    public Item getByCardId(Long id) {
        Item item = itemRepository.findByItemCardId(id);
         if (item == null) {
             log.error("Item not found for get, id={}", id);
             throw  new ResourceNotFoundException(Item.class, id);
         }
         return item;
    }

    public void addItemCard(Item item) {
        log.info("Update Item's ItemCard field: itemId={}, itemCardId={}",
                item.getId(), item.getItemCard().getId());
        itemRepository.save(item);
    }

    public void updateVerify(Long id, boolean verify) {
        Item item = getById(id);
        item.setVerify(verify);
        itemRepository.save(item);
        log.info("Update verify Item: itemId={}, verify={}",
                item.getId(), verify);
    }

    public void update(ItemDto itemDto, Long id, Long sellerId) {
        checkDuplicate(sellerId, itemDto.getName(), id);
        Item item = itemRepository.findById(id)
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
            itemCard.setPrice(itemDto.getCostPrice() * 1.2);
            itemCardRepository.save(itemCard);
            log.debug("Update price itemCard by id={}", itemCard.getId());
        }
        itemRepository.save(item);
        log.info("Update item by id={}", id);
    }

    public void delete(Long id) {
        getById(id);
        itemRepository.deleteById(id);
        log.info("Delete item by id={}", id);
    }

    public void deleteBySellerId(Long id) {
        itemRepository.findBySellerId(id).forEach(item -> delete(item.getId()));
    }

    public void deleteItemCard(Long itemCardId) {
        Item item = getByCardId(itemCardId);
        item.setItemCard(null);
        itemRepository.save(item);
        log.info("Delete item's itemCard. itemId={}", item.getId());
    }

    private void checkDuplicate(Long sellerId, String name, Long id) {
        Item item = itemRepository.findBySellerIdAndName(sellerId, name);
        if (item != null && !item.getId().equals(id)) {
            log.error("Item by sellerId={}, with name={} already exists", sellerId, name);
            throw new ResourceExistsException(Item.class, sellerId + " : " + name);
        }
    }

}
