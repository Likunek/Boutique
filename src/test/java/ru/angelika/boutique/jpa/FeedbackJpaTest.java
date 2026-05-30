package ru.angelika.boutique.jpa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.angelika.boutique.model.*;
import ru.angelika.boutique.repository.AuthenticationRepository;
import ru.angelika.boutique.repository.FeedbackRepository;
import ru.angelika.boutique.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class FeedbackJpaTest {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationRepository authenticationRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        Authentication authentication = new Authentication();
        authentication.setNumber("55566677788");
        authentication.setPassword("feedbackPass");
        authentication.setRole(Role.USER);
        authentication = authenticationRepository.save(authentication);

        testUser = new User();
        testUser.setName("User");
        testUser.setNumber("55566677788");
        testUser.setEmail("feedback@mail.com");
        testUser.setBalance(500.0);
        testUser.setAuthentication(authentication);
        testUser = userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        feedbackRepository.deleteAll();
        userRepository.deleteAll();
        authenticationRepository.deleteAll();
    }

    @Test
    void findByUserId_ShouldReturnFeedbacks_WhenExists() {
        Feedback feedback1 = new Feedback();
        feedback1.setRating(4);
        feedback1.setText("Good");
        feedback1.setUser(testUser);
        feedback1 = feedbackRepository.save(feedback1);

        Feedback feedback2 = new Feedback();
        feedback2.setRating(5);
        feedback2.setText("Excellent!");
        feedback2.setUser(testUser);
        feedback2 = feedbackRepository.save(feedback2);

        List<Feedback> feedbacks = feedbackRepository.findByUserId(testUser.getId());

        assertNotNull(feedbacks);
        assertEquals(2, feedbacks.size());
        assertTrue(feedbacks.stream().anyMatch(f -> f.getRating() == 4 && "Good".equals(f.getText())));
        assertTrue(feedbacks.stream().anyMatch(f -> f.getRating() == 5 && "Excellent!".equals(f.getText())));
    }

    @Test
    void findByUserId_ShouldReturnEmptyList_WhenNotExists() {
        List<Feedback> feedbacks = feedbackRepository.findByUserId(999L);
        assertNotNull(feedbacks);
        assertTrue(feedbacks.isEmpty());
    }
}