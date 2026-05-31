package ru.angelika.boutique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.angelika.boutique.model.ItemsAtStorage;

import java.util.List;

/**
 * Репозиторий для работы со связующей сущностью {@link ItemsAtStorage}
 */
public interface ItemsAtStorageRepository extends JpaRepository<ItemsAtStorage, Long> {

    /**
     * Находит все записи об остатках на указанном складе.
     *
     * @param storageId ID склада
     * @return список остатков
     */
    List<ItemsAtStorage> findByStorageId(Long storageId);

    /**
     * Находит запись об остатке конкретного товара на конкретном складе.
     * Подтягивает все сущности для избежания LazyInitializationException.
     *
     * @param itemId    ID товара
     * @param storageId ID склада
     * @return запись остатка, или null если товар не хранится на этом складе
     */
    @Query("SELECT ias FROM ItemsAtStorage ias " +
            "JOIN FETCH ias.item " +
            "JOIN FETCH ias.storage " +
            "WHERE ias.item.id = :itemId AND ias.storage.id = :storageId")
    ItemsAtStorage findByItemIdAndStorageId(@Param("itemId") Long itemId, @Param("storageId") Long storageId);
}