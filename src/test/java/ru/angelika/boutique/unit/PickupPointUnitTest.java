package ru.angelika.boutique.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.angelika.boutique.dto.PickupPointDto;
import ru.angelika.boutique.exception.ResourceExistsException;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.PickupPoint;
import ru.angelika.boutique.model.Storage;
import ru.angelika.boutique.repository.OrderRepository;
import ru.angelika.boutique.repository.PickupPointRepository;
import ru.angelika.boutique.repository.StorageRepository;
import ru.angelika.boutique.service.PickupPointService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PickupPointUnitTest {

    @Mock
    private PickupPointRepository pickupPointRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private StorageRepository storageRepository;

    @InjectMocks
    private PickupPointService pickupPointService;

    private static final Long POINT_ID = 1L;
    private static final Long STORAGE_ID = 10L;
    private static final Long FALSE_ID = 99L;
    private static final String ADDRESS = "Test Address";
    private static final String CITY = "Test City";
    private static final String NEW_ADDRESS = "New Address";
    private static final String NEW_CITY = "New City";

    private PickupPointDto dto;
    private PickupPoint pickupPoint;
    private Storage storage;

    @BeforeEach
    void setUp() {
        dto = PickupPointDto.builder()
                .address(ADDRESS)
                .city(CITY)
                .storageId(STORAGE_ID)
                .build();

        storage = new Storage();
        storage.setId(STORAGE_ID);
        storage.setAddress("Storage Address");
        storage.setCity("Storage City");

        pickupPoint = new PickupPoint();
        pickupPoint.setId(POINT_ID);
        pickupPoint.setAddress(ADDRESS);
        pickupPoint.setCity(CITY);
        pickupPoint.setStorage(storage);
    }

    @Test
    void add_Success() {
        when(storageRepository.findById(STORAGE_ID)).thenReturn(Optional.of(storage));
        when(pickupPointRepository.findByAddress(ADDRESS)).thenReturn(new ArrayList<>());
        when(pickupPointRepository.findByCity(CITY)).thenReturn(new ArrayList<>());

        assertDoesNotThrow(() -> pickupPointService.add(dto));

        verify(pickupPointRepository).save(any(PickupPoint.class));
    }

    @Test
    void add_Duplicate_ThrowsException() {
        when(pickupPointRepository.findByAddress(ADDRESS)).thenReturn(new ArrayList<>(List.of(pickupPoint)));
        when(pickupPointRepository.findByCity(CITY)).thenReturn(new ArrayList<>(List.of(pickupPoint)));

        assertThrows(ResourceExistsException.class, () -> pickupPointService.add(dto));
        verify(pickupPointRepository, never()).save(any());
    }

    @Test
    void add_StorageNotFound_ThrowsException() {
        when(storageRepository.findById(STORAGE_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> pickupPointService.add(dto));
        verify(pickupPointRepository, never()).save(any());
    }

    @Test
    void get_Success() {
        when(pickupPointRepository.findById(POINT_ID)).thenReturn(Optional.of(pickupPoint));
        PickupPoint result = pickupPointService.get(POINT_ID);
        assertNotNull(result);
        assertEquals(POINT_ID, result.getId());
    }

    @Test
    void get_NotFound_ThrowsException() {
        when(pickupPointRepository.findById(FALSE_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> pickupPointService.get(FALSE_ID));
    }

    @Test
    void getWithStorage_Success() {
        when(pickupPointRepository.findByIdWithStorage(POINT_ID)).thenReturn(pickupPoint);
        PickupPoint result = pickupPointService.getWithStorage(POINT_ID);
        assertNotNull(result);
        assertEquals(POINT_ID, result.getId());
    }

    @Test
    void getWithStorage_NotFound_ThrowsException() {
        when(pickupPointRepository.findByIdWithStorage(FALSE_ID)).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> pickupPointService.getWithStorage(FALSE_ID));
    }

    @Test
    void getAllByStorage_Success() {
        when(pickupPointRepository.findByStorageId(STORAGE_ID)).thenReturn(List.of(pickupPoint));
        List<PickupPoint> result = pickupPointService.getAllByStorage(STORAGE_ID);
        assertEquals(1, result.size());
    }

    @Test
    void getAll_Success() {
        when(pickupPointRepository.findAll()).thenReturn(List.of(pickupPoint));
        List<PickupPoint> result = pickupPointService.getAll();
        assertEquals(1, result.size());
    }
    @Test
    void update_Success() {
        PickupPointDto updateDto = PickupPointDto.builder()
                .address(NEW_ADDRESS)
                .city(NEW_CITY)
                .storageId(STORAGE_ID)
                .build();

        when(pickupPointRepository.findById(POINT_ID)).thenReturn(Optional.of(pickupPoint));
        when(storageRepository.findById(STORAGE_ID)).thenReturn(Optional.of(storage));
        when(pickupPointRepository.findByAddress(NEW_ADDRESS)).thenReturn(new ArrayList<>());
        when(pickupPointRepository.findByCity(NEW_CITY)).thenReturn(new ArrayList<>());

        assertDoesNotThrow(() -> pickupPointService.update(POINT_ID, updateDto));

        verify(pickupPointRepository).save(any(PickupPoint.class));
    }

    @Test
    void update_PickupPointNotFound_ThrowsException() {
        when(pickupPointRepository.findById(FALSE_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> pickupPointService.update(FALSE_ID, dto));
        verify(pickupPointRepository, never()).save(any());
    }

    @Test
    void update_StorageNotFound_ThrowsException() {
        when(pickupPointRepository.findById(POINT_ID)).thenReturn(Optional.of(pickupPoint));
        when(storageRepository.findById(STORAGE_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> pickupPointService.update(POINT_ID, dto));
    }

    @Test
    void update_Duplicate_ThrowsException() {
        PickupPointDto updateDto = PickupPointDto.builder()
                .address(NEW_ADDRESS)
                .city(NEW_CITY)
                .storageId(STORAGE_ID)
                .build();
        PickupPoint existingPoint = new PickupPoint();
        existingPoint.setId(2L);
        existingPoint.setAddress(NEW_ADDRESS);
        existingPoint.setCity(NEW_CITY);

        when(pickupPointRepository.findById(POINT_ID)).thenReturn(Optional.of(pickupPoint));
        when(storageRepository.findById(STORAGE_ID)).thenReturn(Optional.of(storage));
        when(pickupPointRepository.findByAddress(NEW_ADDRESS)).thenReturn(new ArrayList<>(List.of(existingPoint)));
        when(pickupPointRepository.findByCity(NEW_CITY)).thenReturn(new ArrayList<>(List.of(existingPoint)));

        assertThrows(ResourceExistsException.class, () -> pickupPointService.update(POINT_ID, updateDto));
        verify(pickupPointRepository, never()).save(any());
    }

    @Test
    void updateStorage_Success() {
        assertDoesNotThrow(() -> pickupPointService.updateStorage(pickupPoint));
        verify(pickupPointRepository).save(pickupPoint);
    }

    @Test
    void delete_Success() {
        when(pickupPointRepository.findById(POINT_ID)).thenReturn(Optional.of(pickupPoint));
        when(orderRepository.findByPointId(POINT_ID)).thenReturn(List.of());
        doNothing().when(orderRepository).deleteAll(List.of());
        doNothing().when(pickupPointRepository).deleteById(POINT_ID);

        assertDoesNotThrow(() -> pickupPointService.delete(POINT_ID));

        verify(orderRepository).deleteAll(List.of());
        verify(pickupPointRepository).deleteById(POINT_ID);
    }

    @Test
    void delete_PickupPointNotFound_ThrowsException() {
        when(pickupPointRepository.findById(FALSE_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> pickupPointService.delete(FALSE_ID));
        verify(orderRepository, never()).deleteAll(any());
        verify(pickupPointRepository, never()).deleteById(any());
    }
}