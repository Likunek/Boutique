package ru.angelika.boutique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.angelika.boutique.model.Supply;

public interface SupplyRepository extends JpaRepository<Supply, Long> {
}
