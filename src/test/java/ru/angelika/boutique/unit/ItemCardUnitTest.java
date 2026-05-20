package ru.angelika.boutique.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.angelika.boutique.dto.FeedbackDto;
import ru.angelika.boutique.dto.ItemCardDto;
import ru.angelika.boutique.dto.ItemCardUpdateDto;
import ru.angelika.boutique.exception.ResourceExistsException;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.Item;
import ru.angelika.boutique.model.ItemCard;
import ru.angelika.boutique.model.User;
import ru.angelika.boutique.repository.ItemCardRepository;
import ru.angelika.boutique.service.ItemCardService;
import ru.angelika.boutique.service.ItemService;
import ru.angelika.boutique.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemCardUnitTest {

    @Mock
    private ItemCardRepository itemCardRepository;

    @Mock
    private UserService userService;

    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemCardService itemCardService;

    private static final Long ITEM_CARD_ID = 1L;
    private static final Long ITEM_ID = 4L;
    private static final Long USER_ID = 10L;
    private static final Long FALSE_ID = 99L;
    private static final String SELLER = "Test Seller";
    private static final String NAME = "Test Card";
    private static final String DESCRIPTION = "Test Description";
    private final Pageable pageable = PageRequest.of(0, 10);

    private ItemCard itemCard;
    private ItemCardDto itemCardDto;
    private ItemCardUpdateDto updateDto;
    private Item item;
    private FeedbackDto feedbackDto;
    private User user;

    @BeforeEach
    void setUp() {
        itemCard = new ItemCard();
        itemCard.setId(ITEM_CARD_ID);
        itemCard.setName(NAME);
        itemCard.setDescription(DESCRIPTION);
        itemCard.setPrice(120.0);
        itemCard.setSeller(SELLER);

        item = new Item();
        item.setId(ITEM_ID);
        item.setCostPrice(100.0);

        user = new User();
        user.setId(USER_ID);
        user.setName("Test User");

        itemCardDto = ItemCardDto.builder()
                .name(NAME)
                .description(DESCRIPTION)
                .itemId(ITEM_ID)
                .build();

        updateDto = ItemCardUpdateDto.builder()
                .name("Updated Name")
                .description("Updated Description")
                .build();

        feedbackDto = FeedbackDto.builder()
                .userId(USER_ID)
                .rating(5)
                .text("Great product!")
                .build();
    }

    @Test
    void add_Success() {
        when(itemService.getById(ITEM_ID)).thenReturn(item);
        when(itemCardRepository.save(any(ItemCard.class))).thenReturn(itemCard);
        doNothing().when(itemService).addItemCard(any(Item.class));

        assertDoesNotThrow(() -> itemCardService.add(itemCardDto, SELLER));

        verify(itemCardRepository).save(any(ItemCard.class));
        verify(itemService).addItemCard(item);
    }

    @Test
    void add_ItemCardAlreadyExists_ThrowsException() {
        item.setItemCard(itemCard);
        when(itemService.getById(ITEM_ID)).thenReturn(item);

        assertThrows(ResourceExistsException.class, () -> itemCardService.add(itemCardDto, SELLER));
        verify(itemCardRepository, never()).save(any());
    }

    @Test
    void add_DuplicateNameAndDescription_ThrowsException() {
        when(itemCardRepository.findByNameAndSeller(NAME, SELLER)).thenReturn(new ArrayList<>(List.of(itemCard)));
        when(itemCardRepository.findByDescriptionAndSeller(DESCRIPTION, SELLER)).thenReturn(new ArrayList<>(List.of(itemCard)));

        assertThrows(ResourceExistsException.class, () -> itemCardService.add(itemCardDto, SELLER));
        verify(itemCardRepository, never()).save(any());
    }

    @Test
    void addFeedback_Success() {
        when(itemCardRepository.findById(ITEM_CARD_ID)).thenReturn(Optional.of(itemCard));
        when(userService.getById(USER_ID)).thenReturn(user);

        assertDoesNotThrow(() -> itemCardService.addFeedback(feedbackDto, ITEM_CARD_ID));

        verify(itemCardRepository).save(itemCard);
        assertEquals(1, itemCard.getFeedbacks().size());
        assertEquals(5.0, itemCard.getRating());
    }

    @Test
    void addFeedback_ItemCardNotFound_ThrowsException() {
        when(itemCardRepository.findById(FALSE_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> itemCardService.addFeedback(feedbackDto, FALSE_ID));
        verify(itemCardRepository, never()).save(itemCard);
        verify(userService, never()).getById(USER_ID);
    }

    @Test
    void addFeedback_UserNotFound_ThrowsException() {
        when(itemCardRepository.findById(ITEM_CARD_ID)).thenReturn(Optional.of(itemCard));
        when(userService.getById(USER_ID)).thenThrow(new ResourceNotFoundException(User.class, USER_ID));
        assertThrows(ResourceNotFoundException.class, () -> itemCardService.addFeedback(feedbackDto, ITEM_CARD_ID));
        verify(itemCardRepository, never()).save(itemCard);
    }

    @Test
    void get_Success() {
        when(itemCardRepository.findById(ITEM_CARD_ID)).thenReturn(Optional.of(itemCard));
        ItemCard result = itemCardService.get(ITEM_CARD_ID);
        assertNotNull(result);
        assertEquals(ITEM_CARD_ID, result.getId());
    }

    @Test
    void get_NotFound_ThrowsException() {
        when(itemCardRepository.findById(FALSE_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> itemCardService.get(FALSE_ID));
    }

    @Test
    void getAll_Success() {
        Page<ItemCard> page = new PageImpl<>(List.of(itemCard));
        when(itemCardRepository.findAll(pageable)).thenReturn(page);
        Page<ItemCard> result = itemCardService.getAll(pageable);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getAllBySearch_Success() {
        Page<ItemCard> page = new PageImpl<>(List.of(itemCard));
        when(itemCardRepository.findByNameOrDescription(anyString(), eq(pageable))).thenReturn(page);
        Page<ItemCard> result = itemCardService.getAllBySearch(pageable, "test");
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getAllBySeller_Success() {
        Page<ItemCard> page = new PageImpl<>(List.of(itemCard));
        when(itemCardRepository.findBySeller(eq(SELLER), eq(pageable))).thenReturn(page);
        Page<ItemCard> result = itemCardService.getAllBySeller(pageable, SELLER);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void update_Success() {
        when(itemCardRepository.findById(ITEM_CARD_ID)).thenReturn(Optional.of(itemCard));
        when(itemCardRepository.findByNameAndSeller(updateDto.getName(), SELLER)).thenReturn(new ArrayList<>());
        when(itemCardRepository.findByDescriptionAndSeller(updateDto.getDescription(), SELLER))
                .thenReturn(new ArrayList<>());

        assertDoesNotThrow(() -> itemCardService.update(updateDto, ITEM_CARD_ID, SELLER));

        verify(itemCardRepository).save(itemCard);
        assertEquals(updateDto.getName(), itemCard.getName());
        assertEquals(updateDto.getDescription(), itemCard.getDescription());
    }

    @Test
    void update_ItemCardNotFound_ThrowsException() {
        when(itemCardRepository.findById(FALSE_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> itemCardService.update(updateDto, FALSE_ID, SELLER));
        verify(itemCardRepository, never()).save(itemCard);
    }

    @Test
    void update_DuplicateNameAndDescription_ThrowsException() {
        ItemCard existingItemCard = new ItemCard();
        existingItemCard.setId(3L);
        when(itemCardRepository.findById(ITEM_CARD_ID)).thenReturn(Optional.of(itemCard));
        when(itemCardRepository.findByNameAndSeller(updateDto.getName(), SELLER))
                .thenReturn(new ArrayList<>(List.of(existingItemCard)));
        when(itemCardRepository.findByDescriptionAndSeller(updateDto.getDescription(), SELLER))
                .thenReturn(new ArrayList<>(List.of(existingItemCard)));

        assertThrows(ResourceExistsException.class, () -> itemCardService.update(updateDto, ITEM_CARD_ID, SELLER));
        verify(itemCardRepository, never()).save(itemCard);
    }

    @Test
    void delete_Success() {
        when(itemCardRepository.findById(ITEM_CARD_ID)).thenReturn(Optional.of(itemCard));
        doNothing().when(itemService).deleteItemCard(ITEM_CARD_ID);
        doNothing().when(itemCardRepository).deleteById(ITEM_CARD_ID);

        assertDoesNotThrow(() -> itemCardService.delete(ITEM_CARD_ID));

        verify(itemService).deleteItemCard(ITEM_CARD_ID);
        verify(itemCardRepository).deleteById(ITEM_CARD_ID);
    }

    @Test
    void delete_ItemCardNotFound_ThrowsException() {
        when(itemCardRepository.findById(FALSE_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> itemCardService.delete(FALSE_ID));
        verify(itemService, never()).deleteItemCard(any());
    }
}