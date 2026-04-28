package ru.angelika.boutique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.angelika.boutique.model.PointReceipt;

public interface PointReceiptRepository extends JpaRepository<PointReceipt, Long> {
}