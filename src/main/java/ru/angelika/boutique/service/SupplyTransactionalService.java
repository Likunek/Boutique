package ru.angelika.boutique.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.Item;
import ru.angelika.boutique.model.ItemsAtStorage;
import ru.angelika.boutique.model.Order;

import java.util.List;

@Service
public class SupplyTransactionalService {
    private final ItemService itemService;
    private final ItemsAtStorageService itemsAtStorageService;

    @Autowired
    public SupplyTransactionalService(ItemService itemService, ItemsAtStorageService itemsAtStorageService) {
        this.itemService = itemService;
        this.itemsAtStorageService = itemsAtStorageService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Item> checkCountOnStorage(Order order, Long storageId) {
        List<Item> items = order.getItems().stream()
                .map(itemCard -> itemService.getItemByCardId(itemCard.getId())).toList();
        for (Item item : items) {
            ItemsAtStorage itemsAtStorage = itemsAtStorageService
                    .getByItemAndStorage(item.getId(), storageId);
            if (itemsAtStorage.getCount() == 0) {
                throw new ResourceNotFoundException(ItemsAtStorage.class, "items count = 0");
            } else {
                itemsAtStorage.setCount(itemsAtStorage.getCount() - 1);
                itemsAtStorageService.updateCount(itemsAtStorage);
            }
        }
        return items;
    }
}
