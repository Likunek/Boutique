package ru.angelika.boutique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.angelika.boutique.model.PickupPoint;

import java.util.List;

public interface PickupPointRepository extends JpaRepository<PickupPoint, Long> {
    List<PickupPoint> findByStorageId(Long id);
    List<PickupPoint> findByAddress(String address);
    List<PickupPoint> findByCity(String city);
}