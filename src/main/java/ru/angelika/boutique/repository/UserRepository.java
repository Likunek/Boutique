package ru.angelika.boutique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.angelika.boutique.model.User;

/**
 * Репозиторий для работы с сущностью {@link User} (покупатели).
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Находит пользователя по имени.
     *
     * @param name имя пользователя
     * @return пользователь, или null
     */
    User findByName(String name);

    /**
     * Находит пользователя по номеру телефона.
     *
     * @param number номер телефона (логин)
     * @return пользователь, или null
     */
    User findByNumber(String number);

    /**
     * Находит пользователя по email.
     *
     * @param email адрес электронной почты
     * @return пользователь, или null
     */
    User findByEmail(String email);

    /**
     * Находит пользователя по ID с предварительной загрузкой:
     * <ul>
     *   <li>списка купленных товаров (items)</li>
     *   <li>для каждого товара — списка отзывов (feedbacks)</li>
     *   <li>для каждого отзыва — автора (user)</li>
     * </ul>
     * Используется для формирования страницы профиля пользователя.
     *
     * @param id идентификатор пользователя
     * @return пользователь с инициализированными коллекциями, или null
     */
    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.items i " +
            "LEFT JOIN FETCH i.feedbacks f " +
            "LEFT JOIN FETCH f.user " +
            "WHERE u.id = :id")
    User findByIdWithItemsAndFeedbacks(@Param("id") Long id);
}