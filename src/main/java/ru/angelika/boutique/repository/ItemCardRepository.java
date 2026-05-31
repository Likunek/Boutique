package ru.angelika.boutique.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.angelika.boutique.model.ItemCard;

import java.util.List;

/**
 * Репозиторий для работы с сущностью {@link ItemCard}.
 */
public interface ItemCardRepository extends JpaRepository<ItemCard, Long> {

    /**
     * Находит товарные карточки, принадлежащие указанному продавцу, с поддержкой пагинации.
     *
     * @param seller   имя продавца (поле seller денормализовано)
     * @param pageable параметры пагинации и сортировки
     * @return страница с карточками
     */
    Page<ItemCard> findBySeller(String seller, Pageable pageable);

    /**
     * Находит карточки по точному совпадению имени и продавца.
     *
     * @param name   название товара
     * @param seller имя продавца
     * @return список подходящих карточек
     */
    List<ItemCard> findByNameAndSeller(String name, String seller);

    /**
     * Находит карточки по точному совпадению описания и продавца.
     *
     * @param description описание товара
     * @param seller      имя продавца
     * @return список подходящих карточек
     */
    List<ItemCard> findByDescriptionAndSeller(String description, String seller);

    /**
     * Находит товарную карточку, к которой привязан отзыв с указанным ID.
     *
     * @param feedbackId идентификатор отзыва
     * @return карточка товара, содержащая данный отзыв
     */
    @Query("SELECT i FROM ItemCard i JOIN i.feedbacks s WHERE s.id = :feedbackId")
    ItemCard findItemByFeedbackId(@Param("feedbackId") Long feedbackId);

    /**
     * Выполняет полнотекстовый поиск карточек по подстроке в названии или описании (без учёта регистра).
     *
     * @param text     искомая подстрока
     * @param pageable параметры пагинации и сортировки
     * @return страница карточек, у которых name или description содержат указанный текст
     */
    @Query("SELECT i FROM ItemCard i WHERE (LOWER(i.name) LIKE CONCAT('%', :text, '%') OR LOWER(i.description) LIKE CONCAT('%', :text, '%'))")
    Page<ItemCard> findByNameOrDescription(@Param("text") String text, Pageable pageable);
}