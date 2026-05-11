package ru.angelika.boutique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.angelika.boutique.model.Order;
import ru.angelika.boutique.model.Status;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByPointId(Long Id);

    List<Order> findByUserId(Long Id);
    List<Order> findByUserIdAndStatusNot(Long userId, Status status);

    List<Order> findByStatus(Status status);
}
