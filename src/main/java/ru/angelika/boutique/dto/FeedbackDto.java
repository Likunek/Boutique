package ru.angelika.boutique.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class FeedbackDto {

    @NotNull
    private Long userId;
    @Range(min = 1, max = 5)
    private Integer rating;
    private String text;
}
