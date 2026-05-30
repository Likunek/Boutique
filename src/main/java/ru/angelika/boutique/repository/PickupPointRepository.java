package ru.angelika.boutique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.angelika.boutique.model.PickupPoint;

import java.util.List;

public interface PickupPointRepository extends JpaRepository<PickupPoint, Long> {
    List<PickupPoint> findByStorageId(Long id);
    List<PickupPoint> findByAddress(String address);
    List<PickupPoint> findByCity(String city);
    @Query("SELECT p FROM PickupPoint p JOIN FETCH p.storage WHERE p.id = :id")
    PickupPoint findByIdWithStorage(@Param("id") Long id);
}