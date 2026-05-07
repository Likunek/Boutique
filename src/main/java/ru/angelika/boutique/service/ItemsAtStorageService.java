package ru.angelika.boutique.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.angelika.boutique.dto.ItemsAtStorageDto;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.Item;
import ru.angelika.boutique.model.ItemsAtStorage;
import ru.angelika.boutique.model.Storage;
import ru.angelika.boutique.repository.ItemRepository;
import ru.angelika.boutique.repository.ItemsAtStorageRepository;
import ru.angelika.boutique.repository.StorageRepository;

import java.util.List;

@Slf4j
@Service
public class ItemsAtStorageService {
    private final ItemsAtStorageRepository itemsAtStorageRepository;
    private final ItemRepository itemRepository;
    private final StorageRepository storageRepository;

    @Autowired
    public ItemsAtStorageService(ItemsAtStorageRepository itemsAtStorageRepository, ItemRepository itemRepository, StorageRepository storageRepository) {
        this.itemsAtStorageRepository = itemsAtStorageRepository;
        this.itemRepository = itemRepository;
        this.storageRepository = storageRepository;
    }

    public void addItemsAtStorage(ItemsAtStorageDto itemsAtStorageDto) {
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

    public List<ItemsAtStorage> getItemsAtStorageByStorageId(Long id) {
       return itemsAtStorageRepository.findByStorageId(id);
    }

    public ItemsAtStorage getItemsAtStorage(Long id) {
        return itemsAtStorageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ItemsAtStorage.class, id));
    }

    public void updateItemsAtStorage(Long id, Long count) {
        ItemsAtStorage itemsAtStorage = itemsAtStorageRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("ItemsAtStorage not found for update, id={}", id);
                    return new ResourceNotFoundException(ItemsAtStorage.class, id);
                });
        itemsAtStorage.setCount(count);
        itemsAtStorageRepository.save(itemsAtStorage);
        log.info("Update itemsAtStorage by id={}, count={}", id, count);
    }

    public void deleteItemsAtStorage(Long id) {
        itemsAtStorageRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("ItemsAtStorage not found for delete, id={}", id);
                    return new ResourceNotFoundException(ItemsAtStorage.class, id);
                });
        Item item = itemRepository.findItemByItemsAtStorageId(id);
        item.getItemsAtStorages().removeIf(s -> s.getId().equals(id));
        itemRepository.save(item);
        log.info("Update item by id={}, delete ItemsAtStorage by id={}", item.getId(), id);
        itemsAtStorageRepository.deleteById(id);
        log.info("Delete itemsAtStorage by id={}", id);
    }
}
