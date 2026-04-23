package ru.angelika.boutique.repository;

import org.springframework.data.repository.CrudRepository;
import ru.angelika.boutique.model.ItemCard;

public interface ItemCardRepository extends CrudRepository<ItemCard, Integer> {
}
