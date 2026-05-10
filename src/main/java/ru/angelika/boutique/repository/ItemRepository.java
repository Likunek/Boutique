package ru.angelika.boutique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.angelika.boutique.model.Item;
import ru.angelika.boutique.model.Seller;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    Item findByItemCardId(Long itemCardId);

    Item findBySellerIdAndName(Long sellerId, String name);

    List<Item> findBySellerId(Long sellerId);

    List<Item> findByVerifyFalse();

    List<Item> findBySellerIdAndVerifyTrue(Long sellerId);

    @Query("SELECT i FROM Item i JOIN i.itemsAtStorages s WHERE s.id = :itemsAtStorageId")
    Item findItemByItemsAtStorageId(@Param("itemsAtStorageId") Long storageId);

    @Query("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.itemsAtStorages WHERE i.seller = :seller")
    List<Item> findBySellerWithStorages(@Param("seller") Seller seller);

}
