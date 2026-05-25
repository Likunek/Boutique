package ru.angelika.boutique.controller.view;

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

@Slf4j
@Controller
@RequestMapping
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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

    @GetMapping("/admin/users")
    public String getAll(Model model) {
        model.addAttribute("users", userService.getAll());
        return "admin-users";
    }

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


    @DeleteMapping("/user/profile/{id}")
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

    private boolean security(Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String roleName = auth.getAuthorities().iterator().next().getAuthority();
        if (roleName.equals("ROLE_USER")) {
            return !userService.getByNumber(auth.getName()).getId().equals(id);
        }
        return false;
    }
}
