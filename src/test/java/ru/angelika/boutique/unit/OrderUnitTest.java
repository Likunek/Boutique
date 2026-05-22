package ru.angelika.boutique.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.angelika.boutique.dto.OrderDto;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.*;
import ru.angelika.boutique.repository.OrderRepository;
import ru.angelika.boutique.repository.UserRepository;
import ru.angelika.boutique.service.CartService;
import ru.angelika.boutique.service.ItemCardService;
import ru.angelika.boutique.service.OrderService;
import ru.angelika.boutique.service.PickupPointService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderUnitTest {

    @Mock
    private CartService cartService;

    @Mock
    private ItemCardService itemCardService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PickupPointService pickupPointService;

    @InjectMocks
    private OrderService orderService;

    private static final Long USER_ID = 1L;
    private static final Long CART_ID = 10L;
    private static final Long POINT_ID = 20L;
    private static final Long ORDER_ID = 100L;
    private static final Long ITEM_CARD_ID_1 = 30L;
    private static final Long ITEM_CARD_ID_2 = 31L;
    private static final Double PRICE_1 = 100.0;
    private static final Double PRICE_2 = 150.0;
    private static final Double USER_BALANCE = 300.0;

    private OrderDto orderDto;
    private User user;
    private PickupPoint pickupPoint;
    private ItemCard itemCard1;
    private ItemCard itemCard2;
    private Order order;

    @BeforeEach
    void setUp() {
        List<Long> itemCardIds = new ArrayList<>();
        itemCardIds.add(ITEM_CARD_ID_1);
        itemCardIds.add(ITEM_CARD_ID_2);

        orderDto = OrderDto.builder()
                .itemCards(itemCardIds)
                .pointId(POINT_ID)
                .build();

        user = new User();
        user.setId(USER_ID);
        user.setBalance(USER_BALANCE);

        pickupPoint = new PickupPoint();
        pickupPoint.setId(POINT_ID);

        itemCard1 = new ItemCard();
        itemCard1.setId(ITEM_CARD_ID_1);
        itemCard1.setPrice(PRICE_1);

        itemCard2 = new ItemCard();
        itemCard2.setId(ITEM_CARD_ID_2);
        itemCard2.setPrice(PRICE_2);

        order = new Order();
        order.setId(ORDER_ID);
        order.setUser(user);
        order.setStatus(Status.NEW);
    }

    @Test
    void add_Success() {
        when(itemCardService.get(ITEM_CARD_ID_1)).thenReturn(itemCard1);
        when(itemCardService.get(ITEM_CARD_ID_2)).thenReturn(itemCard2);
        when(pickupPointService.get(POINT_ID)).thenReturn(pickupPoint);
        doNothing().when(cartService).deleteCardsFromCart(CART_ID, orderDto.getItemCards());

        assertDoesNotThrow(() -> orderService.add(orderDto, user, CART_ID));

        verify(cartService).deleteCardsFromCart(CART_ID, orderDto.getItemCards());
        verify(orderRepository).save(any(Order.class));
        assertEquals(USER_BALANCE - PRICE_1 - PRICE_2, user.getBalance());
    }

    @Test
    void add_ItemCardNotFound_ThrowsException() {
        when(itemCardService.get(ITEM_CARD_ID_1)).thenThrow(new ResourceNotFoundException(ItemCard.class, ITEM_CARD_ID_1));

        assertThrows(ResourceNotFoundException.class, () -> orderService.add(orderDto, user, CART_ID));
        verify(orderRepository, never()).save(any());
        verify(cartService, never()).deleteCardsFromCart(any(), any());
    }

    @Test
    void add_PickupPointNotFound_ThrowsException() {
        when(itemCardService.get(ITEM_CARD_ID_1)).thenReturn(itemCard1);
        when(itemCardService.get(ITEM_CARD_ID_2)).thenReturn(itemCard2);
        when(pickupPointService.get(POINT_ID)).thenThrow(new ResourceNotFoundException(PickupPoint.class, POINT_ID));

        assertThrows(ResourceNotFoundException.class, () -> orderService.add(orderDto, user, CART_ID));
        verify(orderRepository, never()).save(any());
        verify(cartService, never()).deleteCardsFromCart(any(), any());
    }

    @Test
    void checkPaymentUser_Success() {
        when(itemCardService.get(ITEM_CARD_ID_1)).thenReturn(itemCard1);
        when(itemCardService.get(ITEM_CARD_ID_2)).thenReturn(itemCard2);

        double result = orderService.checkPaymentUser(orderDto, user);

        assertEquals(USER_BALANCE - PRICE_1 - PRICE_2, result);
    }

    @Test
    void checkPaymentUser_InsufficientFunds() {
        user.setBalance(50.0);
        when(itemCardService.get(ITEM_CARD_ID_1)).thenReturn(itemCard1);
        when(itemCardService.get(ITEM_CARD_ID_2)).thenReturn(itemCard2);

        double result = orderService.checkPaymentUser(orderDto, user);

        assertEquals(50.0 - PRICE_1 - PRICE_2, result);
        assertTrue(result < 0);
    }

    @Test
    void checkPaymentUser_ItemCardNotFound_ThrowsException() {
        when(itemCardService.get(ITEM_CARD_ID_1)).thenThrow(new ResourceNotFoundException(ItemCard.class, ITEM_CARD_ID_1));
        assertThrows(ResourceNotFoundException.class, () -> orderService.checkPaymentUser(orderDto, user));
    }

    @Test
    void getAll_Success() {
        when(orderRepository.findAll()).thenReturn(List.of(order));
        List<Order> result = orderService.getAll();
        assertEquals(1, result.size());
    }

    @Test
    void getAllByStatus_Success() {
        when(orderRepository.findByStatus(Status.NEW)).thenReturn(List.of(order));
        List<Order> result = orderService.getAllByStatus(Status.NEW);
        assertEquals(1, result.size());
    }

    @Test
    void updateStatus_ToReceived_Success() {
        order.setItems(List.of(itemCard1, itemCard2));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> orderService.updateStatus(order, Status.RECEIVED));

        verify(orderRepository).save(order);
        verify(userRepository).save(user);
        assertEquals(Status.RECEIVED, order.getStatus());
        assertEquals(2, user.getItems().size());
        assertTrue(user.getItems().contains(itemCard1));
        assertTrue(user.getItems().contains(itemCard2));
    }

    @Test
    void updateStatus_ToReceived_UserNotFound_ThrowsException() {
        order.setUser(user);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.updateStatus(order, Status.RECEIVED));
        verify(orderRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

}