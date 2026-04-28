package ru.angelika.boutique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.angelika.boutique.model.ItemCard;

public interface ItemCardRepository extends JpaRepository<ItemCard, Long> {
}
