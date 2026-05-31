package ru.angelika.boutique.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.Item;
import ru.angelika.boutique.model.ItemsAtStorage;
import ru.angelika.boutique.model.Order;

import java.util.List;

/**
 * Вспомогательный сервис для операций с поставками, требующих отдельной транзакции.
 * Используется внутри SupplyService для проверки остатков и их списания.
 */
@Service
@RequiredArgsConstructor
public class SupplyTransactionalService {
    private final ItemService itemService;
    private final ItemsAtStorageService itemsAtStorageService;

    /**
     * Проверяет наличие каждого товара из заказа на указанном складе,
     * уменьшает количество на 1 для каждого товара и возвращает список товаров.
     * Выполняется в новой транзакции (REQUIRES_NEW), чтобы изменения фиксировались
     * даже при откате основной транзакции формирования поставки.
     *
     * @param order     заказ (уже с загруженными ItemCard)
     * @param storageId ID склада
     * @return список физических товаров (Item), соответствующих карточкам заказа
     * @throws ResourceNotFoundException если для какого-то товара остаток на складе равен 0
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Item> checkCountOnStorage(Order order, Long storageId) {
        List<Item> items = order.getItems().stream()
                .map(itemCard -> itemService.getByCardId(itemCard.getId())).toList();
        for (Item item : items) {
            ItemsAtStorage itemsAtStorage = itemsAtStorageService
                    .getByItemAndStorage(item.getId(), storageId);
            if (itemsAtStorage.getCount() == 0) {
                throw new ResourceNotFoundException(ItemsAtStorage.class, "items count = 0");
            } else {
                itemsAtStorage.setCount(itemsAtStorage.getCount() - 1);
                itemsAtStorageService.updateCount(itemsAtStorage, 1);
            }
        }
        return items;
    }
}