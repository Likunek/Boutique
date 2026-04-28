package ru.angelika.boutique.service;

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
                .orElseThrow(() -> new ResourceNotFoundException(Item.class, itemsAtStorageDto.getItemId()));
        Storage storage = storageRepository.findById(itemsAtStorageDto.getStorageId())
                .orElseThrow(() -> new ResourceNotFoundException(Storage.class, itemsAtStorageDto.getStorageId()));
        ItemsAtStorage itemsAtStorage = new ItemsAtStorage();
        itemsAtStorage.setItem(item);
        itemsAtStorage.setStorage(storage);
        itemsAtStorage.setCount(itemsAtStorageDto.getCount());
        itemsAtStorageRepository.save(itemsAtStorage);
    }

    public ItemsAtStorage getItemsAtStorage(Long id) {
        return itemsAtStorageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ItemsAtStorage.class, id));
    }

    public void updateItemsAtStorage(Long id, Long count) {
        ItemsAtStorage itemsAtStorage = itemsAtStorageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ItemsAtStorage.class, id));
        itemsAtStorage.setCount(count);
        itemsAtStorageRepository.save(itemsAtStorage);
    }

    public void deleteItemsAtStorage(Long id) {
        itemsAtStorageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ItemsAtStorage.class, id));
        itemsAtStorageRepository.deleteById(id);
    }
}
