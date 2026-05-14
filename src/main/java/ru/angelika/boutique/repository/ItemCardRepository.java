package ru.angelika.boutique.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.angelika.boutique.model.Item;
import ru.angelika.boutique.model.ItemCard;

import java.util.List;

public interface ItemCardRepository extends JpaRepository<ItemCard, Long> {
    Page<ItemCard> findBySeller(String seller, Pageable pageable);

    List<ItemCard> findByNameAndSeller(String name, String seller);

    List<ItemCard> findByDescriptionAndSeller(String description, String seller);

    @Query("SELECT i FROM ItemCard i JOIN i.feedbacks s WHERE s.id = :feedbackId")
    ItemCard findItemByFeedbackId(@Param("feedbackId") Long feedbackId);
    @Query("SELECT i FROM ItemCard i WHERE (LOWER(i.name) LIKE CONCAT('%', :text, '%') OR LOWER(i.description) LIKE CONCAT('%', :text, '%'))")
    Page<ItemCard> findByNameOrDescription(@Param("text") String text, Pageable pageable);
}
