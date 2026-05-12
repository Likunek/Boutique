package ru.angelika.boutique.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class FeedbackDto {
    @Range(min = 1, max = 5)
    private Integer rating;
    private String text;
}
