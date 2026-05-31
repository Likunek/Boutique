package ru.angelika.boutique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.angelika.boutique.model.Supply;

/**
 * Репозиторий для работы с сущностью {@link Supply} (поставки товаров).
 */
public interface SupplyRepository extends JpaRepository<Supply, Long> {
}