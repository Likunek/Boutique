package ru.angelika.boutique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.angelika.boutique.model.ItemsAtStorage;

import java.util.List;

public interface ItemsAtStorageRepository extends JpaRepository<ItemsAtStorage, Long> {
    List<ItemsAtStorage> findByStorageId(Long storageId);
}
