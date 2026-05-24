package ru.angelika.boutique.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.*;
import ru.angelika.boutique.repository.SupplyRepository;
import ru.angelika.boutique.service.OrderService;
import ru.angelika.boutique.service.SupplyService;
import ru.angelika.boutique.service.SupplyTransactionalService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplyUnitTest {

    @Mock
    private SupplyRepository supplyRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private SupplyTransactionalService supplyTransactionalService;

    @InjectMocks
    private SupplyService supplyService;

    private static final Long STORAGE_ID = 10L;

    private Order order1;
    private Order order2;
    private Item item1;
    private Item item2;

    @BeforeEach
    void setUp() {
        Storage storage = new Storage();
        storage.setId(STORAGE_ID);

        PickupPoint point = new PickupPoint();
        point.setId(1L);
        point.setStorage(storage);

        ItemCard itemCard1 = new ItemCard();
        itemCard1.setId(4L);
        itemCard1.setPrice(100.0);

        ItemCard itemCard2 = new ItemCard();
        itemCard2.setId(5L);
        itemCard2.setPrice(150.0);

        item1 = new Item();
        item1.setId(2L);
        item1.setWeight(1.0);

        item2 = new Item();
        item2.setId(3L);
        item2.setWeight(2.0);

        List<ItemCard> itemsCard1 = new ArrayList<>();
        itemsCard1.add(itemCard1);
        List<ItemCard> itemsCard2 = new ArrayList<>();
        itemsCard2.add(itemCard2);

        order1 = new Order();
        order1.setId(7L);
        order1.setPoint(point);
        order1.setStatus(Status.NEW);
        order1.setItems(itemsCard1);

        order2 = new Order();
        order2.setId(8L);
        order2.setPoint(point);
        order2.setStatus(Status.NEW);
        order2.setItems(itemsCard2);
    }

    @Test
    void getAll_Success() {
        when(supplyRepository.findAll()).thenReturn(List.of(new Supply()));
        List<Supply> result = supplyService.getAll();
        assertEquals(1, result.size());
    }

    @Test
    void addSupply_Success() throws InterruptedException {
        List<Order> orders = new ArrayList<>(List.of(order1, order2));
        when(orderService.getAllByStatus(Status.NEW)).thenReturn(orders);
        when(supplyTransactionalService.checkCountOnStorage(order1, STORAGE_ID)).thenReturn(List.of(item1));
        when(supplyTransactionalService.checkCountOnStorage(order2, STORAGE_ID)).thenReturn(List.of(item2));
        doNothing().when(orderService).updateStatus(any(Order.class), eq(Status.WAY));

        assertDoesNotThrow(() -> supplyService.addSupply());
        Thread.sleep(300);

        verify(supplyTransactionalService).checkCountOnStorage(order1, STORAGE_ID);
        verify(supplyTransactionalService).checkCountOnStorage(order2, STORAGE_ID);
        verify(orderService, times(2)).updateStatus(any(Order.class), eq(Status.WAY));
        verify(supplyRepository).save(any(Supply.class));
    }

    @Test
    void addSupply_NoNewOrders_DoesNothing() {
        when(orderService.getAllByStatus(Status.NEW)).thenReturn(new ArrayList<>());
        supplyService.addSupply();
        verify(supplyTransactionalService, never()).checkCountOnStorage(any(), any());
        verify(supplyRepository, never()).save(any());
    }

    @Test
    void addSupply_SecondOrderResourceNotFound_Success() throws InterruptedException {
        List<Order> orders = List.of(order1, order2);
        when(orderService.getAllByStatus(Status.NEW)).thenReturn(orders);
        when(supplyTransactionalService.checkCountOnStorage(order1, STORAGE_ID)).thenReturn(List.of(item1));
        when(supplyTransactionalService.checkCountOnStorage(order2, STORAGE_ID))
                .thenThrow(new ResourceNotFoundException(ItemsAtStorage.class, "items count = 0"));
        doNothing().when(orderService).updateStatus(order1, Status.WAY);

        supplyService.addSupply();
        Thread.sleep(300);

        verify(orderService, times(1)).updateStatus(order1, Status.WAY);
        verify(orderService, never()).updateStatus(order2, Status.WAY);
        verify(supplyRepository, times(1)).save(any(Supply.class));
    }
}