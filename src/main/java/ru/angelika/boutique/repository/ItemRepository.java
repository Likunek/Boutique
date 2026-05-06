package ru.angelika.boutique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.angelika.boutique.model.Item;
import ru.angelika.boutique.model.Seller;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Item findByName(String name);
    Item findByItemCardId(Long itemCardId);
    List<Item> findBySellerId(Long sellerId);
    List<Item> findBySellerIdAndVerifyTrue(Long sellerId);
    @Query("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.itemsAtStorages WHERE i.seller = :seller")
    List<Item> findBySellerWithStorages(@Param("seller") Seller seller);


}
