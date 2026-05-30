package ru.angelika.boutique.jpa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.angelika.boutique.repository.AuthenticationRepository;
import ru.angelika.boutique.model.Authentication;
import ru.angelika.boutique.model.Role;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class AuthenticationJpaTest {

    @Autowired
    private AuthenticationRepository authenticationRepository;

    private static Authentication authenticationBase;
    private static final String TEST_NUMBER = "12345678900";
    private static final String TEST_PASSWORD = "password";
    private static final Role TEST_ROLE = Role.USER;

    @BeforeEach
    void setUp() {
        Authentication auth = new Authentication();
        auth.setNumber(TEST_NUMBER);
        auth.setPassword(TEST_PASSWORD);
        auth.setRole(TEST_ROLE);
        authenticationBase = authenticationRepository.save(auth);
    }

    @Test
    void findByNumber_ShouldReturnAuthentication_WhenExists() {
        Authentication found = authenticationRepository.findByNumber(TEST_NUMBER);
        assertNotNull(found);
        assertEquals(authenticationBase.getId(), found.getId());
        assertEquals(TEST_NUMBER, found.getNumber());
        assertEquals(TEST_PASSWORD, found.getPassword());
        assertEquals(TEST_ROLE, found.getRole());
    }

    @Test
    void findByNumber_ShouldReturnNull_WhenNotExists() {
        String nonExistentNumber = "9999999999";
        Authentication found = authenticationRepository.findByNumber(nonExistentNumber);
        assertNull(found);
    }

    @AfterEach
    void tearDown() {
        authenticationRepository.deleteAll();
    }
}