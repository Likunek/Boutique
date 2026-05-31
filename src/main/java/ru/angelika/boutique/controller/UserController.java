package ru.angelika.boutique.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.angelika.boutique.dto.UpdateEntityDto;
import ru.angelika.boutique.exception.PasswordInvalidException;
import ru.angelika.boutique.exception.ResourceExistsException;
import ru.angelika.boutique.model.User;
import ru.angelika.boutique.service.UserService;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Контроллер для управления профилями пользователей.
 * Пользователь может просматривать и редактировать и удалять свой профиль,
 * администратор – просматривать список всех пользователей и удалять их.
 */
@Slf4j
@Controller
@RequestMapping
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Отображает страницу профиля пользователя.
     * Проверяет, что текущий пользователь имеет доступ (только к своему профилю).
     *
     * @param id    ID пользователя
     * @param model модель
     * @return "user" или редирект на "/welcome"
     */
    @GetMapping("/user/profile/{id}")
    public String userPage(@PathVariable Long id, Model model) {
        if (security(id)) {
            log.warn("User tried to view /user/profile/ with id={} from someone else's path", id);
            return "redirect:/welcome";
        }
        User user = userService.getById(id);
        Set<Long> feedbacksId = userService.checkItemIdWithOwnFeedbacks(id);
        model.addAttribute("feedbacksId", feedbacksId);
        model.addAttribute("orders", userService.getOrders(id));
        model.addAttribute("user", user);
        return "user";
    }

    /**
     * Отображает список всех пользователей для администратора.
     *
     * @param model модель
     * @return "admin-users"
     */
    @GetMapping("/admin/users")
    public String getAll(Model model) {
        model.addAttribute("users", userService.getAll());
        return "admin-users";
    }

    /**
     * Обновляет профиль пользователя (имя, номер, email, пароль).
     * При успешном обновлении выполняется выход (редирект на /logout).
     *
     * @param id                 ID пользователя
     * @param updateEntityDto    DTO с новыми данными
     * @param result             результаты валидации
     * @param redirectAttributes атрибуты для flash-сообщений
     * @return редирект на страницу профиля при ошибке, или на /logout при успехе
     */
    @PutMapping("/user/profile/{id}")
    public String update(@PathVariable Long id, @Valid UpdateEntityDto updateEntityDto,
                         BindingResult result, RedirectAttributes redirectAttributes) {
        if (security(id)) {
            log.warn("User tried to update /user/profile/ with id={} from someone else's path", id);
            return "redirect:/welcome";
        }
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the errors: " +
                    result.getAllErrors().stream()
                            .map(DefaultMessageSourceResolvable::getDefaultMessage)
                            .collect(Collectors.joining(", ")));
            return "redirect:/user/profile/" + id;
        }
        try {
            userService.update(updateEntityDto, id);
        } catch (PasswordInvalidException | ResourceExistsException e) {
            log.error("Error update user profile id={}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
            return "redirect:/user/profile/" + id;
        }
        return "redirect:/logout";
    }

    /**
     * Удаляет пользователя. Для обычного пользователя – только свой аккаунт,
     * для администратора – любого.
     *
     * @param id    ID пользователя
     * @param model модель (не используется)
     * @return редирект на /logout (если пользователь удаляет себя) или на /admin/users, чужой на /welcome
     */
    @DeleteMapping("/users/{id}")
    public String delete(@PathVariable Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String roleName = auth.getAuthorities().iterator().next().getAuthority();
        if (roleName.equals("ROLE_USER")) {
            if (!userService.getByNumber(auth.getName()).getId().equals(id)) {
                log.warn("User tried to delete /user/profile/ with id={} from someone else's path", id);
                return "redirect:/welcome";
            }
            userService.delete(id);
            return "redirect:/logout";
        }
        userService.delete(id);
        return "redirect:/admin/users";
    }

    /**
     * Проверяет, имеет ли пользователь доступ к аккаунту.
     *
     * @param id ID пользователя владельца страницы
     * @return false, если пользователь владелец страницы
     */
    private boolean security(Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String roleName = auth.getAuthorities().iterator().next().getAuthority();
        if (roleName.equals("ROLE_USER")) {
            return !userService.getByNumber(auth.getName()).getId().equals(id);
        }
        return false;
    }
}