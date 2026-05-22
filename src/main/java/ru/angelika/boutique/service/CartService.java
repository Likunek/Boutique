package ru.angelika.boutique.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.Cart;
import ru.angelika.boutique.model.ItemCard;
import ru.angelika.boutique.repository.CartRepository;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final ItemCardService itemCardService;


    public Cart getById(Long id) {
        log.debug("Cart get By id={}", id);
        return cartRepository.findById(id).orElseThrow(() -> {
            log.error("Cart not found for get, id={}", id);
            return new ResourceNotFoundException(Cart.class, id);
        });
    }

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

    public void deleteCardsFromCart(Long cartId, List<Long> itemCardId) {
        Cart cart = getById(cartId);
        cart.getItemCards().removeIf(card -> itemCardId.contains(card.getId()));
        Double sum = cart.getItemCards().stream().mapToDouble(ItemCard::getPrice).sum();
        cart.setTotalPrice(sum);
        cartRepository.save(cart);
        log.info("Delete itemCard from cart. itemCardId={}, cartId={}", itemCardId, cartId);
    }

    public void deleteCardFromCart(Long cartId, Long itemCardId) {
        Cart cart = getById(cartId);
        cart.getItemCards().removeIf(card -> card.getId().equals(itemCardId));
        Double sum = cart.getItemCards().stream().mapToDouble(ItemCard::getPrice).sum();
        cart.setTotalPrice(sum);
        cartRepository.save(cart);
        log.info("Delete itemCard from cart. itemCardId={}, cartId={}", itemCardId, cartId);
    }
}
