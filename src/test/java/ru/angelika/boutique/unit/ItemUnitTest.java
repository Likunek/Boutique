package ru.angelika.boutique.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.angelika.boutique.dto.ItemDto;
import ru.angelika.boutique.exception.ResourceExistsException;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.Item;
import ru.angelika.boutique.model.ItemCard;
import ru.angelika.boutique.model.Seller;
import ru.angelika.boutique.repository.ItemCardRepository;
import ru.angelika.boutique.repository.ItemRepository;
import ru.angelika.boutique.service.ItemService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemUnitTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemCardRepository itemCardRepository;

    @InjectMocks
    private ItemService itemService;

    private static final Long SELLER_ID = 1L;
    private static final Long ITEM_ID = 4L;
    private static final Long CARD_ID = 6L;
    private static final Long FALSE_ID = 999L;
    private static final String ITEM_NAME = "Test Item";

    private Seller seller;
    private Item item;
    private ItemDto itemDto;
    private ItemCard itemCard;

    @BeforeEach
    void setUp() {
        seller = new Seller();
        seller.setId(SELLER_ID);

        item = new Item();
        item.setId(ITEM_ID);
        item.setName(ITEM_NAME);
        item.setCostPrice(100.0);
        item.setSquare(0.8);
        item.setWeight(1.5);
        item.setVerify(false);
        item.setSeller(seller);

        itemDto = ItemDto.builder()
                .name(ITEM_NAME)
                .costPrice(100.0)
                .weight(1.5)
                .square(0.5)
                .build();

        itemCard = new ItemCard();
        itemCard.setId(CARD_ID);
        itemCard.setPrice(120.0);
    }

    @Test
    void add_Success() {
        when(itemRepository.findBySellerIdAndName(SELLER_ID, ITEM_NAME)).thenReturn(null);
        assertDoesNotThrow(() -> itemService.add(itemDto, seller));
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void add_DuplicateName_ThrowsException() {
        when(itemRepository.findBySellerIdAndName(SELLER_ID, ITEM_NAME)).thenReturn(item);
        assertThrows(ResourceExistsException.class, () -> itemService.add(itemDto, seller));
        verify(itemRepository, never()).save(any());
    }

    @Test
    void getAll_Success() {
        when(itemRepository.findAll()).thenReturn(List.of(item));
        List<Item> result = itemService.getAll();
        assertEquals(1, result.size());
    }

    @Test
    void getAllVerifyFalse_Success() {
        when(itemRepository.findByVerifyFalse()).thenReturn(List.of(item));
        List<Item> result = itemService.getAllVerifyFalse();
        assertEquals(1, result.size());
    }

    @Test
    void getBySellerId_Success() {
        when(itemRepository.findBySellerIdAndVerifyTrue(SELLER_ID)).thenReturn(List.of(item));
        List<Item> result = itemService.getBySellerId(SELLER_ID);
        assertEquals(1, result.size());
    }

    @Test
    void getAllBySeller_Success() {
        when(itemRepository.findBySellerWithStorages(seller)).thenReturn(List.of(item));
        List<Item> result = itemService.getAllBySellerWithStorages(seller);
        assertEquals(1, result.size());
    }

    @Test
    void getById_Success() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        Item result = itemService.getById(ITEM_ID);
        assertNotNull(result);
        assertEquals(ITEM_ID, result.getId());
    }

    @Test
    void getById_NotFound_ThrowsException() {
        when(itemRepository.findById(FALSE_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> itemService.getById(FALSE_ID));
    }

    @Test
    void getByCardId_Success() {
        when(itemRepository.findByItemCardId(CARD_ID)).thenReturn(item);
        Item result = itemService.getByCardId(CARD_ID);
        assertNotNull(result);
        assertEquals(ITEM_ID, result.getId());
    }

    @Test
    void getByCardId_NotFound_ThrowsException() {
        when(itemRepository.findByItemCardId(CARD_ID)).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> itemService.getByCardId(CARD_ID));
    }

    @Test
    void addItemCard_Success() {
        item.setItemCard(itemCard);
        assertDoesNotThrow(() -> itemService.addItemCard(item));
        verify(itemRepository).save(item);
    }

    @Test
    void updateVerify_Success() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        itemService.updateVerify(ITEM_ID, true);
        assertTrue(item.getVerify());
        verify(itemRepository).save(item);
    }

    @Test
    void updateVerify_ItemNotFound_ThrowsException() {
        when(itemRepository.findById(FALSE_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> itemService.updateVerify(FALSE_ID, true));
    }

    @Test
    void update_Success() {
        item.setItemCard(itemCard);
        when(itemRepository.findBySellerIdAndName(SELLER_ID, ITEM_NAME)).thenReturn(null);
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(itemCardRepository.findById(ITEM_ID)).thenReturn(Optional.of(itemCard));

        assertDoesNotThrow(() -> itemService.update(itemDto, ITEM_ID, SELLER_ID));

        verify(itemCardRepository).save(itemCard);
        verify(itemRepository).save(item);
        assertEquals(120.0, itemCard.getPrice());
    }

    @Test
    void update_WithoutItemCard_Success() {
        when(itemRepository.findBySellerIdAndName(SELLER_ID, ITEM_NAME)).thenReturn(null);
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);

        assertDoesNotThrow(() -> itemService.update(itemDto, ITEM_ID, SELLER_ID));

        verify(itemCardRepository, never()).findById(any());
        verify(itemRepository).save(item);
    }

    @Test
    void update_ItemCardNotFound_ThrowsException() {
        item.setItemCard(itemCard);
        when(itemRepository.findBySellerIdAndName(SELLER_ID, ITEM_NAME)).thenReturn(null);
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(itemCardRepository.findById(ITEM_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> itemService.update(itemDto, ITEM_ID, SELLER_ID));

        verify(itemCardRepository, never()).save(any());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void update_ItemNotFound_ThrowsException() {
        when(itemRepository.findById(FALSE_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> itemService.updateVerify(FALSE_ID, true));
    }

    @Test
    void update_DuplicateName_ThrowsException() {
        Item existingItem = new Item();
        existingItem.setId(FALSE_ID);
        item.setName(ITEM_NAME);
        when(itemRepository.findBySellerIdAndName(SELLER_ID, ITEM_NAME)).thenReturn(existingItem);

        assertThrows(ResourceExistsException.class, () -> itemService.update(itemDto, ITEM_ID, SELLER_ID));
        verify(itemRepository, never()).save(any());
    }

    @Test
    void delete_Success() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        doNothing().when(itemRepository).deleteById(ITEM_ID);
        assertDoesNotThrow(() -> itemService.delete(ITEM_ID));
        verify(itemRepository).deleteById(ITEM_ID);
    }

    @Test
    void delete_NotFound_ThrowsException() {
        when(itemRepository.findById(FALSE_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> itemService.delete(FALSE_ID));
        verify(itemRepository, never()).deleteById(any());
    }

    @Test
    void deleteBySellerId_Success() {
        List<Item> items = List.of(item);
        when(itemRepository.findBySellerId(SELLER_ID)).thenReturn(items);
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        doNothing().when(itemRepository).deleteById(ITEM_ID);

        itemService.deleteBySellerId(SELLER_ID);

        verify(itemRepository).deleteById(ITEM_ID);
    }

    @Test
    void deleteItemCard_Success() {
        item.setItemCard(itemCard);
        when(itemRepository.findByItemCardId(CARD_ID)).thenReturn(item);

        itemService.deleteItemCard(CARD_ID);

        assertNull(item.getItemCard());
        verify(itemRepository).save(item);
    }

    @Test
    void deleteItemCard_ItemNotFound_Success() {
        when(itemRepository.findByItemCardId(CARD_ID)).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> itemService.deleteItemCard(CARD_ID));
        verify(itemRepository, never()).save(any());
    }
}