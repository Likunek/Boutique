package ru.angelika.boutique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.angelika.boutique.model.Order;
import ru.angelika.boutique.model.PickupPoint;
import ru.angelika.boutique.model.Status;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Order findByCode(Integer code);
    List<Order> findByPointId(Long Id);

    List<Order> findByUserId(Long Id);

    List<Order> findByUserIdAndStatusNot(Long userId, Status status);

    List<Order> findByStatus(Status status);

    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.point JOIN FETCH o.items WHERE o.id = :id")
    Order findByIdWithPointAndItems(@Param("id") Long id);
}
