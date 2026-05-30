package ru.angelika.boutique.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.Cart;
import ru.angelika.boutique.model.ItemCard;
import ru.angelika.boutique.repository.CartRepository;
import ru.angelika.boutique.service.CartService;
import ru.angelika.boutique.service.ItemCardService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartUnitTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ItemCardService itemCardService;

    @InjectMocks
    private CartService cartService;

    private static final Long CART_ID = 1L;
    private static final Long ITEM_CARD_ID = 10L;
    private static final Long OTHER_ITEM_CARD_ID = 20L;
    private static final Long FALSE_ID = 99L;
    private static final Double PRICE = 100.0;

    private Cart cart;
    private ItemCard itemCard;
    private ItemCard otherItemCard;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        cart.setId(CART_ID);
        cart.setTotalPrice(0.0);

        itemCard = new ItemCard();
        itemCard.setId(ITEM_CARD_ID);
        itemCard.setPrice(PRICE);

        otherItemCard = new ItemCard();
        otherItemCard.setId(OTHER_ITEM_CARD_ID);
        otherItemCard.setPrice(PRICE);
    }

    @Test
    void getById_Success() {
        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));
        Cart result = cartService.getById(CART_ID);
        assertNotNull(result);
        assertEquals(CART_ID, result.getId());
    }

    @Test
    void getById_NotFound_ThrowsException() {
        when(cartRepository.findById(FALSE_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> cartService.getById(FALSE_ID));
    }

    @Test
    void addCardInCart_Success() {
        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));
        when(itemCardService.get(ITEM_CARD_ID)).thenReturn(itemCard);

        assertDoesNotThrow(() -> cartService.addCardInCart(CART_ID, ITEM_CARD_ID));

        verify(cartRepository).save(cart);
        assertEquals(1, cart.getItemCards().size());
        assertEquals(PRICE, cart.getTotalPrice());
    }

    @Test
    void addCardInCart_AlreadyExists_DoesNothing() {
        cart.getItemCards().add(itemCard);
        cart.setTotalPrice(PRICE);

        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));
        when(itemCardService.get(ITEM_CARD_ID)).thenReturn(itemCard);

        assertDoesNotThrow(() -> cartService.addCardInCart(CART_ID, ITEM_CARD_ID));

        verify(cartRepository, never()).save(any());
        assertEquals(1, cart.getItemCards().size());
        assertEquals(PRICE, cart.getTotalPrice());
    }

    @Test
    void addCardInCart_CartNotFound_ThrowsException() {
        when(cartRepository.findById(FALSE_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> cartService.addCardInCart(FALSE_ID, ITEM_CARD_ID));
        verify(cartRepository, never()).save(any());
    }

    @Test
    void addCardInCart_ItemCardNotFound_ThrowsException() {
        when(itemCardService.get(ITEM_CARD_ID)).thenThrow(new ResourceNotFoundException(ItemCard.class, ITEM_CARD_ID));
        assertThrows(ResourceNotFoundException.class, () -> cartService.addCardInCart(CART_ID, ITEM_CARD_ID));
        verify(cartRepository, never()).save(any());
    }

    @Test
    void deleteCardsFromCart_Success() {
        cart.getItemCards().add(itemCard);
        cart.getItemCards().add(otherItemCard);
        cart.setTotalPrice(PRICE * 2);

        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));

        assertDoesNotThrow(() -> cartService.deleteCardsFromCart(CART_ID, new ArrayList<>(List.of(ITEM_CARD_ID))));

        verify(cartRepository).save(cart);
        assertEquals(1, cart.getItemCards().size());
        assertEquals(otherItemCard.getId(), cart.getItemCards().get(0).getId());
        assertEquals(PRICE, cart.getTotalPrice());
    }

    @Test
    void deleteCardsFromCart_WillCartEmpty_Success() {
        cart.getItemCards().add(itemCard);
        cart.setTotalPrice(PRICE);

        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));

        assertDoesNotThrow(() -> cartService.deleteCardsFromCart(CART_ID, new ArrayList<>(List.of(ITEM_CARD_ID))));

        verify(cartRepository).save(cart);
        assertEquals(0, cart.getItemCards().size());
        assertEquals(0, cart.getTotalPrice());
    }

    @Test
    void deleteCardsFromCart_DoesNothing() {
        cart.getItemCards().add(itemCard);
        cart.getItemCards().add(otherItemCard);
        cart.setTotalPrice(PRICE * 2);

        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));

        assertDoesNotThrow(() -> cartService.deleteCardsFromCart(CART_ID, new ArrayList<>(List.of(6L))));

        verify(cartRepository).save(cart);
        assertEquals(2, cart.getItemCards().size());
    }

    @Test
    void deleteCardsFromCart_ComeEmptyList_DoesNothing() {
        cart.getItemCards().add(itemCard);
        cart.setTotalPrice(PRICE);
        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));

        assertDoesNotThrow(() -> cartService.deleteCardsFromCart(CART_ID, new ArrayList<>()));

        verify(cartRepository).save(cart);
        assertEquals(1, cart.getItemCards().size());
        assertEquals(PRICE, cart.getTotalPrice());
    }

    @Test
    void deleteCardsFromCart_CartNotFound_ThrowsException() {
        when(cartRepository.findById(FALSE_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> cartService.deleteCardsFromCart(FALSE_ID, List.of(ITEM_CARD_ID)));
        verify(cartRepository, never()).save(any());
    }

    @Test
    void deleteCardFromCart_Success() {
        cart.getItemCards().add(itemCard);
        cart.setTotalPrice(PRICE);

        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));

        assertDoesNotThrow(() -> cartService.deleteCardFromCart(CART_ID, ITEM_CARD_ID));

        verify(cartRepository).save(cart);
        assertEquals(0, cart.getItemCards().size());
        assertEquals(0.0, cart.getTotalPrice());
    }

    @Test
    void deleteCardFromCart_CardNotInCart_DoesNothing() {
        cart.getItemCards().add(otherItemCard);
        cart.setTotalPrice(PRICE);

        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));

        assertDoesNotThrow(() -> cartService.deleteCardFromCart(CART_ID, ITEM_CARD_ID));

        verify(cartRepository).save(cart);
        assertEquals(1, cart.getItemCards().size());
        assertEquals(PRICE, cart.getTotalPrice());
    }

    @Test
    void deleteCardFromCart_CartNotFound_ThrowsException() {
        when(cartRepository.findById(FALSE_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> cartService.deleteCardFromCart(FALSE_ID, ITEM_CARD_ID));
        verify(cartRepository, never()).save(any());
    }

}