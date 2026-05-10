package ru.angelika.boutique.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.angelika.boutique.dto.OrderDto;
import ru.angelika.boutique.mapper.OrderMapper;
import ru.angelika.boutique.model.ItemCard;
import ru.angelika.boutique.model.Order;
import ru.angelika.boutique.model.PickupPoint;
import ru.angelika.boutique.model.User;
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
        log.info("Add new Order: userId={}, pointId={}, countItems={}",
                user.getId(), point.getId(), cards.size());
    }

   public List<Order> getAllOrders() {
        return orderRepository.findAll();
   }
}