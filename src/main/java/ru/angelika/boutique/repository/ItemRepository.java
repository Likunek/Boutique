package ru.angelika.boutique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.angelika.boutique.model.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Item findByName(String name);
}
