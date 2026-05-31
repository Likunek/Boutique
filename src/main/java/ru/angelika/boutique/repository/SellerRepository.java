package ru.angelika.boutique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.angelika.boutique.model.Seller;

/**
 * Репозиторий для работы с сущностью {@link Seller} (продавец).
 */
public interface SellerRepository extends JpaRepository<Seller, Long> {

    /**
     * Находит продавца по имени.
     *
     * @param name имя продавца
     * @return продавец, или null если не найден
     */
    Seller findByName(String name);

    /**
     * Находит продавца по номеру телефона (логину).
     *
     * @param number номер телефона
     * @return продавец, или null
     */
    Seller findByNumber(String number);

    /**
     * Находит продавца по email.
     *
     * @param email адрес электронной почты
     * @return продавец, или null
     */
    Seller findByEmail(String email);
}