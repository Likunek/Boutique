package ru.angelika.boutique.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.angelika.boutique.dto.ItemsAtStorageDto;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemsAtStorageService {
    private final ItemsAtStorageRepository itemsAtStorageRepository;
    private final ItemRepository itemRepository;
    private final StorageRepository storageRepository;

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
        ItemsAtStorage itemsAtStorage = new ItemsAtStorage();
        itemsAtStorage.setItem(item);
        itemsAtStorage.setStorage(storage);
        itemsAtStorage.setCount(itemsAtStorageDto.getCount());
        itemsAtStorageRepository.save(itemsAtStorage);
        log.info("Add new itemsAtStorage: itemId={}, storageId={}, count={}",
                itemsAtStorageDto.getItemId(), itemsAtStorageDto.getStorageId(), itemsAtStorageDto.getCount());
    }

    public List<ItemsAtStorage> getByStorageId(Long id) {
        log.debug("Get all itemsAtStorages by storageId={}", id);
        return itemsAtStorageRepository.findByStorageId(id);
    }

    public ItemsAtStorage getByItemAndStorage(Long itemId, Long storageId) {
        log.debug("Get itemsAtStorage by itemId={}, storageId={}", itemId, storageId);
        ItemsAtStorage itemsAtStorage = itemsAtStorageRepository.findByItemIdAndStorageId(itemId, storageId);
        if (itemsAtStorage == null) {
            log.error("ItemsAtStorage not found for get, itemId={}, storageId={}", itemId, storageId);
            throw new ResourceNotFoundException(ItemsAtStorage.class, itemId.toString() + " " + storageId.toString());
        }
        return itemsAtStorage;
    }

    public List<ItemsAtStorage> getAll() {
        log.debug("Get all itemsAtStorages");
        return itemsAtStorageRepository.findAll();
    }

    public ItemsAtStorage get(Long id) {
        return itemsAtStorageRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("ItemsAtStorage not found for update, id={}", id);
                    return new ResourceNotFoundException(ItemsAtStorage.class, id);
                });
    }

    public void updateCount(ItemsAtStorage itemsAtStorage) {
        itemsAtStorageRepository.save(itemsAtStorage);
        log.info("Update count items from itemsAtStorage by id={}", itemsAtStorage.getId());
    }

    public void update(Long id, Long count) {
        ItemsAtStorage itemsAtStorage = get(id);
        if (count == 0) { delete(id);return;}
        itemsAtStorage.setCount(count);
        itemsAtStorageRepository.save(itemsAtStorage);
        log.info("Update itemsAtStorage by id={}, count={}", id, count);
    }

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
