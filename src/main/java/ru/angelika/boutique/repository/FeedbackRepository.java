package ru.angelika.boutique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.angelika.boutique.model.Feedback;

import java.util.List;

/**
 * Репозиторий для работы с сущностью {@link Feedback}.
 */
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    /**
     * Находит все отзывы, оставленные пользователем с указанным ID.
     *
     * @param id идентификатор пользователя (User.id)
     * @return список отзывов (может быть пустым, если пользователь ничего не оставлял)
     */
    List<Feedback> findByUserId(Long id);
}