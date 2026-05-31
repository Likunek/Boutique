package ru.angelika.boutique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.angelika.boutique.model.Item;
import ru.angelika.boutique.model.Seller;

import java.util.List;

/**
 * Репозиторий для работы с сущностью {@link Item}.
 */
public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * Находит физический товар по идентификатору его товарной карточки.
     *
     * @param itemCardId ID карточки товара
     * @return товар, связанный с данной карточкой, или null
     */
    Item findByItemCardId(Long itemCardId);

    /**
     * Находит товар по ID продавца и названию.
     *
     * @param sellerId ID продавца
     * @param name     название товара
     * @return товар, если найден, иначе null
     */
    Item findBySellerIdAndName(Long sellerId, String name);

    /**
     * Находит все товары указанного продавца.
     *
     * @param sellerId ID продавца
     * @return список товаров (может быть пустым)
     */
    List<Item> findBySellerId(Long sellerId);

    /**
     * Находит все товары, ожидающие верификации (verify = false).
     *
     * @return список непроверенных товаров
     */
    List<Item> findByVerifyFalse();

    /**
     * Находит проверенные товары (verify = true) указанного продавца.
     *
     * @param sellerId ID продавца
     * @return список проверенных товаров
     */
    List<Item> findBySellerIdAndVerifyTrue(Long sellerId);

    /**
     * Находит проверенные товары указанного продавца, у которых ещё нет товарной карточки.
     *
     * @param sellerId ID продавца
     * @return список товаров, готовых к созданию карточки
     */
    List<Item> findBySellerIdAndVerifyTrueAndItemCardIsNull(Long sellerId);

    /**
     * Находит товар по идентификатору записи в таблице наличия товара на складе (ItemsAtStorage).
     *
     * @param itemsAtStorageId ID записи items_at_storage
     * @return товар, к которому относится данный остаток
     */
    @Query("SELECT i FROM Item i JOIN i.itemsAtStorages s WHERE s.id = :itemsAtStorageId")
    Item findItemByItemsAtStorageId(@Param("itemsAtStorageId") Long itemsAtStorageId);

    /**
     * Находит все товары указанного продавца с предварительной загрузкой списка остатков на складах.
     * Используется для избежания N+1 запросов при работе с остатками и для избежания LazyInitializationException.
     *
     * @param seller продавец (сущность)
     * @return список товаров с инициализированным полем itemsAtStorages
     */
    @Query("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.itemsAtStorages WHERE i.seller = :seller")
    List<Item> findBySellerWithStorages(@Param("seller") Seller seller);
}