package ru.angelika.boutique.repository;

import org.springframework.data.repository.CrudRepository;
import ru.angelika.boutique.model.Storage;
public interface StorageRepository extends CrudRepository<Storage, Integer> {
}
