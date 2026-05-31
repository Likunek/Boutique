package ru.angelika.boutique.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.angelika.boutique.dto.OrderDto;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.mapper.OrderMapper;
import org.springframework.transaction.annotation.Transactional;
import ru.angelika.boutique.model.*;
import ru.angelika.boutique.repository.OrderRepository;
import ru.angelika.boutique.repository.UserRepository;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Сервис для управления заказами.
 * Оформление заказов, проверка баланса пользователя, обновление статусов.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final CartService cartService;
    private final ItemCardService itemCardService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PickupPointService pickupPointService;

    /**
     * Оформляет новый заказ.
     * Генерирует уникальный 6-значный код заказа, списывает средства со счёта пользователя,
     * удаляет товары из корзины, сохраняет заказ.
     *
     * @param orderDto DTO с itemCards и pointId
     * @param user     пользователь, оформляющий заказ
     * @param cartId   ID корзины (для очистки)
     * @throws ResourceNotFoundException если пункт выдачи не найден
     */
    public void add(OrderDto orderDto, User user, Long cartId) {
        List<ItemCard> cards = orderDto.getItemCards()
                .parallelStream()
                .map(itemCardService::get)
                .toList();
        user.setBalance(user.getBalance() - cards.stream().mapToDouble(ItemCard::getPrice).sum());
        PickupPoint point = pickupPointService.get(orderDto.getPointId());
        Order order = OrderMapper.toOrder(user, point, cards);
        boolean coincidence = true;
        while (coincidence) {
            Integer code = ThreadLocalRandom.current().nextInt(100000, 1000000);
            if (orderRepository.findByCode(code) == null) {
                coincidence = false;
                order.setCode(code);
            }
        }
        cartService.deleteCardsFromCart(cartId, orderDto.getItemCards());
        orderRepository.save(order);
        log.info("Add new order: userId={}, pointId={}, countItems={}",
                user.getId(), point.getId(), cards.size());
    }

    /**
     * Проверяет, хватит ли средств у пользователя для оплаты заказа.
     *
     * @param orderDto DTO заказа
     * @param user     пользователь
     * @return остаток на счету после оплаты (отрицательное значение – не хватает)
     */
    public double checkPaymentUser(OrderDto orderDto, User user) {
        List<ItemCard> cards = orderDto.getItemCards()
                .parallelStream()
                .map(itemCardService::get)
                .toList();
        return user.getBalance() - cards.stream().mapToDouble(ItemCard::getPrice).sum();
    }

    /**
     * Находит заказ по ID с предварительной загрузкой пункта выдачи и товаров.
     *
     * @param id ID заказа
     * @return заказ с инициализированными коллекциями
     * @throws ResourceNotFoundException если заказ не найден
     */
    @Transactional
    public Order getWithPoint(Long id) {
        Order order = orderRepository.findByIdWithPointAndItems(id);
        if (order == null) {
            log.error("Order not found for get, id={}", id);
            throw new ResourceNotFoundException(Order.class, id);
        }
        return order;
    }

    /**
     * Возвращает все заказы.
     *
     * @return список всех заказов
     */
    public List<Order> getAll() {
        return orderRepository.findAll();
    }

    /**
     * Возвращает заказы с указанным статусом.
     *
     * @param status статус (NEW, WAY, DELIVERED, RECEIVED)
     * @return список заказов
     */
    public List<Order> getAllByStatus(Status status) {
        log.debug("Get orders by status={}", status);
        return orderRepository.findByStatus(status);
    }

    /**
     * Обновляет статус заказа.
     * Если статус становится RECEIVED, добавляет товары в историю покупок пользователя.
     *
     * @param order  заказ
     * @param status новый статус
     * @throws ResourceNotFoundException если пользователь не найден (при RECEIVED)
     */
    @Transactional
    public void updateStatus(Order order, Status status) {
        order.setStatus(status);
        log.info("Update order by id={}, new status={}", order.getId(), status);
        if (status == Status.RECEIVED) {
            User user = userRepository.findById(order.getUser().getId())
                    .orElseThrow(() -> {
                        log.error("User not found for update purchase history, id={}", order.getUser().getId());
                        return new ResourceNotFoundException(User.class, order.getUser().getId());
                    });
            user.getItems().addAll(order.getItems());
            userRepository.save(user);
            log.info("Update user's purchase history id={}", order.getUser().getId());
        }
        orderRepository.save(order);
    }
}