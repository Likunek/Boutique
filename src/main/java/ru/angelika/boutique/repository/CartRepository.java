package ru.angelika.boutique.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.angelika.boutique.model.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {
}
