package ru.angelika.boutique.jpa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.angelika.boutique.model.*;
import ru.angelika.boutique.repository.AuthenticationRepository;
import ru.angelika.boutique.repository.FeedbackRepository;
import ru.angelika.boutique.repository.ItemCardRepository;
import ru.angelika.boutique.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserJpaTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemCardRepository itemCardRepository;

    @Autowired
    private AuthenticationRepository authenticationRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        Authentication authentication = new Authentication();
        authentication.setNumber("12345678900");
        authentication.setPassword("encodedPassword");
        authentication.setRole(Role.USER);
        authentication = authenticationRepository.save(authentication);

        testUser = new User();
        testUser.setName("Test User");
        testUser.setNumber("12345678900");
        testUser.setEmail("nadin@gmail.com");
        testUser.setBalance(1000.0);
        testUser.setAuthentication(authentication);
        testUser = userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        feedbackRepository.deleteAll();
        itemCardRepository.deleteAll();
        userRepository.deleteAll();
        authenticationRepository.deleteAll();
    }

    @Test
    void findByName_ShouldReturnUser_WhenExists() {
        User found = userRepository.findByName("Test User");
        assertNotNull(found);
        assertEquals(testUser.getId(), found.getId());
        assertEquals("Test User", found.getName());
    }

    @Test
    void findByName_ShouldReturnNull_WhenNotExists() {
        User found = userRepository.findByName("Nonexistent");
        assertNull(found);
    }

    @Test
    void findByNumber_ShouldReturnUser_WhenExists() {
        User found = userRepository.findByNumber("12345678900");
        assertNotNull(found);
        assertEquals(testUser.getId(), found.getId());
        assertEquals("12345678900", found.getNumber());
    }

    @Test
    void findByNumber_ShouldReturnNull_WhenNotExists() {
        User found = userRepository.findByNumber("99999999999");
        assertNull(found);
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenExists() {
        User found = userRepository.findByEmail("nadin@gmail.com");
        assertNotNull(found);
        assertEquals(testUser.getId(), found.getId());
        assertEquals("nadin@gmail.com", found.getEmail());
    }

    @Test
    void findByEmail_ShouldReturnNull_WhenNotExists() {
        User found = userRepository.findByEmail("none@example.com");
        assertNull(found);
    }

    @Test
    void findByIdWithItemsAndFeedbacks_ShouldLoadCollections() {
        ItemCard itemCard = new ItemCard();
        itemCard.setName("Test Item");
        itemCard.setPrice(100.0);
        itemCard.setSeller("Test Seller");
        itemCard = itemCardRepository.save(itemCard);

        Feedback feedback = new Feedback();
        feedback.setRating(5);
        feedback.setText("Great!");
        feedback.setUser(testUser);
        feedback = feedbackRepository.save(feedback);

        itemCard.getFeedbacks().add(feedback);
        itemCardRepository.save(itemCard);

        Set<ItemCard> items = new HashSet<>();
        items.add(itemCard);
        testUser.setItems(items);
        userRepository.save(testUser);

        User loadedUser = userRepository.findByIdWithItemsAndFeedbacks(testUser.getId());
        assertNotNull(loadedUser);
        assertNotNull(loadedUser.getItems());
        assertEquals(1, loadedUser.getItems().size());

        Set<ItemCard> loadedItems = loadedUser.getItems();
        ItemCard loadedItem = loadedItems.iterator().next();

        assertNotNull(loadedItem.getFeedbacks());
        assertEquals(1, loadedItem.getFeedbacks().size());

        Feedback loadedFeedback = loadedItem.getFeedbacks().iterator().next();
        assertEquals(5, loadedFeedback.getRating());
        assertEquals("Great!", loadedFeedback.getText());
        assertNotNull(loadedFeedback.getUser());
        assertEquals(testUser.getId(), loadedFeedback.getUser().getId());
    }
}