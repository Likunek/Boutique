package ru.angelika.boutique.controller.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.angelika.boutique.service.CartService;
import ru.angelika.boutique.service.UserService;

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
        if (!security(id).equals(id)) {
            return "redirect:/welcome";
        }
        model.addAttribute("cart", cartService.getCartById(id));
        return "user-cart";
    }

    @GetMapping("/user/cart/add-card/{id}")
    public String addItemInCart(@PathVariable Long id) {
        Long cartId = security(id);
        cartService.addCardInCart(cartId, id);
        return "redirect:/item-card?role=ROLE_USER";
    }

    private Long security(Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.getByNumber(auth.getName()).getCart().getId();
    }
}
