package ru.angelika.boutique.repository;

import org.springframework.data.repository.CrudRepository;
import ru.angelika.boutique.model.Сustomer;

public interface UserRepository extends CrudRepository<Сustomer, Integer> {
}