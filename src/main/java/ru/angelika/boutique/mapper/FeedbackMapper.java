package ru.angelika.boutique.mapper;

import ru.angelika.boutique.dto.FeedbackDto;
import ru.angelika.boutique.model.Feedback;
import ru.angelika.boutique.model.User;

public class FeedbackMapper {
    public static Feedback toFeedback(FeedbackDto feedbackDto , User user) {
        Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setRating(feedbackDto.getRating());
        feedback.setText(feedbackDto.getText());
        return feedback;
    }
}
