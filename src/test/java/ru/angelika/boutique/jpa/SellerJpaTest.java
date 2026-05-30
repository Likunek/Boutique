package ru.angelika.boutique.jpa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.angelika.boutique.model.Authentication;
import ru.angelika.boutique.model.Role;
import ru.angelika.boutique.model.Seller;
import ru.angelika.boutique.repository.AuthenticationRepository;
import ru.angelika.boutique.repository.SellerRepository;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class SellerJpaTest {

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private AuthenticationRepository authenticationRepository;

    private Seller testSeller;

    @BeforeEach
    void setUp() {
        Authentication authentication = new Authentication();
        authentication.setNumber("89876543210");
        authentication.setPassword("password");
        authentication.setRole(Role.SELLER);
        authentication = authenticationRepository.save(authentication);

        testSeller = new Seller();
        testSeller.setName("Test Seller");
        testSeller.setNumber("89876543210");
        testSeller.setEmail("seller@mail.com");
        testSeller.setAuthentication(authentication);
        testSeller = sellerRepository.save(testSeller);
    }

    @AfterEach
    void tearDown() {
        sellerRepository.deleteAll();
        authenticationRepository.deleteAll();
    }

    @Test
    void findByName_ShouldReturnSeller_WhenExists() {
        Seller found = sellerRepository.findByName("Test Seller");
        assertNotNull(found);
        assertEquals(testSeller.getId(), found.getId());
        assertEquals("Test Seller", found.getName());
    }

    @Test
    void findByName_ShouldReturnNull_WhenNotExists() {
        Seller found = sellerRepository.findByName("Null Seller");
        assertNull(found);
    }

    @Test
    void findByNumber_ShouldReturnSeller_WhenExists() {
        Seller found = sellerRepository.findByNumber("89876543210");
        assertNotNull(found);
        assertEquals(testSeller.getId(), found.getId());
        assertEquals("89876543210", found.getNumber());
    }

    @Test
    void findByNumber_ShouldReturnNull_WhenNotExists() {
        Seller found = sellerRepository.findByNumber("00000000000");
        assertNull(found);
    }

    @Test
    void findByEmail_ShouldReturnSeller_WhenExists() {
        Seller found = sellerRepository.findByEmail("seller@mail.com");
        assertNotNull(found);
        assertEquals(testSeller.getId(), found.getId());
        assertEquals("seller@mail.com", found.getEmail());
    }

    @Test
    void findByEmail_ShouldReturnNull_WhenNotExists() {
        Seller found = sellerRepository.findByEmail("non@mail.com");
        assertNull(found);
    }
}