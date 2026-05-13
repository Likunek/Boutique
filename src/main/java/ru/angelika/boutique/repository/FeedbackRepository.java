package ru.angelika.boutique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.angelika.boutique.model.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, Long>  {
}
