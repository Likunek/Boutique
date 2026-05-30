package ru.angelika.boutique.jpa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.angelika.boutique.model.*;
import ru.angelika.boutique.repository.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class OrderJpaTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PickupPointRepository pickupPointRepository;

    @Autowired
    private ItemCardRepository itemCardRepository;

    @Autowired
    private AuthenticationRepository authenticationRepository;

    private User testUser;
    private PickupPoint testPoint;
    private ItemCard testItemCard;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        Authentication auth = new Authentication();
        auth.setNumber("12345678999");
        auth.setPassword("order");
        auth.setRole(Role.USER);
        auth = authenticationRepository.save(auth);

        testUser = new User();
        testUser.setName("User");
        testUser.setNumber("12345678999");
        testUser.setEmail("nadin@mail.com");
        testUser.setBalance(1000.0);
        testUser.setAuthentication(auth);
        testUser = userRepository.save(testUser);

        testPoint = new PickupPoint();
        testPoint.setAddress("Address");
        testPoint.setCity("City");
        testPoint = pickupPointRepository.save(testPoint);

        testItemCard = new ItemCard();
        testItemCard.setName("Item Card");
        testItemCard.setPrice(99.99);
        testItemCard.setSeller("Seller");
        testItemCard = itemCardRepository.save(testItemCard);

        testOrder = new Order();
        testOrder.setCode(1001);
        testOrder.setUser(testUser);
        testOrder.setStatus(Status.NEW);
        testOrder.setDate(LocalDateTime.now());
        testOrder.setItems(List.of(testItemCard));
        testOrder.setPrice(99.99);
        testOrder.setPoint(testPoint);
        testOrder = orderRepository.save(testOrder);
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        itemCardRepository.deleteAll();
        pickupPointRepository.deleteAll();
        userRepository.deleteAll();
        authenticationRepository.deleteAll();
    }

    @Test
    void findByCode_ShouldReturnOrder_WhenExists() {
        Order found = orderRepository.findByCode(1001);
        assertNotNull(found);
        assertEquals(testOrder.getId(), found.getId());
        assertEquals(1001, found.getCode());
    }

    @Test
    void findByCode_ShouldReturnNull_WhenNotExists() {
        Order found = orderRepository.findByCode(9999);
        assertNull(found);
    }

    @Test
    void findByPointId_ShouldReturnOrders() {
        List<Order> orders = orderRepository.findByPointId(testPoint.getId());
        assertNotNull(orders);
        assertEquals(1, orders.size());
        assertEquals(testOrder.getId(), orders.get(0).getId());
    }

    @Test
    void findByPointId_ShouldReturnEmptyList_WhenNoOrders() {
        List<Order> orders = orderRepository.findByPointId(999L);
        assertTrue(orders.isEmpty());
    }

    @Test
    void findByUserId_ShouldReturnOrders() {
        List<Order> orders = orderRepository.findByUserId(testUser.getId());
        assertNotNull(orders);
        assertEquals(1, orders.size());
        assertEquals(testOrder.getId(), orders.get(0).getId());
    }

    @Test
    void findByUserId_ShouldReturnEmptyList_WhenNoOrders() {
        List<Order> orders = orderRepository.findByUserId(999L);
        assertTrue(orders.isEmpty());
    }

    @Test
    void findByUserIdAndStatusNot_ShouldExcludeGivenStatus() {
        Order wayOrder = new Order();
        wayOrder.setCode(1002);
        wayOrder.setUser(testUser);
        wayOrder.setStatus(Status.WAY);
        wayOrder.setDate(LocalDateTime.now());
        wayOrder.setItems(List.of(testItemCard));
        wayOrder.setPrice(50.0);
        wayOrder.setPoint(testPoint);
        orderRepository.save(wayOrder);

        List<Order> orders = orderRepository.findByUserIdAndStatusNot(testUser.getId(), Status.WAY);
        assertNotNull(orders);
        assertEquals(1, orders.size());
        assertEquals(Status.NEW, orders.get(0).getStatus());
        assertEquals(testOrder.getId(), orders.get(0).getId());
    }

    @Test
    void findByStatus_ShouldReturnOrdersWithGivenStatus() {
        Order deliveredOrder = new Order();
        deliveredOrder.setCode(1003);
        deliveredOrder.setUser(testUser);
        deliveredOrder.setStatus(Status.DELIVERED);
        deliveredOrder.setDate(LocalDateTime.now());
        deliveredOrder.setItems(List.of(testItemCard));
        deliveredOrder.setPrice(30.0);
        deliveredOrder.setPoint(testPoint);
        deliveredOrder = orderRepository.save(deliveredOrder);

        List<Order> delivered = orderRepository.findByStatus(Status.DELIVERED);
        assertEquals(1, delivered.size());
        assertEquals(deliveredOrder.getId(), delivered.get(0).getId());
    }

    @Test
    void findByIdWithPointAndItems_ShouldLoadAllRelations() {
        Order loadedOrder = orderRepository.findByIdWithPointAndItems(testOrder.getId());
        assertNotNull(loadedOrder);
        assertEquals(testOrder.getId(), loadedOrder.getId());

        assertNotNull(loadedOrder.getPoint());
        assertEquals(testPoint.getId(), loadedOrder.getPoint().getId());

        assertNotNull(loadedOrder.getItems());
        assertFalse(loadedOrder.getItems().isEmpty());
        assertEquals(testItemCard.getId(), loadedOrder.getItems().get(0).getId());
    }
}