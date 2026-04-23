package ru.angelika.boutique.repository;

import org.springframework.data.repository.CrudRepository;
import ru.angelika.boutique.model.Seller;

public interface SellerRepository extends CrudRepository<Seller, Integer> {
}