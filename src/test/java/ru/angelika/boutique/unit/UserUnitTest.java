package ru.angelika.boutique.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.angelika.boutique.dto.AuthenticationDto;
import ru.angelika.boutique.dto.UpdateEntityDto;
import ru.angelika.boutique.dto.UserDto;
import ru.angelika.boutique.exception.PasswordInvalidException;
import ru.angelika.boutique.exception.ResourceExistsException;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.*;
import ru.angelika.boutique.repository.*;
import ru.angelika.boutique.service.AuthenticationService;
import ru.angelika.boutique.service.UserService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemCardRepository itemCardRepository;

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private UserService userService;
    private static final Long id = 1L;
    private static final Long falseId = 99L;
    private static final String falseNumber = "99999999999";
    private final Cart cart = new Cart();
    private User user;
    private UserDto userDto;
    private UpdateEntityDto updateDto;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .name("Test")
                .number("89538921299")
                .email("test@gmail.com")
                .build();

        user = new User();
        user.setId(id);
        user.setName("Test");
        user.setNumber("89538921299");
        user.setEmail("test@gmail.com");
        user.setCart(cart);

        updateDto = UpdateEntityDto.builder()
                .name("Jane")
                .number("70008921299")
                .email("jane@gmail.com")
                .oldPassword("oldPass")
                .newPassword("newPass")
                .build();
    }

    @Test
    void addUser_Success() {
        when(userRepository.findByName(userDto.getName())).thenReturn(null);
        when(userRepository.findByNumber(userDto.getNumber())).thenReturn(null);
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(null);
        when(cartRepository.save(new Cart())).thenReturn(cart);
        when(authenticationService.findByNumber(userDto.getNumber())).thenReturn(new Authentication());

        assertDoesNotThrow(() -> userService.addUser(userDto));

        verify(cartRepository).save(any(Cart.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void addUser_DuplicateName_ThrowsException() {
        when(userRepository.findByName(userDto.getName())).thenReturn(user);

        ResourceExistsException exception = assertThrows(ResourceExistsException.class,
                () -> userService.addUser(userDto));
        assertEquals("class ru.angelika.boutique.model.User with data "
                + userDto.getName() + " already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void addUser_DuplicateNumber_ThrowsException() {
        when(userRepository.findByName(userDto.getName())).thenReturn(null);
        when(userRepository.findByNumber(userDto.getNumber())).thenReturn(user);

        assertThrows(ResourceExistsException.class, () -> userService.addUser(userDto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void addUser_DuplicateEmail_ThrowsException() {
        when(userRepository.findByName(userDto.getName())).thenReturn(null);
        when(userRepository.findByNumber(userDto.getNumber())).thenReturn(null);
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(user);

        assertThrows(ResourceExistsException.class, () -> userService.addUser(userDto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void addUser_AuthenticationNull_ThrowsException() {
        when(userRepository.findByName(userDto.getName())).thenReturn(null);
        when(userRepository.findByNumber(userDto.getNumber())).thenReturn(null);
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(null);
        when(authenticationService.findByNumber(userDto.getNumber()))
                .thenThrow(new ResourceNotFoundException(Authentication.class, userDto.getNumber()));

        assertThrows(ResourceNotFoundException.class, () -> userService.addUser(userDto));
        verify(userRepository, never()).save(any(User.class));
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void getById_Success() {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        User result = userService.getById(id);
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
    }

    @Test
    void getById_NotFound_ThrowsException() {
        when(userRepository.findById(falseId)).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> userService.getById(falseId));
        assertEquals("class ru.angelika.boutique.model.User not found with id: " + falseId, exception.getMessage());
    }

    @Test
    void checkOwnFeedbacks_Success() {
        Feedback feedback = new Feedback();
        feedback.setId(10L);
        feedback.setUser(user);
        ItemCard itemCard = new ItemCard();
        itemCard.setId(5L);
        itemCard.setFeedbacks(List.of(feedback));
        user.setItems(Set.of(itemCard));
        when(userRepository.findByIdWithItemsAndFeedbacks(id)).thenReturn(user);

        Set<Long> result = userService.checkItemIdWithOwnFeedbacks(id);

        assertEquals(1, result.size());
        assertTrue(result.contains(itemCard.getId()));
    }

    @Test
    void checkOwnFeedbacks_FeedbacksEmpty_Success() {
        user.setItems(Set.of(new ItemCard()));
        when(userRepository.findByIdWithItemsAndFeedbacks(id)).thenReturn(user);

        Set<Long> result = userService.checkItemIdWithOwnFeedbacks(id);

        assertEquals(0, result.size());
    }

    @Test
    void checkOwnFeedbacks_ItemsEmpty_Success() {
        when(userRepository.findByIdWithItemsAndFeedbacks(id)).thenReturn(user);

        Set<Long> result = userService.checkItemIdWithOwnFeedbacks(id);

        assertEquals(0, result.size());
    }

    @Test
    void checkOwnFeedbacks_UserNotFound_ThrowsException() {
        when(userRepository.findByIdWithItemsAndFeedbacks(falseId)).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> userService.checkItemIdWithOwnFeedbacks(falseId));
    }

    @Test
    void getByNumber_Success() {
        when(userRepository.findByNumber(userDto.getNumber())).thenReturn(user);
        User result = userService.getByNumber(userDto.getNumber());
        assertNotNull(result);
        assertEquals(user.getNumber(), result.getNumber());
    }

    @Test
    void getByNumber_NotFound_ThrowsException() {
        when(userRepository.findByNumber(falseNumber)).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> userService.getByNumber(falseNumber));
    }

    @Test
    void getOrders_Success() {
        when(orderRepository.findByUserIdAndStatusNot(id, Status.RECEIVED)).thenReturn(List.of(new Order()));
        List<Order> result = userService.getOrders(id);
        assertEquals(1, result.size());
    }

    @Test
    void getAllUsers_Success() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        List<User> result = userService.getAllUsers();
        assertEquals(1, result.size());
    }

    @Test
    void getCardsId_Success() {
        ItemCard itemCard = new ItemCard();
        itemCard.setId(5L);
        cart.setItemCards(List.of(itemCard));
        when(userRepository.findByNumber(userDto.getNumber())).thenReturn(user);

        List<Long> result = userService.getCardsId(userDto.getNumber());
        assertEquals(1, result.size());
        assertTrue(result.contains(itemCard.getId()));
    }

    @Test
    void getCardsId_ItemsEmpty_Success() {
        when(userRepository.findByNumber(userDto.getNumber())).thenReturn(user);

        List<Long> result = userService.getCardsId(userDto.getNumber());
        assertEquals(0, result.size());
    }

    @Test
    void getCardsId_UserNotFound_ThrowsException() {
        when(userRepository.findByNumber(falseNumber)).thenReturn(null);
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.getCardsId(falseNumber));
        assertEquals("class ru.angelika.boutique.model.User not found with data: " + falseNumber , exception.getMessage());
    }

    @Test
    void updateUser_Success() {
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.findByName(updateDto.getName())).thenReturn(null);
        when(userRepository.findByNumber(updateDto.getNumber())).thenReturn(null);
        when(userRepository.findByEmail(updateDto.getEmail())).thenReturn(null);
        doNothing().when(authenticationService).updateData(any(AuthenticationDto.class), any());

        assertDoesNotThrow(() -> userService.updateUser(updateDto, id));
        verify(userRepository).save(user);
        verify(authenticationService).updateData(any(AuthenticationDto.class), any());
    }

    @Test
    void updateUser_EntitiesEquals_Success() {
        UpdateEntityDto dto = UpdateEntityDto.builder()
                .name(user.getName())
                .number(user.getNumber())
                .email(user.getEmail())
                .oldPassword("oldPass")
                .build();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        doNothing().when(authenticationService).updateData(any(AuthenticationDto.class), any());

        assertDoesNotThrow(() -> userService.updateUser(dto, id));

        verify(userRepository, never()).findByName(any());
        verify(userRepository, never()).findByNumber(any());
        verify(userRepository, never()).findByEmail(any());
        verify(userRepository).save(user);
        verify(authenticationService).updateData(any(AuthenticationDto.class), any());
    }

    @Test
    void updateUser_NameAlreadyExists_ThrowsException() {
        User existingUser = new User();
        existingUser.setName(updateDto.getName());
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.findByName(updateDto.getName())).thenReturn(existingUser);

        assertThrows(ResourceExistsException.class, () -> userService.updateUser(updateDto, id));
        verify(userRepository, never()).save(any());
        verify(authenticationService, never()).updateData(any(), any());
    }

    @Test
    void updateUser_NumberAlreadyExists_ThrowsException() {
        User existingUser = new User();
        existingUser.setNumber(updateDto.getName());
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.findByName(updateDto.getName())).thenReturn(null);
        when(userRepository.findByNumber(updateDto.getNumber())).thenReturn(existingUser);

        assertThrows(ResourceExistsException.class, () -> userService.updateUser(updateDto, id));
        verify(userRepository, never()).save(any());
        verify(authenticationService, never()).updateData(any(), any());
    }

    @Test
    void updateUser_EmailAlreadyExists_ThrowsException() {
        User existingUser = new User();
        existingUser.setEmail(updateDto.getEmail());
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.findByName(updateDto.getName())).thenReturn(null);
        when(userRepository.findByNumber(updateDto.getNumber())).thenReturn(null);
        when(userRepository.findByEmail(updateDto.getEmail())).thenReturn(existingUser);

        assertThrows(ResourceExistsException.class, () -> userService.updateUser(updateDto, id));
        verify(userRepository, never()).save(any());
        verify(authenticationService, never()).updateData(any(AuthenticationDto.class), any());
    }
    @Test
    void updateUser_PasswordInvalid_ThrowsException() {
        User existingUser = new User();
        existingUser.setEmail(updateDto.getEmail());
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.findByName(updateDto.getName())).thenReturn(null);
        when(userRepository.findByNumber(updateDto.getNumber())).thenReturn(null);
        when(userRepository.findByEmail(updateDto.getEmail())).thenReturn(null);
        doThrow(new PasswordInvalidException("Your password is incorrect"))
                .when(authenticationService).updateData(any(AuthenticationDto.class), any());

        assertThrows(PasswordInvalidException.class, () -> userService.updateUser(updateDto, id));
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_Success() {
        Feedback feedback = new Feedback();
        feedback.setId(10L);
        feedback.setUser(user);
        ItemCard itemCard = new ItemCard();
        itemCard.setFeedbacks(new ArrayList<>());
        itemCard.getFeedbacks().add(feedback);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        doNothing().when(authenticationService).deleteAuthentication(user.getNumber());
        when(orderRepository.findByUserId(id)).thenReturn(List.of(new Order()));
        when(feedbackRepository.findByUserId(id)).thenReturn(List.of(feedback));
        when(itemCardRepository.findItemByFeedbackId(feedback.getId())).thenReturn(itemCard);

        assertDoesNotThrow(() -> userService.deleteUser(id));
        assertEquals(0, itemCard.getFeedbacks().size());
        verify(userRepository).deleteById(id);
        verify(feedbackRepository).deleteAll(anyList());
        verify(itemCardRepository).save(itemCard);
        verify(orderRepository).deleteAll(anyList());
    }

    @Test
    void deleteUser_ItemCardNull_Success() {
        Feedback feedback = new Feedback();
        feedback.setId(10L);
        feedback.setUser(user);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        doNothing().when(authenticationService).deleteAuthentication(user.getNumber());
        when(orderRepository.findByUserId(id)).thenReturn(List.of(new Order()));
        when(feedbackRepository.findByUserId(id)).thenReturn(List.of(feedback));
        when(itemCardRepository.findItemByFeedbackId(feedback.getId())).thenReturn(null);

        assertDoesNotThrow(() -> userService.deleteUser(id));
        verify(itemCardRepository, never()).save(any());
        verify(userRepository).deleteById(id);
        verify(feedbackRepository).deleteAll(anyList());
        verify(orderRepository).deleteAll(anyList());
    }

    @Test
    void deleteUser_FeedbacksEmpty_Success() {

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        doNothing().when(authenticationService).deleteAuthentication(user.getNumber());
        when(orderRepository.findByUserId(id)).thenReturn(List.of(new Order()));
        when(feedbackRepository.findByUserId(id)).thenReturn(List.of());

        assertDoesNotThrow(() -> userService.deleteUser(id));
        verify(itemCardRepository, never()).findItemByFeedbackId(any());
        verify(userRepository).deleteById(id);
        verify(feedbackRepository).deleteAll(anyList());
        verify(orderRepository).deleteAll(anyList());
    }

    @Test
    void deleteUser_UserNotFound_ThrowsException() {
        when(userRepository.findById(falseId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(falseId));
    }
}