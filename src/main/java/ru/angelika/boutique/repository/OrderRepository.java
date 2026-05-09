package ru.angelika.boutique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.angelika.boutique.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
