package ru.angelika.boutique.controller.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.angelika.boutique.model.ItemCard;
import ru.angelika.boutique.model.User;
import ru.angelika.boutique.service.CartService;
import ru.angelika.boutique.service.UserService;

import java.util.List;

@Controller
@RequestMapping
public class CartController {
    private final CartService cartService;
    private final UserService userService;

    @Autowired
    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping("user/cart/{id}")
    public String getCart(@PathVariable Long id, Model model) {
        if (!security().getCart().getId().equals(id)) {
            return "redirect:/welcome";
        }
        model.addAttribute("cart", cartService.getCartById(id));
        return "user-cart";
    }

    @PostMapping("/user/cart/add-card/{id}")
    public String addItemInCart(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean card) {
        Long cartId = security().getCart().getId();
        cartService.addCardInCart(cartId, id);
        if (card) {
            return "redirect:/item-card/" + id + "?role=ROLE_USER";
        }
        return "redirect:/item-card?role=ROLE_USER";
    }

    @PostMapping("/user/cart/card")
    public String deleteItemsFromCart(@RequestParam List<Long> cards) {
        Long cartId = security().getCart().getId();
        cartService.deleteCardsFromCart(cartId, cards);
        return "redirect:/user/cart/" + cartId;
    }

    @DeleteMapping("/user/cart/card/{id}")
    public String deleteItemFromCart(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean card) {
        Long cartId = security().getCart().getId();
        cartService.deleteCardFromCart(cartId, id);
        if (card) {
            return "redirect:/item-card/" + id + "?role=ROLE_USER";
        }
        return "redirect:/item-card?role=ROLE_USER";
    }

    private User security() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.getByNumber(auth.getName());
    }
}
