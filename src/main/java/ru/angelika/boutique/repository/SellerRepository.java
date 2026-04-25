package ru.angelika.boutique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.angelika.boutique.model.Seller;

public interface SellerRepository extends JpaRepository<Seller, Long> {
}