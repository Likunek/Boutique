package ru.angelika.boutique.mapper;

import ru.angelika.boutique.model.ItemCard;
import ru.angelika.boutique.model.Order;
import ru.angelika.boutique.model.PickupPoint;
import ru.angelika.boutique.model.User;

import java.util.List;

/**
 * Маппер для создания сущности {@link Order} из пользователя, пункта выдачи и списка товаров.
 */
public class OrderMapper {

    /**
     * Создаёт новый заказ.
     * <p>Итоговая цена рассчитывается как сумма цен всех товарных карточек в заказе.</p>
     *
     * @param user   пользователь, оформляющий заказ
     * @param point  пункт выдачи, куда будет доставлен заказ
     * @param items  список товарных карточек, выбранных пользователем
     * @return заполненный объект {@link Order} (статус по умолчанию NEW, текущая дата)
     */
    public static Order toOrder(User user, PickupPoint point, List<ItemCard> items) {
        Order order = new Order();
        order.setUser(user);
        order.setItems(items);
        order.setPoint(point);
        order.setPrice(items.stream().mapToDouble(ItemCard::getPrice).sum());
        return order;
    }
}