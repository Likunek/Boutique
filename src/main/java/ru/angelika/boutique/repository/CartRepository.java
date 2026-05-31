package ru.angelika.boutique.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.angelika.boutique.model.Cart;

/**
 * Репозиторий для работы с сущностью {@link Cart}.
 * Предоставляет стандартные CRUD-операции через наследование от {@link JpaRepository}.
 */
public interface CartRepository extends JpaRepository<Cart, Long> {
}
