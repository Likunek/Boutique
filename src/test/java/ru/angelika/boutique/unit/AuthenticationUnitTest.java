package ru.angelika.boutique.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.angelika.boutique.dto.AuthenticationDto;
import ru.angelika.boutique.exception.PasswordInvalidException;
import ru.angelika.boutique.exception.ResourceExistsException;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.Authentication;
import ru.angelika.boutique.model.Role;
import ru.angelika.boutique.repository.AuthenticationRepository;
import ru.angelika.boutique.service.AuthenticationService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationUnitTest {

    @Mock
    private AuthenticationRepository authenticationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    private static final Long ID = 1L;
    private static final String NUMBER = "89538921299";
    private static final String OTHER_NUMBER = "70008921299";
    private static final String ENCODED_PASSWORD = "encoded";
    private static final String RAW_PASSWORD = "rawPass";
    private static final String NEW_RAW_PASSWORD = "newPass";

    private Authentication authentication;
    private AuthenticationDto dto;

    @BeforeEach
    void setUp() {
        authentication = new Authentication();
        authentication.setId(ID);
        authentication.setNumber(NUMBER);
        authentication.setPassword(ENCODED_PASSWORD);
        authentication.setRole(Role.USER);

        dto = AuthenticationDto.builder()
                .number(OTHER_NUMBER)
                .oldPassword(RAW_PASSWORD)
                .newPassword(NEW_RAW_PASSWORD)
                .build();
    }

    @Test
    void add_Success() {
        Authentication newAuth = new Authentication();
        newAuth.setNumber(NUMBER);
        newAuth.setPassword(RAW_PASSWORD);
        when(authenticationRepository.findByNumber(NUMBER)).thenReturn(null);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);

        authenticationService.add(newAuth);

        assertEquals(ENCODED_PASSWORD, newAuth.getPassword());
        verify(passwordEncoder).encode(RAW_PASSWORD);
        verify(authenticationRepository).save(newAuth);
    }

    @Test
    void add_NumberExists_ThrowsException() {
        Authentication newAuth = new Authentication();
        newAuth.setNumber(NUMBER);
        newAuth.setPassword(RAW_PASSWORD);
        when(authenticationRepository.findByNumber(NUMBER)).thenReturn(authentication);

        assertThrows(ResourceExistsException.class,
                () -> authenticationService.add(newAuth));

        verify(passwordEncoder, never()).encode(RAW_PASSWORD);
        verify(authenticationRepository, never()).save(newAuth);
    }

    @Test
    void findByNumber_Success() {
        when(authenticationRepository.findByNumber(NUMBER)).thenReturn(authentication);

        Authentication result = authenticationService.findByNumber(NUMBER);

        assertNotNull(result);
        assertEquals(authentication.getNumber(), result.getNumber());
        verify(authenticationRepository).findByNumber(NUMBER);
    }

    @Test
    void findByNumber_NotFound_ThrowsException() {
        when(authenticationRepository.findByNumber(NUMBER)).thenReturn(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> authenticationService.findByNumber(NUMBER));

        assertEquals("class ru.angelika.boutique.model.Authentication not found with data: " + NUMBER,
                exception.getMessage());
        verify(authenticationRepository).findByNumber(NUMBER);
    }

    @Test
    void update_Success() {
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(passwordEncoder.encode(NEW_RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD + "new");
        when(authenticationRepository.findByNumber(dto.getNumber())).thenReturn(null);

        authenticationService.update(dto, authentication);

        assertEquals(ENCODED_PASSWORD + "new", authentication.getPassword());
        assertEquals(OTHER_NUMBER, authentication.getNumber());
        verify(authenticationRepository).save(authentication);
    }

    @Test
    void update_numberMatches_Success() {
        dto.setNumber(NUMBER);
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(passwordEncoder.encode(NEW_RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD + "new");

        authenticationService.update(dto, authentication);

        assertEquals(ENCODED_PASSWORD + "new", authentication.getPassword());
        assertEquals(NUMBER, authentication.getNumber());
        verify(authenticationRepository, never()).findByNumber(any());
        verify(authenticationRepository).save(authentication);
    }

    @Test
    void update_NewPasswordBlank_Success() {
        dto.setNewPassword("");
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(authenticationRepository.findByNumber(OTHER_NUMBER)).thenReturn(null);

        authenticationService.update(dto, authentication);

        assertEquals(ENCODED_PASSWORD, authentication.getPassword());
        assertEquals(OTHER_NUMBER, authentication.getNumber());
        verify(passwordEncoder, never()).encode(any());
        verify(authenticationRepository).save(authentication);
    }

    @Test
    void update_NumberAlreadyExists_ThrowsException() {
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(passwordEncoder.encode(NEW_RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD + "new");
        when(authenticationRepository.findByNumber(OTHER_NUMBER)).thenReturn(new Authentication());

        assertThrows(ResourceExistsException.class,
                () -> authenticationService.update(dto, authentication));

        verify(authenticationRepository, never()).save(any());
    }

    @Test
    void update_PasswordInvalid_ThrowsException() {
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        PasswordInvalidException exception = assertThrows(PasswordInvalidException.class,
                () -> authenticationService.update(dto, authentication));

        assertEquals("Your password is incorrect", exception.getMessage());
        verify(authenticationRepository, never()).findByNumber(any());
        verify(authenticationRepository, never()).save(any());
    }

    @Test
    void delete_Success() {
        when(authenticationRepository.findByNumber(NUMBER)).thenReturn(authentication);
        authenticationService.delete(NUMBER);
        verify(authenticationRepository).deleteById(ID);
    }

    @Test
    void delete_NotFound_ThrowsException() {
        when(authenticationRepository.findByNumber(NUMBER)).thenReturn(null);;

        assertThrows(ResourceNotFoundException.class,
                () -> authenticationService.delete(NUMBER));

        verify(authenticationRepository, never()).deleteById(any());
    }

    @Test
    void loadUserByUsername_Success() {
        when(authenticationRepository.findByNumber(NUMBER)).thenReturn(authentication);

        UserDetails userDetails = authenticationService.loadUserByUsername(NUMBER);

        assertNotNull(userDetails);
        assertEquals(authentication.getNumber(), userDetails.getUsername());
        assertEquals(authentication.getPassword(), userDetails.getPassword());
    }

    @Test
    void loadUserByUsername_UserNotFound_ThrowsException() {
        when(authenticationRepository.findByNumber(NUMBER)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () ->  authenticationService.loadUserByUsername(NUMBER));
    }
}