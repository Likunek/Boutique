package ru.angelika.boutique.mapper;

import ru.angelika.boutique.dto.FeedbackDto;
import ru.angelika.boutique.model.Feedback;

public class FeedbackMapper {
    public static Feedback toFeedback(FeedbackDto feedbackDto) {
        Feedback feedback = new Feedback();
        feedback.setRating(feedback.getRating());
        feedback.setText(feedback.getText());
        return feedback;
    }
}
