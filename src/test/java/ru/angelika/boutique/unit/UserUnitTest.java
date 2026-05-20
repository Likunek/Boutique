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
    private static final Long ID = 1L;
    private static final Long FALSE_ID = 99L;
    private static final String FALSE_NUMBER = "99999999999";
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
        user.setId(ID);
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
    void add_Success() {
        when(userRepository.findByName(userDto.getName())).thenReturn(null);
        when(userRepository.findByNumber(userDto.getNumber())).thenReturn(null);
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(null);
        when(cartRepository.save(new Cart())).thenReturn(cart);
        when(authenticationService.findByNumber(userDto.getNumber())).thenReturn(new Authentication());

        assertDoesNotThrow(() -> userService.add(userDto));

        verify(cartRepository).save(any(Cart.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void add_DuplicateName_ThrowsException() {
        when(userRepository.findByName(userDto.getName())).thenReturn(user);

        ResourceExistsException exception = assertThrows(ResourceExistsException.class,
                () -> userService.add(userDto));

        assertEquals("class ru.angelika.boutique.model.User with data "
                + userDto.getName() + " already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void add_DuplicateNumber_ThrowsException() {
        when(userRepository.findByName(userDto.getName())).thenReturn(null);
        when(userRepository.findByNumber(userDto.getNumber())).thenReturn(user);

        assertThrows(ResourceExistsException.class, () -> userService.add(userDto));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void add_DuplicateEmail_ThrowsException() {
        when(userRepository.findByName(userDto.getName())).thenReturn(null);
        when(userRepository.findByNumber(userDto.getNumber())).thenReturn(null);
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(user);

        assertThrows(ResourceExistsException.class, () -> userService.add(userDto));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void add_AuthenticationNull_ThrowsException() {
        when(userRepository.findByName(userDto.getName())).thenReturn(null);
        when(userRepository.findByNumber(userDto.getNumber())).thenReturn(null);
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(null);
        when(authenticationService.findByNumber(userDto.getNumber()))
                .thenThrow(new ResourceNotFoundException(Authentication.class, userDto.getNumber()));

        assertThrows(ResourceNotFoundException.class, () -> userService.add(userDto));

        verify(userRepository, never()).save(any(User.class));
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void getById_Success() {
        when(userRepository.findById(ID)).thenReturn(Optional.of(user));

        User result = userService.getById(ID);

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
    }

    @Test
    void getById_NotFound_ThrowsException() {
        when(userRepository.findById(FALSE_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.getById(FALSE_ID));

        assertEquals("class ru.angelika.boutique.model.User not found with ID: " + FALSE_ID, exception.getMessage());
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
        when(userRepository.findByIdWithItemsAndFeedbacks(ID)).thenReturn(user);

        Set<Long> result = userService.checkItemIdWithOwnFeedbacks(ID);

        assertEquals(1, result.size());
        assertTrue(result.contains(itemCard.getId()));
    }

    @Test
    void checkOwnFeedbacks_FeedbacksEmpty_Success() {
        user.setItems(Set.of(new ItemCard()));
        when(userRepository.findByIdWithItemsAndFeedbacks(ID)).thenReturn(user);

        Set<Long> result = userService.checkItemIdWithOwnFeedbacks(ID);

        assertEquals(0, result.size());
    }

    @Test
    void checkOwnFeedbacks_ItemsEmpty_Success() {
        when(userRepository.findByIdWithItemsAndFeedbacks(ID)).thenReturn(user);

        Set<Long> result = userService.checkItemIdWithOwnFeedbacks(ID);

        assertEquals(0, result.size());
    }

    @Test
    void checkOwnFeedbacks_UserNotFound_ThrowsException() {
        when(userRepository.findByIdWithItemsAndFeedbacks(FALSE_ID)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> userService.checkItemIdWithOwnFeedbacks(FALSE_ID));
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
        when(userRepository.findByNumber(FALSE_NUMBER)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> userService.getByNumber(FALSE_NUMBER));
    }

    @Test
    void getOrders_Success() {
        when(orderRepository.findByUserIdAndStatusNot(ID, Status.RECEIVED)).thenReturn(List.of(new Order()));

        List<Order> result = userService.getOrders(ID);

        assertEquals(1, result.size());
    }

    @Test
    void getAll_Success() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> result = userService.getAll();

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
        when(userRepository.findByNumber(FALSE_NUMBER)).thenReturn(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.getCardsId(FALSE_NUMBER));

        assertEquals("class ru.angelika.boutique.model.User not found with data: " + FALSE_NUMBER, exception.getMessage());
    }

    @Test
    void update_Success() {
        when(userRepository.findById(ID)).thenReturn(Optional.of(user));
        when(userRepository.findByName(updateDto.getName())).thenReturn(null);
        when(userRepository.findByNumber(updateDto.getNumber())).thenReturn(null);
        when(userRepository.findByEmail(updateDto.getEmail())).thenReturn(null);
        doNothing().when(authenticationService).update(any(AuthenticationDto.class), any());

        assertDoesNotThrow(() -> userService.update(updateDto, ID));

        verify(userRepository).save(user);
        verify(authenticationService).update(any(AuthenticationDto.class), any());
    }

    @Test
    void update_EntitiesEquals_Success() {
        UpdateEntityDto dto = UpdateEntityDto.builder()
                .name(user.getName())
                .number(user.getNumber())
                .email(user.getEmail())
                .oldPassword("oldPass")
                .build();
        when(userRepository.findById(ID)).thenReturn(Optional.of(user));
        doNothing().when(authenticationService).update(any(AuthenticationDto.class), any());

        assertDoesNotThrow(() -> userService.update(dto, ID));

        verify(userRepository, never()).findByName(any());
        verify(userRepository, never()).findByNumber(any());
        verify(userRepository, never()).findByEmail(any());
        verify(userRepository).save(user);
        verify(authenticationService).update(any(AuthenticationDto.class), any());
    }

    @Test
    void update_NameAlreadyExists_ThrowsException() {
        User existingUser = new User();
        existingUser.setName(updateDto.getName());
        when(userRepository.findById(ID)).thenReturn(Optional.of(user));
        when(userRepository.findByName(updateDto.getName())).thenReturn(existingUser);

        assertThrows(ResourceExistsException.class, () -> userService.update(updateDto, ID));

        verify(userRepository, never()).save(any());
        verify(authenticationService, never()).update(any(), any());
    }

    @Test
    void update_NumberAlreadyExists_ThrowsException() {
        User existingUser = new User();
        existingUser.setNumber(updateDto.getName());
        when(userRepository.findById(ID)).thenReturn(Optional.of(user));
        when(userRepository.findByName(updateDto.getName())).thenReturn(null);
        when(userRepository.findByNumber(updateDto.getNumber())).thenReturn(existingUser);

        assertThrows(ResourceExistsException.class, () -> userService.update(updateDto, ID));

        verify(userRepository, never()).save(any());
        verify(authenticationService, never()).update(any(), any());
    }

    @Test
    void update_EmailAlreadyExists_ThrowsException() {
        User existingUser = new User();
        existingUser.setEmail(updateDto.getEmail());
        when(userRepository.findById(ID)).thenReturn(Optional.of(user));
        when(userRepository.findByName(updateDto.getName())).thenReturn(null);
        when(userRepository.findByNumber(updateDto.getNumber())).thenReturn(null);
        when(userRepository.findByEmail(updateDto.getEmail())).thenReturn(existingUser);

        assertThrows(ResourceExistsException.class, () -> userService.update(updateDto, ID));

        verify(userRepository, never()).save(any());
        verify(authenticationService, never()).update(any(AuthenticationDto.class), any());
    }
    @Test
    void update_PasswordInvalid_ThrowsException() {
        when(userRepository.findById(ID)).thenReturn(Optional.of(user));
        when(userRepository.findByName(updateDto.getName())).thenReturn(null);
        when(userRepository.findByNumber(updateDto.getNumber())).thenReturn(null);
        when(userRepository.findByEmail(updateDto.getEmail())).thenReturn(null);
        doThrow(new PasswordInvalidException("Your password is incorrect"))
                .when(authenticationService).update(any(AuthenticationDto.class), any());

        assertThrows(PasswordInvalidException.class, () -> userService.update(updateDto, ID));

        verify(userRepository, never()).save(any());
    }

    @Test
    void delete_Success() {
        Feedback feedback = new Feedback();
        feedback.setId(10L);
        feedback.setUser(user);
        ItemCard itemCard = new ItemCard();
        itemCard.setFeedbacks(new ArrayList<>());
        itemCard.getFeedbacks().add(feedback);

        when(userRepository.findById(ID)).thenReturn(Optional.of(user));
        doNothing().when(authenticationService).delete(user.getNumber());
        when(orderRepository.findByUserId(ID)).thenReturn(List.of(new Order()));
        when(feedbackRepository.findByUserId(ID)).thenReturn(List.of(feedback));
        when(itemCardRepository.findItemByFeedbackId(feedback.getId())).thenReturn(itemCard);

        assertDoesNotThrow(() -> userService.delete(ID));

        assertEquals(0, itemCard.getFeedbacks().size());
        verify(userRepository).deleteById(ID);
        verify(feedbackRepository).deleteAll(anyList());
        verify(itemCardRepository).save(itemCard);
        verify(orderRepository).deleteAll(anyList());
    }

    @Test
    void delete_ItemCardNull_Success() {
        Feedback feedback = new Feedback();
        feedback.setId(10L);
        feedback.setUser(user);

        when(userRepository.findById(ID)).thenReturn(Optional.of(user));
        doNothing().when(authenticationService).delete(user.getNumber());
        when(orderRepository.findByUserId(ID)).thenReturn(List.of(new Order()));
        when(feedbackRepository.findByUserId(ID)).thenReturn(List.of(feedback));
        when(itemCardRepository.findItemByFeedbackId(feedback.getId())).thenReturn(null);

        assertDoesNotThrow(() -> userService.delete(ID));

        verify(itemCardRepository, never()).save(any());
        verify(userRepository).deleteById(ID);
        verify(feedbackRepository).deleteAll(anyList());
        verify(orderRepository).deleteAll(anyList());
    }

    @Test
    void delete_FeedbacksEmpty_Success() {
        when(userRepository.findById(ID)).thenReturn(Optional.of(user));
        doNothing().when(authenticationService).delete(user.getNumber());
        when(orderRepository.findByUserId(ID)).thenReturn(List.of(new Order()));
        when(feedbackRepository.findByUserId(ID)).thenReturn(List.of());

        assertDoesNotThrow(() -> userService.delete(ID));

        verify(itemCardRepository, never()).findItemByFeedbackId(any());
        verify(userRepository).deleteById(ID);
        verify(feedbackRepository).deleteAll(anyList());
        verify(orderRepository).deleteAll(anyList());
    }

    @Test
    void delete_AuthenticationNotFound_ThrowsException() {
        when(userRepository.findById(ID)).thenReturn(Optional.of(user));
        doThrow(new ResourceNotFoundException(Authentication.class, user.getNumber()))
                .when(authenticationService).delete(user.getNumber());

        assertThrows(ResourceNotFoundException.class, () -> userService.delete(ID));

        verify(userRepository, never()).deleteById(ID);
        verify(feedbackRepository, never()).deleteAll(anyList());
        verify(orderRepository, never()).deleteAll(anyList());
    }

    @Test
    void delete_UserNotFound_ThrowsException() {
        when(userRepository.findById(FALSE_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.delete(FALSE_ID));

        verify(userRepository, never()).deleteById(ID);
        verify(feedbackRepository, never()).deleteAll(anyList());
        verify(orderRepository, never()).deleteAll(anyList());
    }
}