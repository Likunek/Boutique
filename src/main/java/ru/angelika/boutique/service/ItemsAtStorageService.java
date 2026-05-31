package ru.angelika.boutique.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.angelika.boutique.dto.ItemsAtStorageDto;
import ru.angelika.boutique.exception.NotEnoughSpaceException;
import ru.angelika.boutique.exception.ResourceExistsException;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.Item;
import ru.angelika.boutique.model.ItemCard;
import ru.angelika.boutique.model.ItemsAtStorage;
import ru.angelika.boutique.model.Storage;
import ru.angelika.boutique.repository.ItemRepository;
import ru.angelika.boutique.repository.ItemsAtStorageRepository;
import ru.angelika.boutique.repository.StorageRepository;

import java.util.List;

/**
 * Сервис для управления остатками товаров на складах (ItemsAtStorage).
 * Позволяет добавлять ItemsAtStorage, обновлять количество, удалять записи, получать остатки по складу или товару.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemsAtStorageService {
    private final ItemsAtStorageRepository itemsAtStorageRepository;
    private final ItemRepository itemRepository;
    private final StorageRepository storageRepository;

    /**
     * Создаёт новую запись об остатке товара на складе.
     * Проверяет достаточно ли места на складе (с учётом площади товара).
     *
     * @param itemsAtStorageDto DTO с itemId, storageId, count
     * @throws ResourceExistsException   если запись уже существует
     * @throws ResourceNotFoundException если товар или склад не найдены
     * @throws NotEnoughSpaceException   если на складе недостаточно места
     */
    public void add(ItemsAtStorageDto itemsAtStorageDto) {
        if (itemsAtStorageRepository.findByItemIdAndStorageId(itemsAtStorageDto.getItemId(),
                itemsAtStorageDto.getStorageId()) != null) {
            log.error("ItemsAtStorage  with itemId={}, storageId={} already exists",
                    itemsAtStorageDto.getItemId(), itemsAtStorageDto.getStorageId());
            throw new ResourceExistsException(ItemCard.class,
                    itemsAtStorageDto.getItemId() + " , " + itemsAtStorageDto.getStorageId());
        }
        Item item = itemRepository.findById(itemsAtStorageDto.getItemId())
                .orElseThrow(() -> {
                    log.error("Item not found for adding itemsAtStorage, id={}", itemsAtStorageDto.getItemId());
                    return new ResourceNotFoundException(Item.class, itemsAtStorageDto.getItemId());
                });
        Storage storage = storageRepository.findById(itemsAtStorageDto.getStorageId())
                .orElseThrow(() -> {
                    log.error("Storage not found for adding itemsAtStorage, id={}", itemsAtStorageDto.getStorageId());
                    return new ResourceNotFoundException(Storage.class, itemsAtStorageDto.getStorageId());
                });
        double capacity = storage.getCurrentCapacity() - item.getSquare() * itemsAtStorageDto.getCount();
        if (capacity < 0) {
            throw new NotEnoughSpaceException("There is not enough space in warehouse at moment.");
        }
        storage.setCurrentCapacity(capacity);
        ItemsAtStorage itemsAtStorage = new ItemsAtStorage();
        itemsAtStorage.setItem(item);
        itemsAtStorage.setStorage(storage);
        itemsAtStorage.setCount(itemsAtStorageDto.getCount());
        itemsAtStorageRepository.save(itemsAtStorage);
        storageRepository.save(storage);
        log.info("Add new itemsAtStorage: itemId={}, storageId={}, count={}",
                itemsAtStorageDto.getItemId(), itemsAtStorageDto.getStorageId(), itemsAtStorageDto.getCount());
    }

    /**
     * Возвращает все остатки на указанном складе.
     *
     * @param id ID склада
     * @return список ItemsAtStorage
     */
    public List<ItemsAtStorage> getByStorageId(Long id) {
        log.debug("Get all itemsAtStorages by storageId={}", id);
        return itemsAtStorageRepository.findByStorageId(id);
    }

    /**
     * Находит запись об остатке по товару и складу.
     *
     * @param itemId    ID товара
     * @param storageId ID склада
     * @return найденная запись
     * @throws ResourceNotFoundException если запись не найдена
     */
    public ItemsAtStorage getByItemAndStorage(Long itemId, Long storageId) {
        log.debug("Get itemsAtStorage by itemId={}, storageId={}", itemId, storageId);
        ItemsAtStorage itemsAtStorage = itemsAtStorageRepository.findByItemIdAndStorageId(itemId, storageId);
        if (itemsAtStorage == null) {
            log.error("ItemsAtStorage not found for get, itemId={}, storageId={}", itemId, storageId);
            throw new ResourceNotFoundException(ItemsAtStorage.class,
                    "itemId: " + itemId.toString() + " storageId: " + storageId.toString());
        }
        return itemsAtStorage;
    }

    /**
     * Возвращает все записи об остатках.
     *
     * @return список всех ItemsAtStorage
     */
    public List<ItemsAtStorage> getAll() {
        log.debug("Get all itemsAtStorages");
        return itemsAtStorageRepository.findAll();
    }

    /**
     * Находит запись об остатке по её ID.
     *
     * @param id ID записи
     * @return найденная запись
     * @throws ResourceNotFoundException если запись не найдена
     */
    public ItemsAtStorage get(Long id) {
        return itemsAtStorageRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("ItemsAtStorage not found for update, id={}", id);
                    return new ResourceNotFoundException(ItemsAtStorage.class, id);
                });
    }

    /**
     * Обновляет количество в существующей записи.
     * Увеличивает свободное место на складе
     * @param itemsAtStorage запись с новым количеством (должна быть сохранена ранее)
     */
    public void updateCount(ItemsAtStorage itemsAtStorage, int count) {
        Storage storage = itemsAtStorage.getStorage();
        Item item = itemsAtStorage.getItem();
        storage.setCurrentCapacity(storage.getCurrentCapacity() + item.getSquare() * count);
        itemsAtStorageRepository.save(itemsAtStorage);
        storageRepository.save(storage);
        log.info("Update count items from itemsAtStorage by id={}", itemsAtStorage.getId());
    }

    /**
     * Обновляет количество товара в записи остатка.
     * Если count = 0, запись удаляется.
     *
     * @param id    ID записи
     * @param count новое количество
     * @throws ResourceNotFoundException если запись не найдена
     */
    public void update(Long id, Long count) {
        ItemsAtStorage itemsAtStorage = get(id);
        if (count == 0) {
            delete(id);
            return;
        }
        itemsAtStorage.setCount(count);
        itemsAtStorageRepository.save(itemsAtStorage);
        log.info("Update itemsAtStorage by id={}, count={}", id, count);
    }

    /**
     * Удаляет запись об остатке, а также удаляет ссылку на неё из списка itemsAtStorages у товара.
     *
     * @param id ID записи
     * @throws ResourceNotFoundException если запись не найдена
     */
    public void delete(Long id) {
        get(id);
        Item item = itemRepository.findItemByItemsAtStorageId(id);
        item.getItemsAtStorages().removeIf(s -> s.getId().equals(id));
        itemRepository.save(item);
        log.info("Update item by id={}, delete ItemsAtStorage by id={}", item.getId(), id);
        itemsAtStorageRepository.deleteById(id);
        log.info("Delete itemsAtStorage by id={}", id);
    }
}