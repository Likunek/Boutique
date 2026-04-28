package ru.angelika.boutique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.angelika.boutique.model.ItemsAtStorage;

public interface ItemsAtStorageRepository extends JpaRepository<ItemsAtStorage, Long> {
}
