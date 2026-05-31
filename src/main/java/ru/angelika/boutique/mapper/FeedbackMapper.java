package ru.angelika.boutique.mapper;

import ru.angelika.boutique.dto.FeedbackDto;
import ru.angelika.boutique.model.Feedback;
import ru.angelika.boutique.model.User;

/**
 * Маппер для преобразования DTO в сущность {@link Feedback}.
 * Содержит методы для создания отзыва из данных DTO и пользователя.
 */
public class FeedbackMapper {

    /**
     * Преобразует {@link FeedbackDto} в сущность {@link Feedback}.
     *
     * @param feedbackDto DTO с данными отзыва (оценка, текст)
     * @param user        пользователь, оставляющий отзыв
     * @return заполненный объект {@link Feedback}
     */
    public static Feedback toFeedback(FeedbackDto feedbackDto, User user) {
        Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setRating(feedbackDto.getRating());
        feedback.setText(feedbackDto.getText());
        return feedback;
    }
}