package ru.angelika.boutique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.angelika.boutique.model.Order;
import ru.angelika.boutique.model.Status;

import java.util.List;

/**
 * Репозиторий для работы с сущностью {@link Order}(заказы).
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Находит заказ по уникальному числовому коду.
     *
     * @param code код заказа
     * @return заказ, или null если не найден
     */
    Order findByCode(Integer code);

    /**
     * Находит все заказы, привязанные к указанному пункту выдачи.
     *
     * @param Id ID пункта выдачи (PickupPoint.id)
     * @return список заказов
     */
    List<Order> findByPointId(Long Id);

    /**
     * Находит все заказы пользователя.
     *
     * @param Id ID пользователя
     * @return список заказов
     */
    List<Order> findByUserId(Long Id);

    /**
     * Находит заказы пользователя, исключая те, у которых указанный статус.
     *
     * @param userId ID пользователя
     * @param status статус, который нужно исключить
     * @return отфильтрованный список заказов
     */
    List<Order> findByUserIdAndStatusNot(Long userId, Status status);

    /**
     * Находит все заказы с определённым статусом.
     *
     * @param status статус заказа
     * @return список заказов
     */
    List<Order> findByStatus(Status status);

    /**
     * Находит заказ по ID с предварительной загрузкой пункта выдачи и списка товаров.
     * Используется для избежания LazyInitializationException.
     *
     * @param id идентификатор заказа
     * @return заказ с инициализированными полями point и items, или null
     */
    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.point JOIN FETCH o.items WHERE o.id = :id")
    Order findByIdWithPointAndItems(@Param("id") Long id);
}