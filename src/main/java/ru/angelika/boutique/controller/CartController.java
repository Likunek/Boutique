package ru.angelika.boutique.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.angelika.boutique.model.User;
import ru.angelika.boutique.service.CartService;
import ru.angelika.boutique.service.UserService;

import java.util.List;

/**
 * Контроллер для управления корзиной пользователя.
 * Позволяет просматривать корзину, добавлять и удалять товары.
 */
@Slf4j
@Controller
@RequestMapping
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final UserService userService;

    /**
     * Отображает страницу корзины пользователя.
     * Проверяет, что указанный ID корзины принадлежит текущему аутентифицированному пользователю.
     *
     * @param id    ID корзины (из пути)
     * @param model модель для передачи атрибута "cart"
     * @return "user-cart" если доступ разрешён, иначе редирект на "/welcome"
     */
    @GetMapping("user/cart/{id}")
    public String getCart(@PathVariable Long id, Model model) {
        User user = security();
        if (!user.getCart().getId().equals(id)) {
            log.warn("User with id={} tried to view cart from someone else's path", user.getId());
            return "redirect:/welcome";
        }
        model.addAttribute("cart", cartService.getById(id));
        return "user-cart";
    }

    /**
     * Добавляет товарную карточку в корзину текущего пользователя.
     *
     * @param id   ID товарной карточки
     * @param card если true – после добавления возвращается на страницу карточки,
     *             иначе – на список всех карточек
     * @return редирект на страницу всех карточек
     */
    @PostMapping("/user/cart/add-card/{id}")
    public String addItemInCart(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean card) {
        Long cartId = security().getCart().getId();
        cartService.addCardInCart(cartId, id);
        if (card) {
            return "redirect:/item-card/" + id + "?role=ROLE_USER";
        }
        return "redirect:/item-card?role=ROLE_USER";
    }

    /**
     * Удаляет несколько товарных карточек из корзины (по списку ID).
     *
     * @param cards список ID карточек для удаления
     * @return редирект на страницу корзины
     */
    @PostMapping("/user/cart/card")
    public String deleteItemsFromCart(@RequestParam List<Long> cards) {
        Long cartId = security().getCart().getId();
        cartService.deleteCardsFromCart(cartId, cards);
        return "redirect:/user/cart/" + cartId;
    }

    /**
     * Удаляет одну товарную карточку из корзины.
     *
     * @param id   ID карточки
     * @param card если true – после удаления возвращается на страницу карточки,
     *             иначе – на список всех карточек
     * @return редирект на старницу всех карточек
     */
    @DeleteMapping("/user/cart/card/{id}")
    public String deleteItemFromCart(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean card) {
        Long cartId = security().getCart().getId();
        cartService.deleteCardFromCart(cartId, id);
        if (card) {
            return "redirect:/item-card/" + id + "?role=ROLE_USER";
        }
        return "redirect:/item-card?role=ROLE_USER";
    }

    /**
     * Вспомогательный метод – получает текущего аутентифицированного пользователя.
     *
     * @return объект {@link User}
     */
    private User security() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.getByNumber(auth.getName());
    }
}