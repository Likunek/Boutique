package ru.angelika.boutique.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.angelika.boutique.dto.ItemsAtStorageDto;
import ru.angelika.boutique.exception.ResourceExistsException;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.Item;
import ru.angelika.boutique.model.ItemsAtStorage;
import ru.angelika.boutique.model.Storage;
import ru.angelika.boutique.repository.ItemRepository;
import ru.angelika.boutique.repository.ItemsAtStorageRepository;
import ru.angelika.boutique.repository.StorageRepository;
import ru.angelika.boutique.service.ItemsAtStorageService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemsAtStorageUnitTest {

    @Mock
    private ItemsAtStorageRepository itemsAtStorageRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private StorageRepository storageRepository;

    @InjectMocks
    private ItemsAtStorageService itemsAtStorageService;

    private static final Long ITEM_ID = 1L;
    private static final Long STORAGE_ID = 2L;
    private static final Long ITEMS_AT_STORAGE_ID = 10L;
    private static final Long FALSE_ID = 99L;
    private static final Long COUNT = 5L;

    private ItemsAtStorageDto dto;
    private Item item;
    private Storage storage;
    private ItemsAtStorage itemsAtStorage;

    @BeforeEach
    void setUp() {
        dto = ItemsAtStorageDto.builder()
                .itemId(ITEM_ID)
                .storageId(STORAGE_ID)
                .count(COUNT)
                .build();

        item = new Item();
        item.setId(ITEM_ID);
        item.setName("Test Item");

        storage = new Storage();
        storage.setId(STORAGE_ID);
        storage.setAddress("Test Address");
        storage.setCity("Test City");

        itemsAtStorage = new ItemsAtStorage();
        itemsAtStorage.setId(ITEMS_AT_STORAGE_ID);
        itemsAtStorage.setItem(item);
        itemsAtStorage.setStorage(storage);
        itemsAtStorage.setCount(COUNT);
    }

    @Test
    void add_Success() {
        when(itemsAtStorageRepository.findByItemIdAndStorageId(ITEM_ID, STORAGE_ID)).thenReturn(null);
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(storageRepository.findById(STORAGE_ID)).thenReturn(Optional.of(storage));
        when(itemsAtStorageRepository.save(any(ItemsAtStorage.class))).thenReturn(itemsAtStorage);

        assertDoesNotThrow(() -> itemsAtStorageService.add(dto));

        verify(itemsAtStorageRepository).save(any(ItemsAtStorage.class));
    }

    @Test
    void add_AlreadyExists_ThrowsException() {
        when(itemsAtStorageRepository.findByItemIdAndStorageId(ITEM_ID, STORAGE_ID))
                .thenReturn(itemsAtStorage);

        assertThrows(ResourceExistsException.class, () -> itemsAtStorageService.add(dto));
        verify(itemsAtStorageRepository, never()).save(any());
    }

    @Test
    void add_ItemNotFound_ThrowsException() {
        when(itemsAtStorageRepository.findByItemIdAndStorageId(ITEM_ID, STORAGE_ID)).thenReturn(null);
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> itemsAtStorageService.add(dto));
        verify(itemsAtStorageRepository, never()).save(any());
    }

    @Test
    void add_StorageNotFound_ThrowsException() {
        when(itemsAtStorageRepository.findByItemIdAndStorageId(ITEM_ID, STORAGE_ID)).thenReturn(null);
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));


        when(storageRepository.findById(STORAGE_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> itemsAtStorageService.add(dto));
        verify(itemsAtStorageRepository, never()).save(any());
    }

    @Test
    void getByStorageId_Success() {
        List<ItemsAtStorage> list = List.of(itemsAtStorage);
        when(itemsAtStorageRepository.findByStorageId(STORAGE_ID)).thenReturn(list);
        List<ItemsAtStorage> result = itemsAtStorageService.getByStorageId(STORAGE_ID);
        assertEquals(1, result.size());
    }

    @Test
    void getByItemAndStorage_Success() {
        when(itemsAtStorageRepository.findByItemIdAndStorageId(ITEM_ID, STORAGE_ID))
                .thenReturn(itemsAtStorage);
        ItemsAtStorage result = itemsAtStorageService.getByItemAndStorage(ITEM_ID, STORAGE_ID);
        assertNotNull(result);
        assertEquals(ITEMS_AT_STORAGE_ID, result.getId());
    }

    @Test
    void getByItemAndStorage_NotFound_ThrowsException() {
        when(itemsAtStorageRepository.findByItemIdAndStorageId(ITEM_ID, STORAGE_ID)).thenReturn(null);
        assertThrows(ResourceNotFoundException.class,
                () -> itemsAtStorageService.getByItemAndStorage(ITEM_ID, STORAGE_ID));
    }

    @Test
    void getAll_Success() {
        List<ItemsAtStorage> list = List.of(itemsAtStorage);
        when(itemsAtStorageRepository.findAll()).thenReturn(list);
        List<ItemsAtStorage> result = itemsAtStorageService.getAll();
        assertEquals(1, result.size());
    }

    @Test
    void get_Success() {
        when(itemsAtStorageRepository.findById(ITEMS_AT_STORAGE_ID)).thenReturn(Optional.of(itemsAtStorage));
        ItemsAtStorage result = itemsAtStorageService.get(ITEMS_AT_STORAGE_ID);
        assertNotNull(result);
        assertEquals(ITEMS_AT_STORAGE_ID, result.getId());
    }

    @Test
    void get_NotFound_ThrowsException() {
        when(itemsAtStorageRepository.findById(FALSE_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> itemsAtStorageService.get(FALSE_ID));
    }

    @Test
    void updateCount_Success() {
        assertDoesNotThrow(() -> itemsAtStorageService.updateCount(itemsAtStorage));
        verify(itemsAtStorageRepository).save(itemsAtStorage);
    }

    @Test
    void update_UpdateCount_Success() {
        when(itemsAtStorageRepository.findById(ITEMS_AT_STORAGE_ID)).thenReturn(Optional.of(itemsAtStorage));

        assertDoesNotThrow(() -> itemsAtStorageService.update(ITEMS_AT_STORAGE_ID, COUNT + 1));

        verify(itemsAtStorageRepository).save(itemsAtStorage);
        assertEquals(COUNT + 1, itemsAtStorage.getCount());
    }

    @Test
    void update_ZeroCount_Deletes() {
        when(itemsAtStorageRepository.findById(ITEMS_AT_STORAGE_ID)).thenReturn(Optional.of(itemsAtStorage));
        when(itemRepository.findItemByItemsAtStorageId(ITEMS_AT_STORAGE_ID)).thenReturn(item);
        doNothing().when(itemsAtStorageRepository).deleteById(ITEMS_AT_STORAGE_ID);

        assertDoesNotThrow(() -> itemsAtStorageService.update(ITEMS_AT_STORAGE_ID, 0L));

        verify(itemsAtStorageRepository).deleteById(ITEMS_AT_STORAGE_ID);
        verify(itemsAtStorageRepository, never()).save(any());
    }

    @Test
    void update_ItemNotFound_ThrowsException() {
        when(itemsAtStorageRepository.findById(FALSE_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> itemsAtStorageService.update(FALSE_ID, COUNT));
    }

    @Test
    void delete_Success() {
        when(itemsAtStorageRepository.findById(ITEMS_AT_STORAGE_ID)).thenReturn(Optional.of(itemsAtStorage));
        when(itemRepository.findItemByItemsAtStorageId(ITEMS_AT_STORAGE_ID)).thenReturn(item);
        doNothing().when(itemsAtStorageRepository).deleteById(ITEMS_AT_STORAGE_ID);

        assertDoesNotThrow(() -> itemsAtStorageService.delete(ITEMS_AT_STORAGE_ID));

        verify(itemRepository).save(item);
        verify(itemsAtStorageRepository).deleteById(ITEMS_AT_STORAGE_ID);
    }

    @Test
    void delete_NotFound_ThrowsException() {
        when(itemsAtStorageRepository.findById(FALSE_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> itemsAtStorageService.delete(FALSE_ID));
        verify(itemsAtStorageRepository, never()).deleteById(any());
    }
}




