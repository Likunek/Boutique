package ru.angelika.boutique.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.*;
import ru.angelika.boutique.service.ItemService;
import ru.angelika.boutique.service.ItemsAtStorageService;
import ru.angelika.boutique.service.SupplyTransactionalService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplyTransactionalUnitTest {

    @Mock
    private ItemService itemService;

    @Mock
    private ItemsAtStorageService itemsAtStorageService;

    @InjectMocks
    private SupplyTransactionalService supplyTransactionalService;

    private static final Long STORAGE_ID = 1L;
    private static final Long ITEM_CARD_ID = 10L;
    private static final Long ITEM_ID = 12L;
    private static final Long COUNT = 5L;

    private Order order;
    private ItemCard itemCard;
    private Item item;
    private ItemsAtStorage itemsAtStorage;

    @BeforeEach
    void setUp() {
        itemCard = new ItemCard();
        itemCard.setId(ITEM_CARD_ID);

        item = new Item();
        item.setId(ITEM_ID);
        item.setWeight(1.5);

        itemsAtStorage = new ItemsAtStorage();
        itemsAtStorage.setId(1L);
        itemsAtStorage.setItem(item);
        itemsAtStorage.setCount(COUNT);

        order = new Order();
        order.setId(1L);
        order.setItems(List.of(itemCard));
    }

    @Test
    void checkCountOnStorage_Success() {
        when(itemService.getByCardId(ITEM_CARD_ID)).thenReturn(item);
        when(itemsAtStorageService.getByItemAndStorage(ITEM_ID, STORAGE_ID)).thenReturn(itemsAtStorage);
        doNothing().when(itemsAtStorageService).updateCount(any(ItemsAtStorage.class), eq(1));

        List<Item> result = supplyTransactionalService.checkCountOnStorage(order, STORAGE_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ITEM_ID, result.get(0).getId());
        verify(itemsAtStorageService).updateCount(itemsAtStorage, 1);
        assertEquals(COUNT - 1, itemsAtStorage.getCount());
    }

    @Test
    void checkCountOnStorage_ItemCardNotFound_ThrowsException() {
        when(itemService.getByCardId(ITEM_CARD_ID)).thenThrow(new ResourceNotFoundException(Item.class, ITEM_CARD_ID));

        assertThrows(ResourceNotFoundException.class, () -> supplyTransactionalService.checkCountOnStorage(order, STORAGE_ID));
        verify(itemsAtStorageService, never()).getByItemAndStorage(any(), any());
    }

    @Test
    void checkCountOnStorage_ItemsAtStorageNotFound_ThrowsException() {
        when(itemService.getByCardId(ITEM_CARD_ID)).thenReturn(item);
        when(itemsAtStorageService.getByItemAndStorage(ITEM_ID, STORAGE_ID)).thenThrow(new ResourceNotFoundException(ItemsAtStorage.class, ITEM_ID + " " + STORAGE_ID));

        assertThrows(ResourceNotFoundException.class, () -> supplyTransactionalService.checkCountOnStorage(order, STORAGE_ID));
        verify(itemsAtStorageService, never()).updateCount(any(), eq(1));
    }

    @Test
    void checkCountOnStorage_ZeroCount_ThrowsException() {
        itemsAtStorage.setCount(0L);
        when(itemService.getByCardId(ITEM_CARD_ID)).thenReturn(item);
        when(itemsAtStorageService.getByItemAndStorage(ITEM_ID, STORAGE_ID)).thenReturn(itemsAtStorage);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> supplyTransactionalService.checkCountOnStorage(order, STORAGE_ID));
        assertEquals("class ru.angelika.boutique.model.ItemsAtStorage not found with data: items count = 0", exception.getMessage());
        verify(itemsAtStorageService, never()).updateCount(any(), eq(1));
    }

    @Test
    void checkCountOnStorage_MultipleItems_Success() {
        ItemCard itemCard2 = new ItemCard();
        itemCard2.setId(ITEM_CARD_ID + 1);
        Item item2 = new Item();
        item2.setId(ITEM_ID + 1);
        ItemsAtStorage itemsAtStorage2 = new ItemsAtStorage();
        itemsAtStorage2.setCount(3L);

        order.setItems(List.of(itemCard, itemCard2));

        when(itemService.getByCardId(ITEM_CARD_ID)).thenReturn(item);
        when(itemService.getByCardId(ITEM_CARD_ID + 1)).thenReturn(item2);
        when(itemsAtStorageService.getByItemAndStorage(ITEM_ID, STORAGE_ID)).thenReturn(itemsAtStorage);
        when(itemsAtStorageService.getByItemAndStorage(ITEM_ID + 1, STORAGE_ID)).thenReturn(itemsAtStorage2);

        List<Item> result = supplyTransactionalService.checkCountOnStorage(order, STORAGE_ID);

        assertEquals(2, result.size());
        verify(itemsAtStorageService, times(2)).updateCount(any(), eq(1));
        assertEquals(COUNT - 1, itemsAtStorage.getCount());
        assertEquals(2L, itemsAtStorage2.getCount());
    }
}