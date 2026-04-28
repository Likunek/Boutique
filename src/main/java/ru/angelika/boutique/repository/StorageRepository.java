package ru.angelika.boutique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.angelika.boutique.model.Storage;
public interface StorageRepository extends JpaRepository<Storage, Long> {
}
