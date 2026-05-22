package ru.angelika.boutique.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.angelika.boutique.dto.StorageDto;
import ru.angelika.boutique.exception.ResourceExistsException;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.ItemsAtStorage;
import ru.angelika.boutique.model.PickupPoint;
import ru.angelika.boutique.model.Storage;
import ru.angelika.boutique.repository.StorageRepository;
import ru.angelika.boutique.service.ItemsAtStorageService;
import ru.angelika.boutique.service.PickupPointService;
import ru.angelika.boutique.service.StorageService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageUnitTest {

    @Mock
    private StorageRepository storageRepository;

    @Mock
    private ItemsAtStorageService itemsAtStorageService;

    @Mock
    private PickupPointService pickupPointService;

    @InjectMocks
    private StorageService storageService;

    private static final Long STORAGE_ID = 1L;
    private static final Long OTHER_STORAGE_ID = 2L;
    private static final Long FALSE_ID = 99L;
    private static final String ADDRESS = "Test Address";
    private static final String CITY = "Test City";
    private static final String NEW_ADDRESS = "New Address";
    private static final String NEW_CITY = "New City";
    private static final Long MAX_CAPACITY = 1000L;

    private StorageDto storageDto;
    private Storage storage;
    private Storage otherStorage;

    @BeforeEach
    void setUp() {
        storageDto = StorageDto.builder()
                .address(ADDRESS)
                .city(CITY)
                .maxCapacity(MAX_CAPACITY)
                .build();

        storage = new Storage();
        storage.setId(STORAGE_ID);
        storage.setAddress(ADDRESS);
        storage.setCity(CITY);
        storage.setMaxCapacity(MAX_CAPACITY);

        otherStorage = new Storage();
        otherStorage.setId(OTHER_STORAGE_ID);
        otherStorage.setAddress(NEW_ADDRESS);
        otherStorage.setCity(CITY);
        otherStorage.setMaxCapacity(2000L);
    }

    @Test
    void add_Success() {
        when(storageRepository.findByAddress(ADDRESS)).thenReturn(new ArrayList<>());
        when(storageRepository.findByCity(CITY)).thenReturn(new ArrayList<>());

        assertDoesNotThrow(() -> storageService.add(storageDto));

        verify(storageRepository).save(any(Storage.class));
    }

    @Test
    void add_Duplicate_ThrowsException() {
        when(storageRepository.findByAddress(ADDRESS)).thenReturn(new ArrayList<>(List.of(storage)));
        when(storageRepository.findByCity(CITY)).thenReturn(new ArrayList<>(List.of(storage)));
        assertThrows(ResourceExistsException.class, () -> storageService.add(storageDto));
        verify(storageRepository, never()).save(any());
    }

    @Test
    void get_Success() {
        when(storageRepository.findById(STORAGE_ID)).thenReturn(Optional.of(storage));
        Storage result = storageService.get(STORAGE_ID);
        assertNotNull(result);
        assertEquals(STORAGE_ID, result.getId());
    }

    @Test
    void get_NotFound_ThrowsException() {
        when(storageRepository.findById(FALSE_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> storageService.get(FALSE_ID));
    }

    @Test
    void getAll_Success() {
        when(storageRepository.findAll()).thenReturn(new ArrayList<>(List.of(storage)));
        List<Storage> result = storageService.getAll();
        assertEquals(1, result.size());
    }

    @Test
    void update_Success() {
        StorageDto updateDto = StorageDto.builder()
                .address(NEW_ADDRESS)
                .city(NEW_CITY)
                .maxCapacity(2000L)
                .build();

        when(storageRepository.findById(STORAGE_ID)).thenReturn(Optional.of(storage));
        when(storageRepository.findByAddress(NEW_ADDRESS)).thenReturn(new ArrayList<>());
        when(storageRepository.findByCity(NEW_CITY)).thenReturn(new ArrayList<>());
        when(storageRepository.save(any(Storage.class))).thenReturn(storage);

        assertDoesNotThrow(() -> storageService.update(updateDto, STORAGE_ID));

        verify(storageRepository).save(any(Storage.class));
    }

    @Test
    void update_StorageNotFound_ThrowsException() {
        when(storageRepository.findById(FALSE_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> storageService.update(storageDto, FALSE_ID));
        verify(storageRepository, never()).save(any());
    }

    @Test
    void update_Duplicate_ThrowsException() {
        StorageDto updateDto = StorageDto.builder()
                .address(ADDRESS)
                .city(CITY)
                .maxCapacity(2000L)
                .build();
        Storage existingStorage = new Storage();
        existingStorage.setId(OTHER_STORAGE_ID);
        existingStorage.setAddress(ADDRESS);
        existingStorage.setCity(CITY);

        when(storageRepository.findById(STORAGE_ID)).thenReturn(Optional.of(storage));
        when(storageRepository.findByAddress(ADDRESS)).thenReturn(new ArrayList<>(List.of(existingStorage)));
        when(storageRepository.findByCity(CITY)).thenReturn(new ArrayList<>(List.of(existingStorage)));

        assertThrows(ResourceExistsException.class, () -> storageService.update(updateDto, STORAGE_ID));
        verify(storageRepository, never()).save(any());
    }

    @Test
    void delete_Success() {
        ItemsAtStorage item = new ItemsAtStorage();
        item.setId(100L);
        PickupPoint point = new PickupPoint();
        point.setId(32L);
        point.setCity(CITY);
        when(storageRepository.findById(STORAGE_ID)).thenReturn(Optional.of(storage));
        when(itemsAtStorageService.getByStorageId(STORAGE_ID)).thenReturn(new ArrayList<>(List.of(item)));
        doNothing().when(itemsAtStorageService).delete(100L);
        when(pickupPointService.getAllByStorage(STORAGE_ID)).thenReturn(new ArrayList<>(List.of(point)));
        when(storageRepository.findByCity(point.getCity())).thenReturn(new ArrayList<>(List.of(otherStorage)));
        doNothing().when(pickupPointService).updateStorage(any(PickupPoint.class));
        doNothing().when(storageRepository).deleteById(STORAGE_ID);

        assertDoesNotThrow(() -> storageService.delete(STORAGE_ID));

        assertEquals(otherStorage.getId(), point.getStorage().getId());
        verify(itemsAtStorageService).delete(item.getId());
        verify(pickupPointService).getAllByStorage(STORAGE_ID);
        verify(storageRepository).findByCity(point.getCity());
        verify(pickupPointService).updateStorage(any(PickupPoint.class));
        verify(pickupPointService, never()).delete(any());
        verify(storageRepository).deleteById(STORAGE_ID);
    }

    @Test
    void delete_ReplaceStorage_StorageByCityPointEqualsCurrentStorage_Success() {
        ItemsAtStorage item = new ItemsAtStorage();
        item.setId(100L);
        PickupPoint point = new PickupPoint();
        point.setId(32L);
        point.setCity(CITY);
        when(storageRepository.findById(STORAGE_ID)).thenReturn(Optional.of(storage));
        when(itemsAtStorageService.getByStorageId(STORAGE_ID)).thenReturn(new ArrayList<>(List.of(item)));
        doNothing().when(itemsAtStorageService).delete(100L);
        when(pickupPointService.getAllByStorage(STORAGE_ID)).thenReturn(new ArrayList<>(List.of(point)));
        when(storageRepository.findByCity(point.getCity())).thenReturn(new ArrayList<>(List.of(storage)));
        doNothing().when(pickupPointService).delete(point.getId());
        doNothing().when(storageRepository).deleteById(STORAGE_ID);

        assertDoesNotThrow(() -> storageService.delete(STORAGE_ID));

        verify(itemsAtStorageService).delete(item.getId());
        verify(pickupPointService).getAllByStorage(STORAGE_ID);
        verify(storageRepository).findByCity(point.getCity());
        verify(pickupPointService, never()).updateStorage(any(PickupPoint.class));
        verify(pickupPointService).delete(point.getId());
        verify(storageRepository).deleteById(STORAGE_ID);
    }

    @Test
    void delete_ReplaceStorage_StorageListEmpty_Success() {
        ItemsAtStorage item = new ItemsAtStorage();
        item.setId(100L);
        PickupPoint point = new PickupPoint();
        point.setId(32L);
        point.setCity(CITY);
        when(storageRepository.findById(STORAGE_ID)).thenReturn(Optional.of(storage));
        when(itemsAtStorageService.getByStorageId(STORAGE_ID)).thenReturn(new ArrayList<>(List.of(item)));
        doNothing().when(itemsAtStorageService).delete(100L);
        when(pickupPointService.getAllByStorage(STORAGE_ID)).thenReturn(new ArrayList<>(List.of(point)));
        when(storageRepository.findByCity(point.getCity())).thenReturn(new ArrayList<>());
        doNothing().when(pickupPointService).delete(point.getId());
        doNothing().when(storageRepository).deleteById(STORAGE_ID);

        assertDoesNotThrow(() -> storageService.delete(STORAGE_ID));

        verify(itemsAtStorageService).delete(item.getId());
        verify(pickupPointService).getAllByStorage(STORAGE_ID);
        verify(storageRepository).findByCity(point.getCity());
        verify(pickupPointService, never()).updateStorage(any(PickupPoint.class));
        verify(pickupPointService).delete(point.getId());
        verify(storageRepository).deleteById(STORAGE_ID);
    }

    @Test
    void delete_ReplaceStorage_PointListEmpty_Success() {
        ItemsAtStorage item = new ItemsAtStorage();
        item.setId(100L);

        when(storageRepository.findById(STORAGE_ID)).thenReturn(Optional.of(storage));
        when(itemsAtStorageService.getByStorageId(STORAGE_ID)).thenReturn(new ArrayList<>(List.of(item)));
        doNothing().when(itemsAtStorageService).delete(100L);
        when(pickupPointService.getAllByStorage(STORAGE_ID)).thenReturn(new ArrayList<>());
        doNothing().when(storageRepository).deleteById(STORAGE_ID);

        assertDoesNotThrow(() -> storageService.delete(STORAGE_ID));

        verify(itemsAtStorageService).delete(item.getId());
        verify(pickupPointService).getAllByStorage(STORAGE_ID);
        verify(storageRepository, never()).findByCity(any());
        verify(pickupPointService, never()).updateStorage(any(PickupPoint.class));
        verify(storageRepository).deleteById(STORAGE_ID);
    }

    @Test
    void delete_ItemsAtStorageListEmpty_Success() {
        ItemsAtStorage item = new ItemsAtStorage();
        item.setId(100L);

        when(storageRepository.findById(STORAGE_ID)).thenReturn(Optional.of(storage));
        when(itemsAtStorageService.getByStorageId(STORAGE_ID)).thenReturn(new ArrayList<>());
        when(pickupPointService.getAllByStorage(STORAGE_ID)).thenReturn(new ArrayList<>());
        doNothing().when(storageRepository).deleteById(STORAGE_ID);

        assertDoesNotThrow(() -> storageService.delete(STORAGE_ID));

        verify(itemsAtStorageService, never()).delete(item.getId());
        verify(pickupPointService).getAllByStorage(STORAGE_ID);
        verify(storageRepository, never()).findByCity(any());
        verify(pickupPointService, never()).updateStorage(any(PickupPoint.class));
        verify(storageRepository).deleteById(STORAGE_ID);
    }
    @Test
    void delete_StorageNotFound_ThrowsException() {
        when(storageRepository.findById(FALSE_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> storageService.delete(FALSE_ID));
        verify(storageRepository, never()).deleteById(any());
    }
}