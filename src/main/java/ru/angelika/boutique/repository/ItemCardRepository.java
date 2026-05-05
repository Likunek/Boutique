package ru.angelika.boutique.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.angelika.boutique.model.ItemCard;

import java.util.List;

public interface ItemCardRepository extends JpaRepository<ItemCard, Long> {
    Page<ItemCard> findBySeller(String seller, Pageable pageable);
    List<ItemCard> findByNameAndSeller(String name, String seller);
    List<ItemCard> findByDescriptionAndSeller(String description, String seller);
}
