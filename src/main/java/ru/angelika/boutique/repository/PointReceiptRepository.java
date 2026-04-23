package ru.angelika.boutique.repository;

import org.springframework.data.repository.CrudRepository;
import ru.angelika.boutique.model.PointReceipt;

public interface PointReceiptRepository extends CrudRepository<PointReceipt, Integer> {
}