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

/**
 * Сервис для управления физическими товарами (Item).
 * Позволяет добавлять, обновлять, удалять товары, а также управлять верификацией и привязкой карточек.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final ItemCardRepository itemCardRepository;

    /**
     * Добавляет новый товар.
     *
     * @param itemDto DTO с данными товара
     * @param seller  продавец, которому принадлежит товар
     * @throws ResourceExistsException если у этого продавца уже есть товар с таким именем
     */
    public void add(ItemDto itemDto, Seller seller) {
        checkDuplicate(seller.getId(), itemDto.getName(), null);
        itemRepository.save(ItemMapper.toItem(itemDto, seller));
        log.info("Add new item: name={}, price={}, weight={}, square={}, sellerId={}",
                itemDto.getName(), itemDto.getCostPrice(), itemDto.getWeight(), itemDto.getSquare(), seller.getId());
    }

    /**
     * Возвращает все товары.
     *
     * @return список всех товаров
     */
    public List<Item> getAll() {
        return itemRepository.findAll();
    }

    /**
     * Возвращает список товаров, ожидающих верификации (verify = false).
     *
     * @return список непроверенных товаров
     */
    public List<Item> getAllVerifyFalse() {
        return itemRepository.findByVerifyFalse();
    }

    /**
     * Возвращает проверенные товары продавца, у которых ещё нет товарной карточки.
     *
     * @param id ID продавца
     * @return список товаров
     */
    public List<Item> getBySellerIdItemCardNull(Long id) {
        return itemRepository.findBySellerIdAndVerifyTrueAndItemCardIsNull(id);
    }

    /**
     * Возвращает все проверенные товары продавца (с карточками или без).
     *
     * @param id ID продавца
     * @return список товаров
     */
    public List<Item> getBySellerId(Long id) {
        return itemRepository.findBySellerIdAndVerifyTrue(id);
    }

    /**
     * Возвращает товары продавца с предварительной загрузкой остатков на складах.
     *
     * @param seller продавец
     * @return список товаров с инициализированным полем itemsAtStorages
     */
    public List<Item> getAllBySellerWithStorages(Seller seller) {
        return itemRepository.findBySellerWithStorages(seller);
    }

    /**
     * Находит товар по ID.
     *
     * @param id ID товара
     * @return найденный товар
     * @throws ResourceNotFoundException если товар не найден
     */
    public Item getById(Long id) {
        log.debug("Get item by id={}", id);
        return itemRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Item not found for get, id={}", id);
                    return new ResourceNotFoundException(Item.class, id);
                });
    }

    /**
     * Находит товар по ID его товарной карточки.
     *
     * @param id ID товарной карточки
     * @return найденный товар
     * @throws ResourceNotFoundException если товар не найден
     */
    public Item getByCardId(Long id) {
        Item item = itemRepository.findByItemCardId(id);
        if (item == null) {
            log.error("Item not found for get, id={}", id);
            throw new ResourceNotFoundException(Item.class, id);
        }
        return item;
    }

    /**
     * Сохраняет связь товара с его карточкой (используется при создании карточки).
     *
     * @param item товар, у которого уже установлено поле itemCard
     */
    public void addItemCard(Item item) {
        log.info("Update Item's ItemCard field: itemId={}, itemCardId={}",
                item.getId(), item.getItemCard().getId());
        itemRepository.save(item);
    }

    /**
     * Обновляет статус верификации товара.
     *
     * @param id     ID товара
     * @param verify новое значение verify (true – проверен)
     * @throws ResourceNotFoundException если товар не найден
     */
    public void updateVerify(Long id, boolean verify) {
        Item item = getById(id);
        item.setVerify(verify);
        itemRepository.save(item);
        log.info("Update verify Item: itemId={}, verify={}", item.getId(), verify);
    }

    /**
     * Обновляет данные товара (название, стоимость, вес, площадь).
     * Если у товара есть карточка, её цена пересчитывается (стоимость * 1.2).
     *
     * @param itemDto  DTO с новыми данными
     * @param id       ID товара
     * @param sellerId ID продавца (для проверки дубликатов)
     * @throws ResourceExistsException   если у продавца уже есть товар с таким именем
     * @throws ResourceNotFoundException если товар не найден
     */
    public void update(ItemDto itemDto, Long id, Long sellerId) {
        checkDuplicate(sellerId, itemDto.getName(), id);
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Item not found for update, id={}", id);
                    return new ResourceNotFoundException(Item.class, id);
                });
        ItemMapper.toItemUpdate(itemDto, item);
        if (item.getItemCard() != null) {
            ItemCard itemCard = itemCardRepository.findById(item.getItemCard().getId())
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

    /**
     * Удаляет товар по ID.
     *
     * @param id ID товара
     * @throws ResourceNotFoundException если товар не найден
     */
    public void delete(Long id) {
        getById(id);
        itemRepository.deleteById(id);
        log.info("Delete item by id={}", id);
    }

    /**
     * Удаляет все товары продавца (вызывается при удалении продавца).
     *
     * @param id ID продавца
     */
    public void deleteBySellerId(Long id) {
        itemRepository.findBySellerId(id).forEach(item -> delete(item.getId()));
    }

    /**
     * Разрывает связь товара с его карточкой (при удалении карточки).
     *
     * @param itemCardId ID карточки
     * @throws ResourceNotFoundException если товар по карточке не найден
     */
    public void deleteItemCard(Long itemCardId) {
        Item item = getByCardId(itemCardId);
        item.setItemCard(null);
        itemRepository.save(item);
        log.info("Delete item's itemCard. itemId={}", item.getId());
    }

    /**
     * Проверяет, нет ли у продавца другого товара с таким же именем.
     *
     * @param sellerId ID продавца
     * @param name     название товара
     * @param id       ID текущего товара (при обновлении – исключить себя)
     * @throws ResourceExistsException если дубликат найден
     */
    private void checkDuplicate(Long sellerId, String name, Long id) {
        Item item = itemRepository.findBySellerIdAndName(sellerId, name);
        if (item != null && !item.getId().equals(id)) {
            log.error("Item by sellerId={}, with name={} already exists", sellerId, name);
            throw new ResourceExistsException(Item.class, sellerId + " : " + name);
        }
    }
}