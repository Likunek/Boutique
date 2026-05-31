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
import ru.angelika.boutique.model.Seller;
import ru.angelika.boutique.service.SellerService;

import java.util.stream.Collectors;

/**
 * Контроллер для управления профилями продавцов.
 * Продавец может просматривать, редактировать свой профиль и удалять,
 * администратор – просматривать список всех продавцов и удалять их.
 */
@Slf4j
@Controller
@RequestMapping
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    /**
     * Отображает страницу профиля продавца.
     * Проверяет, что текущий продавец имеет доступ (только к своему профилю).
     *
     * @param id    ID продавца
     * @param model модель
     * @return "seller" или редирект на "/welcome"
     */
    @GetMapping("/seller/profile/{id}")
    public String sellerPage(@PathVariable Long id, Model model) {
        if (security(id)) {
            log.warn("Seller tried to view /seller/profile/ with id={} from someone else's path", id);
            return "redirect:/welcome";
        }
        Seller seller = sellerService.getById(id);
        model.addAttribute("seller", seller);
        return "seller";
    }

    /**
     * Публичная страница продавца (доступна для всех ролей).
     *
     * @param id    ID продавца
     * @param role  роль текущего пользователя
     * @param model модель
     * @return "public-seller"
     */
    @GetMapping("/public-seller/{id}")
    public String getPublicPage(@PathVariable Long id, @RequestParam String role, Model model) {
        Seller seller = sellerService.getById(id);
        model.addAttribute("role", role);
        model.addAttribute("seller", seller);
        return "public-seller";
    }

    /**
     * Отображает список всех продавцов для администратора.
     *
     * @param model модель
     * @return "admin-sellers"
     */
    @GetMapping("/admin/sellers")
    public String getAll(Model model) {
        model.addAttribute("sellers", sellerService.getAll());
        return "admin-sellers";
    }

    /**
     * Обновляет профиль продавца (имя, номер, email, пароль).
     * При успешном обновлении выполняется выход (редирект на /logout).
     *
     * @param id                 ID продавца
     * @param updateEntityDto    DTO с новыми данными
     * @param result             результаты валидации
     * @param redirectAttributes атрибуты для flash-сообщений
     * @return редирект на страницу профиля при ошибке, или на /logout при успехе
     */
    @PutMapping("/seller/profile/{id}")
    public String update(@PathVariable Long id, @Valid UpdateEntityDto updateEntityDto,
                         BindingResult result, RedirectAttributes redirectAttributes) {
        if (security(id)) {
            log.warn("Seller tried to update /seller/profile/ with id={} from someone else's path", id);
            return "redirect:/welcome";
        }
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the errors: " +
                    result.getAllErrors().stream()
                            .map(DefaultMessageSourceResolvable::getDefaultMessage)
                            .collect(Collectors.joining(", ")));
            return "redirect:/seller/profile/" + id;
        }
        try {
            sellerService.update(updateEntityDto, id);
        } catch (PasswordInvalidException | ResourceExistsException e) {
            log.error("Error update seller profile id={}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
            return "redirect:/seller/profile/" + id;
        }
        return "redirect:/logout";
    }

    /**
     * Удаляет продавца (доступно для самого продавца и администратора).
     *
     * @param id ID продавца
     * @return продавцу редирект на /logout, администратору на /admin/sellers, продавцу с чужим id на /welcome
     */
    @DeleteMapping("/sellers/{id}")
    public String delete(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String roleName = auth.getAuthorities().iterator().next().getAuthority();
        if (roleName.equals("ROLE_SELLER")) {
            if (!sellerService.getByNumber(auth.getName()).getId().equals(id)) {
                log.warn("Seller tried to delete /sellers/ with id={} from someone else's path", id);
                return "redirect:/welcome";
            }
            sellerService.delete(id);
            return "redirect:/logout";
        }
        sellerService.delete(id);
        return "redirect:/admin/sellers";
    }

    /**
     * Проверяет, имеет ли продавец доступ к своему аккаунту.
     *
     * @param id ID продавца
     * @return true, если текущий продавец не является владельцем страницы
     */
    private boolean security(Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String roleName = auth.getAuthorities().iterator().next().getAuthority();
        if (roleName.equals("ROLE_SELLER")) {
            return !sellerService.getByNumber(auth.getName()).getId().equals(id);
        }
        return false;
    }
}