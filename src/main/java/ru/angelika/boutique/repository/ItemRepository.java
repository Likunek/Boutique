package ru.angelika.boutique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.angelika.boutique.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Item findByName(String name);
    List<Item> findBySellerIdAndVerifyTrue(Long sellerId);
}
