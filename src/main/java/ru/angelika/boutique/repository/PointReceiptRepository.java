package ru.angelika.boutique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.angelika.boutique.model.PointReceipt;

import java.util.List;

public interface PointReceiptRepository extends JpaRepository<PointReceipt, Long> {
    List<PointReceipt> findByAddress(String address);
    List<PointReceipt> findByCity(String city);
}