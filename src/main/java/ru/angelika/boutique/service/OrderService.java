package ru.angelika.boutique.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.angelika.boutique.dto.OrderDto;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.mapper.OrderMapper;
import ru.angelika.boutique.model.*;
import ru.angelika.boutique.repository.OrderRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderService {
    private final CartService cartService;
    private final ItemCardService itemCardService;
    private final OrderRepository orderRepository;
    private final PickupPointService pickupPointService;

    @Autowired
    public OrderService(ItemCardService itemCardService, OrderRepository orderRepository,
                        PickupPointService pickupPointService, CartService cartService) {
        this.itemCardService = itemCardService;
        this.orderRepository = orderRepository;
        this.pickupPointService = pickupPointService;
        this.cartService = cartService;
    }

    public void addOrder(OrderDto orderDto, User user, Long cartId) {
        List<ItemCard> cards = orderDto.getItemCards().stream().map(itemCardService::getItemCard).toList();
        PickupPoint point = pickupPointService.getPickupPoint(orderDto.getPointId());
        orderRepository.save(OrderMapper.toOrder(user, point, cards));
        cartService.deleteCardsFromCart(cartId, orderDto.getItemCards());
        log.info("Add new order: userId={}, pointId={}, countItems={}",
                user.getId(), point.getId(), cards.size());
    }

    public List<Order> getOrdersByStatus(Status status) {
        log.debug("Get orders by status={}", status);
        return orderRepository.findByStatus(status);
    }

    public void updateStatus(Order order, Status status) {
        order.setStatus(status);
        orderRepository.save(order);
        log.info("Update order by id={}, new status={}", order.getId(), status);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}