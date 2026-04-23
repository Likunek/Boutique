package ru.angelika.boutique.repository;

import org.springframework.data.repository.CrudRepository;
import ru.angelika.boutique.model.Cart;

public interface CartRepository extends CrudRepository<Cart, Integer> {
}
