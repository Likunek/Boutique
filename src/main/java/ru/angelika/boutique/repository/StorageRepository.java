package ru.angelika.boutique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.angelika.boutique.model.Storage;

import java.util.List;

/**
 * Репозиторий для работы с сущностью {@link Storage} (склады).
 */
public interface StorageRepository extends JpaRepository<Storage, Long> {

    /**
     * Находит склады с указанным адресом.
     *
     * @param address адрес склада
     * @return список складов
     */
    List<Storage> findByAddress(String address);

    /**
     * Находит склады в указанном городе.
     *
     * @param city название города
     * @return список складов
     */
    List<Storage> findByCity(String city);
}