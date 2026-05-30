package ru.angelika.boutique.mapper;

import ru.angelika.boutique.model.ItemCard;
import ru.angelika.boutique.model.Order;
import ru.angelika.boutique.model.PickupPoint;
import ru.angelika.boutique.model.User;

import java.util.List;

public class OrderMapper {
    public static Order toOrder(User user, PickupPoint point, List<ItemCard> items) {
        Order order = new Order();
        order.setUser(user);
        order.setItems(items);
        order.setPoint(point);
        order.setPrice(items.stream().mapToDouble(ItemCard::getPrice).sum());
        return order;
    }
}
