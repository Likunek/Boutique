package ru.angelika.boutique.repository;

import org.springframework.data.repository.CrudRepository;
import ru.angelika.boutique.model.Item;

public interface ItemRepository extends CrudRepository<Item, Integer> {

}
