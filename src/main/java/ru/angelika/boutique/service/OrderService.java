package ru.angelika.boutique.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.angelika.boutique.dto.OrderDto;
import ru.angelika.boutique.mapper.OrderMapper;
import org.springframework.transaction.annotation.Transactional;
import ru.angelika.boutique.model.*;
import ru.angelika.boutique.repository.OrderRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final CartService cartService;
    private final ItemCardService itemCardService;
    private final OrderRepository orderRepository;
    private final PickupPointService pickupPointService;

    public void addOrder(OrderDto orderDto, User user, Long cartId) {
        List<ItemCard> cards = orderDto.getItemCards()
                .parallelStream()
                .map(itemCardService::getItemCard)
                .toList();
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
    @Transactional
    public void updateStatus(Order order, Status status) {
        order.setStatus(status);
        orderRepository.save(order);
        log.info("Update order by id={}, new status={}", order.getId(), status);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}