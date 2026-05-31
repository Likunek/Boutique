package ru.angelika.boutique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.angelika.boutique.model.PickupPoint;

import java.util.List;

/**
 * Репозиторий для работы с сущностью {@link PickupPoint} (пункты выдачи заказов).
 */
public interface PickupPointRepository extends JpaRepository<PickupPoint, Long> {

    /**
     * Находит все пункты выдачи, обслуживаемые указанным складом.
     *
     * @param id ID склада
     * @return список ПВЗ
     */
    List<PickupPoint> findByStorageId(Long id);

    /**
     * Находит пункты выдачи по точному адресу.
     *
     * @param address адрес
     * @return список ПВЗ
     */
    List<PickupPoint> findByAddress(String address);

    /**
     * Находит пункты выдачи в указанном городе.
     *
     * @param city название города
     * @return список ПВЗ в этом городе
     */
    List<PickupPoint> findByCity(String city);

    /**
     * Находит пункт выдачи по ID с предварительной загрузкой связанного склада.
     * Используется для избежания LazyInitializationException.
     *
     * @param id идентификатор ПВЗ
     * @return ПВЗ с инициализированным полем storage, или null
     */
    @Query("SELECT p FROM PickupPoint p JOIN FETCH p.storage WHERE p.id = :id")
    PickupPoint findByIdWithStorage(@Param("id") Long id);
}