package ru.angelika.boutique.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.Cart;
import ru.angelika.boutique.model.ItemCard;
import ru.angelika.boutique.model.User;
import ru.angelika.boutique.repository.CartRepository;

@Slf4j
@Controller
public class CartService {
    private final CartRepository cartRepository;
    private final ItemCardService itemCardService;

    @Autowired
    public CartService(CartRepository cartRepository, ItemCardService itemCardService) {
        this.cartRepository = cartRepository;
        this.itemCardService = itemCardService;
    }

    public Cart getCartById(Long id) {
        return cartRepository.findById(id).orElseThrow(() -> {
            log.error("Cart not found for get, id={}", id);
            return new ResourceNotFoundException(Cart.class, id);
        });
    }

    public void addCardInCart(Long cartId, Long itemCardId) {
        ItemCard itemCard = itemCardService.getItemCard(itemCardId);
        Cart cart = getCartById(cartId);
        if (cart.getItemCards().stream().anyMatch(card -> card.getId().equals(itemCardId))) {
            log.debug("itemCard already added. itemCardId={}, cartId={}", itemCardId, cartId);
            return;
        }
        cart.getItemCards().add(itemCard);
        Double price = cart.getTotalPrice() + itemCard.getPrice();
        cart.setTotalPrice(price);
        cartRepository.save(cart);
        log.debug("Add itemCard in cart. itemCardId={}, cartId={}", itemCardId, cartId);
    }
}
