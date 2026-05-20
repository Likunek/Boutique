package ru.angelika.boutique.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.angelika.boutique.dto.AuthenticationDto;
import ru.angelika.boutique.dto.UpdateEntityDto;
import ru.angelika.boutique.exception.PasswordInvalidException;
import ru.angelika.boutique.exception.ResourceExistsException;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.Authentication;
import ru.angelika.boutique.model.Seller;
import ru.angelika.boutique.repository.SellerRepository;
import ru.angelika.boutique.service.AuthenticationService;
import ru.angelika.boutique.service.ItemService;
import ru.angelika.boutique.service.SellerService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerUnitTest {

    @Mock
    private ItemService itemService;

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private SellerService sellerService;

    private static final Long id = 1L;
    private static final Long falseId = 99L;
    private static final String falseNumber = "99999999999";
    private static final String falseName = "Unknown";
    private Seller seller;
    private UpdateEntityDto updateDto;

    @BeforeEach
    void setUp() {
        seller = new Seller();
        seller.setId(id);
        seller.setName("Test Seller");
        seller.setNumber("89538921299");
        seller.setEmail("seller@test.com");

        updateDto = UpdateEntityDto.builder()
                .name("Updated Seller")
                .number("70008921299")
                .email("updated@test.com")
                .oldPassword("oldPass")
                .newPassword("newPass")
                .build();
    }

    @Test
    void addSeller_Success() {
        when(sellerRepository.findByName(seller.getName())).thenReturn(null);
        when(sellerRepository.findByNumber(seller.getNumber())).thenReturn(null);
        when(sellerRepository.findByEmail(seller.getEmail())).thenReturn(null);
        when(authenticationService.findByNumber(seller.getNumber())).thenReturn(any(Authentication.class));

        assertDoesNotThrow(() -> sellerService.add(seller));

        verify(sellerRepository).save(seller);
    }

    @Test
    void addSeller_DuplicateName_ThrowsException() {
        when(sellerRepository.findByName(seller.getName())).thenReturn(seller);

        ResourceExistsException exception = assertThrows(ResourceExistsException.class,
                () -> sellerService.add(seller));

        assertEquals("class ru.angelika.boutique.model.Seller with data "
                + seller.getName() + " already exists", exception.getMessage());
        verify(sellerRepository, never()).save(any());
    }

    @Test
    void addSeller_DuplicateNumber_ThrowsException() {
        when(sellerRepository.findByName(seller.getName())).thenReturn(null);
        when(sellerRepository.findByNumber(seller.getNumber())).thenReturn(seller);

        assertThrows(ResourceExistsException.class, () -> sellerService.add(seller));

        verify(sellerRepository, never()).save(any());
    }

    @Test
    void addSeller_DuplicateEmail_ThrowsException() {
        when(sellerRepository.findByName(seller.getName())).thenReturn(null);
        when(sellerRepository.findByNumber(seller.getNumber())).thenReturn(null);
        when(sellerRepository.findByEmail(seller.getEmail())).thenReturn(seller);

        assertThrows(ResourceExistsException.class, () -> sellerService.add(seller));

        verify(sellerRepository, never()).save(any());
    }

    @Test
    void addSeller_AuthenticationNotFound_ThrowsException() {
        when(sellerRepository.findByName(seller.getName())).thenReturn(null);
        when(sellerRepository.findByNumber(seller.getNumber())).thenReturn(null);
        when(sellerRepository.findByEmail(seller.getEmail())).thenReturn(null);
        when(authenticationService.findByNumber(seller.getNumber()))
                .thenThrow(new ResourceNotFoundException(Authentication.class, seller.getNumber()));

        assertThrows(ResourceNotFoundException.class, () -> sellerService.add(seller));

        verify(sellerRepository, never()).save(any());
    }

    @Test
    void getById_Success() {
        when(sellerRepository.findById(id)).thenReturn(Optional.of(seller));
        Seller result = sellerService.getById(id);

        assertNotNull(result);

        assertEquals(seller.getId(), result.getId());
    }

    @Test
    void getById_NotFound_ThrowsException() {
        when(sellerRepository.findById(falseId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> sellerService.getById(falseId));

        assertEquals("class ru.angelika.boutique.model.Seller not found with id: " + falseId, exception.getMessage());
    }

    @Test
    void getByNumber_Success() {
        when(sellerRepository.findByNumber(seller.getNumber())).thenReturn(seller);
        Seller result = sellerService.getByNumber(seller.getNumber());

        assertNotNull(result);

        assertEquals(seller.getNumber(), result.getNumber());
    }

    @Test
    void getByNumber_NotFound_ThrowsException() {
        when(sellerRepository.findByNumber(falseNumber)).thenReturn(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> sellerService.getByNumber(falseNumber));

        assertEquals("class ru.angelika.boutique.model.Seller not found with data: " + falseNumber, exception.getMessage());
    }

    @Test
    void getBySellerName_Success() {
        when(sellerRepository.findByName(seller.getName())).thenReturn(seller);
        Seller result = sellerService.getByName(seller.getName());

        assertNotNull(result);

        assertEquals(seller.getName(), result.getName());
    }

    @Test
    void getBySellerName_NotFound_ThrowsException() {
        when(sellerRepository.findByName(falseName)).thenReturn(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> sellerService.getByName(falseName));

        assertEquals("class ru.angelika.boutique.model.Seller not found with data: " + falseName, exception.getMessage());
    }

    @Test
    void getAllSeller_Success() {
        when(sellerRepository.findAll()).thenReturn(List.of(seller));

        List<Seller> result = sellerService.getAll();

        assertEquals(1, result.size());
    }

    @Test
    void updateSeller_Success() {
        when(sellerRepository.findById(id)).thenReturn(Optional.of(seller));
        when(sellerRepository.findByName(updateDto.getName())).thenReturn(null);
        when(sellerRepository.findByNumber(updateDto.getNumber())).thenReturn(null);
        when(sellerRepository.findByEmail(updateDto.getEmail())).thenReturn(null);
        doNothing().when(authenticationService).updateData(any(AuthenticationDto.class), any());

        assertDoesNotThrow(() -> sellerService.update(updateDto, id));

        verify(sellerRepository).save(seller);
        verify(authenticationService).updateData(any(AuthenticationDto.class), any());
    }

    @Test
    void updateSeller_EntitiesEquals_Success() {
        UpdateEntityDto sameDto = UpdateEntityDto.builder()
                .name(seller.getName())
                .number(seller.getNumber())
                .email(seller.getEmail())
                .oldPassword("oldPass")
                .build();
        when(sellerRepository.findById(id)).thenReturn(Optional.of(seller));
        doNothing().when(authenticationService).updateData(any(AuthenticationDto.class), any());

        assertDoesNotThrow(() -> sellerService.update(sameDto, id));

        verify(sellerRepository, never()).findByName(any());
        verify(sellerRepository, never()).findByNumber(any());
        verify(sellerRepository, never()).findByEmail(any());
        verify(sellerRepository).save(seller);
        verify(authenticationService).updateData(any(AuthenticationDto.class), any());
    }

    @Test
    void updateSeller_NameAlreadyExists_ThrowsException() {
        Seller existingSeller = new Seller();
        existingSeller.setName(seller.getName());
        when(sellerRepository.findById(id)).thenReturn(Optional.of(seller));
        when(sellerRepository.findByName(updateDto.getName())).thenReturn(existingSeller);

        assertThrows(ResourceExistsException.class, () -> sellerService.update(updateDto, id));

        verify(sellerRepository, never()).save(any());
        verify(authenticationService, never()).updateData(any(), any());
    }

    @Test
    void updateSeller_NumberAlreadyExists_ThrowsException() {
        Seller existingSeller = new Seller();
        existingSeller.setNumber(seller.getNumber());
        when(sellerRepository.findById(id)).thenReturn(Optional.of(seller));
        when(sellerRepository.findByName(updateDto.getName())).thenReturn(null);
        when(sellerRepository.findByNumber(updateDto.getNumber())).thenReturn(existingSeller);

        assertThrows(ResourceExistsException.class, () -> sellerService.update(updateDto, id));

        verify(sellerRepository, never()).save(any());
        verify(authenticationService, never()).updateData(any(), any());
    }

    @Test
    void updateSeller_EmailAlreadyExists_ThrowsException() {
        Seller existingSeller = new Seller();
        existingSeller.setEmail(seller.getEmail());
        when(sellerRepository.findById(id)).thenReturn(Optional.of(seller));
        when(sellerRepository.findByName(updateDto.getName())).thenReturn(null);
        when(sellerRepository.findByNumber(updateDto.getNumber())).thenReturn(null);
        when(sellerRepository.findByEmail(updateDto.getEmail())).thenReturn(existingSeller);

        assertThrows(ResourceExistsException.class, () -> sellerService.update(updateDto, id));

        verify(sellerRepository, never()).save(any());
        verify(authenticationService, never()).updateData(any(), any());
    }

    @Test
    void updateSeller_PasswordInvalid_ThrowsException() {
        when(sellerRepository.findById(id)).thenReturn(Optional.of(seller));
        when(sellerRepository.findByName(updateDto.getName())).thenReturn(null);
        when(sellerRepository.findByNumber(updateDto.getNumber())).thenReturn(null);
        when(sellerRepository.findByEmail(updateDto.getEmail())).thenReturn(null);
        doThrow(new PasswordInvalidException("Your password is incorrect"))
                .when(authenticationService).updateData(any(AuthenticationDto.class), any());

        assertThrows(PasswordInvalidException.class, () -> sellerService.update(updateDto, id));

        verify(sellerRepository, never()).save(any());
    }

    @Test
    void deleteSeller_Success() {
        when(sellerRepository.findById(id)).thenReturn(Optional.of(seller));
        doNothing().when(authenticationService).deleteAuthentication(seller.getNumber());
        doNothing().when(itemService).deleteBySellerId(id);

        assertDoesNotThrow(() -> sellerService.delete(id));

        verify(authenticationService).deleteAuthentication(seller.getNumber());
        verify(itemService).deleteBySellerId(id);
        verify(sellerRepository).deleteById(id);
    }

    @Test
    void deleteSeller_AuthenticationNotFound_ThrowsException() {
        when(sellerRepository.findById(id)).thenReturn(Optional.of(seller));
        doThrow(new ResourceNotFoundException(Authentication.class, seller.getNumber()))
                .when(authenticationService).deleteAuthentication(seller.getNumber());

        assertThrows(ResourceNotFoundException.class, () -> sellerService.delete(id));

        verify(sellerRepository, never()).deleteById(id);
        verify(itemService, never()).deleteBySellerId(seller.getId());
    }

    @Test
    void deleteSeller_NotFound_ThrowsException() {
        when(sellerRepository.findById(falseId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> sellerService.delete(falseId));

        assertEquals("class ru.angelika.boutique.model.Seller not found with id: " + falseId, exception.getMessage());
        verify(authenticationService, never()).deleteAuthentication(any());
        verify(itemService, never()).deleteBySellerId(any());
        verify(sellerRepository, never()).deleteById(any());
    }
}