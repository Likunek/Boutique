package ru.angelika.boutique.controller.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.angelika.boutique.service.CartService;
import ru.angelika.boutique.service.OrderService;

@Controller
@RequestMapping
public class OrdersController {

    private final OrderService orderService;
    private final CartService cartService;

    @Autowired
    public OrdersController(OrderService orderService, CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    @GetMapping("/admin/orders")
    public String getAllOrders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        return "admin-orders";
    }
    @GetMapping("/user/order/{cartId}")
    public String getFormNewOrder(@PathVariable Long cartId, Model model) {
        model.addAttribute("cartId", cartId);
        model.addAttribute("items", cartService.getCartById(cartId).getItemCards());
        return "user-order";
    }
}
