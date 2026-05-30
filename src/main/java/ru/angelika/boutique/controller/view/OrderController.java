package ru.angelika.boutique.controller.view;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.angelika.boutique.dto.OrderDto;
import ru.angelika.boutique.model.Cart;
import ru.angelika.boutique.model.User;
import ru.angelika.boutique.service.CartService;
import ru.angelika.boutique.service.OrderService;
import ru.angelika.boutique.service.PickupPointService;
import ru.angelika.boutique.service.UserService;
@Slf4j
@Controller
@RequestMapping
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final UserService userService;
    private final PickupPointService pickupPointService;

    @PostMapping("/user/order")
    public String add(@Valid OrderDto orderDto, @RequestParam Long cartId, RedirectAttributes redirectAttributes) {
        User user = security();
        if (!user.getCart().getId().equals(cartId)) {
            log.warn("User with id={} tried to place order from someone else's path", user.getId());
            return "redirect:/welcome";
        }
        if (orderService.checkPaymentUser(orderDto, user) < 0) {
            log.warn("User does have insufficient funds to pay, userId={}", user.getId());
            redirectAttributes.addFlashAttribute("errorMessage", "Insufficient funds!");
            return "redirect:/user/order/" + cartId;
        }
        orderService.add(orderDto, user, cartId);
        return "redirect:/user/cart/" + cartId;
    }

    @GetMapping("/admin/orders")
    public String getAll(Model model) {
        model.addAttribute("orders", orderService.getAll());
        return "admin-orders";
    }

    @GetMapping("/user/order/{cartId}")
    public String getFormNewOrder(@PathVariable Long cartId, Model model) {
        User user = security();
        if (!user.getCart().getId().equals(cartId)) {
            log.warn("User with id={} tried to view order from someone else's path", user.getId());
            return "redirect:/welcome";
        }
        Cart cart = cartService.getById(cartId);
        model.addAttribute("cartId", cartId);
        model.addAttribute("userBalance", user.getBalance());
        model.addAttribute("totalPrice", cart.getTotalPrice());
        model.addAttribute("pickupPoints", pickupPointService.getAll());
        model.addAttribute("items", cart.getItemCards());
        return "user-order";
    }

    private User security() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.getByNumber(auth.getName());
    }
}
