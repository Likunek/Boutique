package ru.angelika.boutique.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.Cart;
import ru.angelika.boutique.model.ItemCard;
import ru.angelika.boutique.repository.CartRepository;

import java.util.List;

/**
 * Сервис для работы с корзиной покупок.
 * Позволяет добавлять/удалять товары, получать корзину по ID.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final ItemCardService itemCardService;

    /**
     * Находит корзину по идентификатору.
     *
     * @param id ID корзины
     * @return корзина (Cart)
     * @throws ResourceNotFoundException если корзина не найдена
     */
    public Cart getById(Long id) {
        log.debug("Cart get By id={}", id);
        return cartRepository.findById(id).orElseThrow(() -> {
            log.error("Cart not found for get, id={}", id);
            return new ResourceNotFoundException(Cart.class, id);
        });
    }

    /**
     * Добавляет товарную карточку в корзину.
     * Если товар уже есть в корзине, ничего не делает.
     *
     * @param cartId      ID корзины
     * @param itemCardId  ID товарной карточки
     * @throws ResourceNotFoundException если корзина или карточка не найдены
     */
    public void addCardInCart(Long cartId, Long itemCardId) {
        ItemCard itemCard = itemCardService.get(itemCardId);
        Cart cart = getById(cartId);
        if (cart.getItemCards().stream().anyMatch(card -> card.getId().equals(itemCardId))) {
            log.debug("ItemCard already added. itemCardId={}, cartId={}", itemCardId, cartId);
            return;
        }
        cart.getItemCards().add(itemCard);
        Double price = cart.getTotalPrice() + itemCard.getPrice();
        cart.setTotalPrice(price);
        cartRepository.save(cart);
        log.debug("Add itemCard in cart. itemCardId={}, cartId={}", itemCardId, cartId);
    }

    /**
     * Удаляет несколько товарных карточек из корзины по списку их ID.
     *
     * @param cartId      ID корзины
     * @param itemCardId  список ID карточек для удаления
     * @throws ResourceNotFoundException если корзина не найдена
     */
    public void deleteCardsFromCart(Long cartId, List<Long> itemCardId) {
        Cart cart = getById(cartId);
        cart.getItemCards().removeIf(card -> itemCardId.contains(card.getId()));
        Double sum = cart.getItemCards().stream().mapToDouble(ItemCard::getPrice).sum();
        cart.setTotalPrice(sum);
        cartRepository.save(cart);
        log.info("Delete itemCard from cart. itemCardId={}, cartId={}", itemCardId, cartId);
    }

    /**
     * Удаляет одну товарную карточку из корзины.
     *
     * @param cartId      ID корзины
     * @param itemCardId  ID карточки для удаления
     * @throws ResourceNotFoundException если корзина не найдена
     */
    public void deleteCardFromCart(Long cartId, Long itemCardId) {
        Cart cart = getById(cartId);
        cart.getItemCards().removeIf(card -> card.getId().equals(itemCardId));
        Double sum = cart.getItemCards().stream().mapToDouble(ItemCard::getPrice).sum();
        cart.setTotalPrice(sum);
        cartRepository.save(cart);
        log.info("Delete itemCard from cart. itemCardId={}, cartId={}", itemCardId, cartId);
    }
}