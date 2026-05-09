package ru.angelika.boutique.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.angelika.boutique.model.Order;
import ru.angelika.boutique.repository.OrderRepository;

import java.util.List;

@Slf4j
@Service
public class OrderService {

    private final UserService userService;
    private final ItemService itemService;
    private final OrderRepository orderRepository;
    private final PointReceiptService pointReceiptService;

    @Autowired
    public OrderService(UserService userService, ItemService itemService, OrderRepository orderRepository,
                        PointReceiptService pointReceiptService) {
        this.userService = userService;
        this.itemService = itemService;
        this.orderRepository = orderRepository;
        this.pointReceiptService = pointReceiptService;
    }

   public List<Order> getAllOrders() {
        return orderRepository.findAll();
   }
}