package ru.angelika.boutique.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.Cart;
import ru.angelika.boutique.model.User;
import ru.angelika.boutique.repository.CartRepository;

@Slf4j
@Controller
public class CartService {
    private final CartRepository cartRepository;

    @Autowired
    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public Cart getCartById(Long id) {
        return cartRepository.findById(id).orElseThrow(() -> {
            log.error("Cart not found for get, id={}", id);
            return new ResourceNotFoundException(Cart.class, id);
        });
    }

}
