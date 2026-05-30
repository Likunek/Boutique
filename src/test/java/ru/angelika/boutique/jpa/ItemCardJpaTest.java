package ru.angelika.boutique.jpa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.angelika.boutique.model.*;
import ru.angelika.boutique.repository.FeedbackRepository;
import ru.angelika.boutique.repository.ItemCardRepository;
import ru.angelika.boutique.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ItemCardJpaTest {

    @Autowired
    private ItemCardRepository itemCardRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private ItemCard testCard;
    private final String SELLER_NAME = "Test Seller";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setName("Feedback User");
        testUser.setNumber("12345678901");
        testUser.setEmail("user@example.com");
        testUser.setBalance(100.0);
        testUser = userRepository.save(testUser);

        testCard = new ItemCard();
        testCard.setName("Gold ring");
        testCard.setDescription("The best gold ring");
        testCard.setPrice(99.99);
        testCard.setSeller(SELLER_NAME);
        testCard = itemCardRepository.save(testCard);
    }

    @AfterEach
    void tearDown() {
        itemCardRepository.deleteAll();
        feedbackRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void findBySeller_ShouldReturnPageOfCards() {
        ItemCard card2 = new ItemCard();
        card2.setName("Another Item");
        card2.setDescription("Description");
        card2.setPrice(49.99);
        card2.setSeller(SELLER_NAME);
        itemCardRepository.save(card2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<ItemCard> result = itemCardRepository.findBySeller(SELLER_NAME, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(c -> c.getSeller().equals(SELLER_NAME)));
    }

    @Test
    void findByNameAndSeller_ShouldReturnMatchingCards() {
        List<ItemCard> result = itemCardRepository.findByNameAndSeller("Gold ring", SELLER_NAME);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCard.getId(), result.get(0).getId());
    }

    @Test
    void findByNameAndSeller_ShouldReturnEmptyList_WhenNoMatch() {
        List<ItemCard> result = itemCardRepository.findByNameAndSeller("Non", SELLER_NAME);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByDescriptionAndSeller_ShouldReturnMatchingCards() {
        List<ItemCard> result = itemCardRepository.findByDescriptionAndSeller("The best gold ring", SELLER_NAME);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCard.getId(), result.get(0).getId());
    }

    @Test
    void findByDescriptionAndSeller_ShouldReturnEmptyList_WhenNoMatch() {
        List<ItemCard> result = itemCardRepository.findByDescriptionAndSeller("No such description", SELLER_NAME);
        assertTrue(result.isEmpty());
    }

    @Test
    void findItemByFeedbackId_ShouldReturnItemCard() {
        Feedback feedback = new Feedback();
        feedback.setRating(5);
        feedback.setText("Norm");
        feedback.setUser(testUser);
        feedback = feedbackRepository.save(feedback);

        testCard.getFeedbacks().add(feedback);
        testCard = itemCardRepository.save(testCard);

        ItemCard found = itemCardRepository.findItemByFeedbackId(feedback.getId());
        assertNotNull(found);
        assertEquals(testCard.getId(), found.getId());
    }

    @Test
    void findItemByFeedbackId_ShouldReturnNull_WhenNotExists() {
        ItemCard found = itemCardRepository.findItemByFeedbackId(999L);
        assertNull(found);
    }

    @Test
    void findByNameOrDescription_ShouldFindByName() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ItemCard> result = itemCardRepository.findByNameOrDescription("gold", pageable);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testCard.getId(), result.getContent().get(0).getId());
    }

    @Test
    void findByNameOrDescription_ShouldFindByDescription() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ItemCard> result = itemCardRepository.findByNameOrDescription("best", pageable);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testCard.getId(), result.getContent().get(0).getId());
    }

    @Test
    void findByNameOrDescription_ShouldReturnEmpty_WhenNoMatch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ItemCard> result = itemCardRepository.findByNameOrDescription("xxx", pageable);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}