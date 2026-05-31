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
import ru.angelika.boutique.service.PickupPointService;
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
    private PickupPointService pickupPointService;

    @Mock
    private SupplyTransactionalService supplyTransactionalService;

    @InjectMocks
    private SupplyService supplyService;

    private static final Long STORAGE_ID = 10L;
    private static final Long POINT_ID = 1L;

    private PickupPoint point;
    private Storage storage;
    private Order order1;
    private Order order2;
    private Order order3;
    private Item item1;
    private Item item2;
    private Item item3;

    @BeforeEach
    void setUp() {
        storage = new Storage();
        storage.setId(STORAGE_ID);

        point = new PickupPoint();
        point.setId(POINT_ID);
        point.setStorage(storage);

        ItemCard itemCard1 = new ItemCard();
        itemCard1.setId(4L);

        ItemCard itemCard2 = new ItemCard();
        itemCard2.setId(5L);

        ItemCard itemCard3 = new ItemCard();
        itemCard3.setId(6L);

        item1 = new Item();
        item1.setId(2L);
        item1.setWeight(600.0);

        item2 = new Item();
        item2.setId(3L);
        item2.setWeight(400.0);

        item3 = new Item();
        item3.setId(4L);
        item3.setWeight(500.0);

        List<ItemCard> itemsCard1 = new ArrayList<>();
        itemsCard1.add(itemCard1);
        List<ItemCard> itemsCard2 = new ArrayList<>();
        itemsCard2.add(itemCard2);
        List<ItemCard> itemsCard3 = new ArrayList<>();
        itemsCard3.add(itemCard3);

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

        order3 = new Order();
        order3.setId(9L);
        order3.setPoint(point);
        order3.setStatus(Status.NEW);
        order3.setItems(itemsCard3);
    }

    @Test
    void getAll_Success() {
        when(supplyRepository.findAll()).thenReturn(List.of(new Supply()));
        List<Supply> result = supplyService.getAll();
        assertEquals(1, result.size());
    }

    @Test
    void addSupply_Success() throws InterruptedException {
        List<Order> orders = List.of(order1, order2);
        when(orderService.getAllByStatus(Status.NEW)).thenReturn(orders);
        when(pickupPointService.getWithStorage(POINT_ID)).thenReturn(point);
        when(orderService.getWithPoint(order1.getId())).thenReturn(order1);
        when(orderService.getWithPoint(order2.getId())).thenReturn(order2);
        when(supplyTransactionalService.checkCountOnStorage(order1, STORAGE_ID)).thenReturn(List.of(item1));
        when(supplyTransactionalService.checkCountOnStorage(order2, STORAGE_ID)).thenReturn(List.of(item2));
        doNothing().when(orderService).updateStatus(any(Order.class), eq(Status.WAY));

        supplyService.addSupply();

        Thread.sleep(300);

        verify(supplyTransactionalService, times(1)).checkCountOnStorage(order1, STORAGE_ID);
        verify(supplyTransactionalService, times(1)).checkCountOnStorage(order2, STORAGE_ID);
        verify(orderService, times(2)).updateStatus(any(Order.class), eq(Status.WAY));
        verify(supplyRepository, times(1)).save(any(Supply.class));
    }

    @Test
    void addSupply_NoNewOrders_DoesNothing() {
        when(orderService.getAllByStatus(Status.NEW)).thenReturn(new ArrayList<>());
        supplyService.addSupply();
        verify(pickupPointService, never()).getWithStorage(any());
        verify(orderService, never()).getWithPoint(any());
        verify(supplyTransactionalService, never()).checkCountOnStorage(any(), any());
        verify(supplyRepository, never()).save(any());
    }

    @Test
    void addSupply_SecondOrderResourceNotFound_Success() throws InterruptedException {
        List<Order> orders = List.of(order1, order2);
        when(orderService.getAllByStatus(Status.NEW)).thenReturn(orders);
        when(pickupPointService.getWithStorage(POINT_ID)).thenReturn(point);
        when(orderService.getWithPoint(order1.getId())).thenReturn(order1);
        when(orderService.getWithPoint(order2.getId())).thenReturn(order2);
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

    @Test
    void addSupply_WeightLimitExceeded_FirstTwoFit_ThirdExceeds() throws InterruptedException {
        List<Order> orders = List.of(order1, order2, order3);
        when(orderService.getAllByStatus(Status.NEW)).thenReturn(orders);
        when(pickupPointService.getWithStorage(POINT_ID)).thenReturn(point);
        when(orderService.getWithPoint(order1.getId())).thenReturn(order1);
        when(orderService.getWithPoint(order2.getId())).thenReturn(order2);
        when(orderService.getWithPoint(order3.getId())).thenReturn(order3);
        when(supplyTransactionalService.checkCountOnStorage(order1, STORAGE_ID)).thenReturn(List.of(item1)); // 600
        when(supplyTransactionalService.checkCountOnStorage(order2, STORAGE_ID)).thenReturn(List.of(item2)); // 400 -> total 1000
        when(supplyTransactionalService.checkCountOnStorage(order3, STORAGE_ID)).thenReturn(List.of(item3)); // 500 -> would exceed
        doNothing().when(orderService).updateStatus(order1, Status.WAY);
        doNothing().when(orderService).updateStatus(order2, Status.WAY);

        supplyService.addSupply();
        Thread.sleep(300);

        verify(orderService, times(1)).updateStatus(order1, Status.WAY);
        verify(orderService, times(1)).updateStatus(order2, Status.WAY);
        verify(orderService, never()).updateStatus(order3, Status.WAY);
        verify(supplyRepository, times(1)).save(any(Supply.class));
    }

    @Test
    void addSupply_FirstOrderExceedsWeightLimit_Skipped() throws InterruptedException {
        Item heavyItem = new Item();
        heavyItem.setId(99L);
        heavyItem.setWeight(1500.0);

        List<Order> orders = List.of(order1, order2);
        when(orderService.getAllByStatus(Status.NEW)).thenReturn(orders);
        when(pickupPointService.getWithStorage(POINT_ID)).thenReturn(point);
        when(orderService.getWithPoint(order1.getId())).thenReturn(order1);
        when(orderService.getWithPoint(order2.getId())).thenReturn(order2);
        when(supplyTransactionalService.checkCountOnStorage(order1, STORAGE_ID)).thenReturn(List.of(heavyItem));
        when(supplyTransactionalService.checkCountOnStorage(order2, STORAGE_ID)).thenReturn(List.of(item2));
        doNothing().when(orderService).updateStatus(order2, Status.WAY);

        supplyService.addSupply();
        Thread.sleep(300);

        verify(orderService, never()).updateStatus(order1, Status.WAY);
        verify(orderService, times(1)).updateStatus(order2, Status.WAY);
        verify(supplyRepository, times(1)).save(any(Supply.class));
    }

    @Test
    void addSupply_NoItemsFit_DontCreateSupply() throws InterruptedException {
        Item heavyItem = new Item();
        heavyItem.setId(99L);
        heavyItem.setWeight(1500.0);

        List<Order> orders = List.of(order1);
        when(orderService.getAllByStatus(Status.NEW)).thenReturn(orders);
        when(pickupPointService.getWithStorage(POINT_ID)).thenReturn(point);
        when(orderService.getWithPoint(order1.getId())).thenReturn(order1);
        when(supplyTransactionalService.checkCountOnStorage(order1, STORAGE_ID)).thenReturn(List.of(heavyItem));

        supplyService.addSupply();
        Thread.sleep(300);

        verify(orderService, never()).updateStatus(any(), any());
        verify(supplyRepository, never()).save(any());
    }
}