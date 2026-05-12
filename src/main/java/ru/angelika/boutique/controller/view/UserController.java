package ru.angelika.boutique.controller.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.angelika.boutique.model.User;
import ru.angelika.boutique.service.UserService;

@Controller
@RequestMapping
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user/profile/{id}")
    public String userPage(@PathVariable Long id, Model model) {
        if (security(id)) {
            return "redirect:/welcome";
        }
        User user = userService.getById(id);
        model.addAttribute("orders", userService.getOrders(id));
        model.addAttribute("user", user);
        return "user";
    }

    @GetMapping("/admin/users")
    public String getAllUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin-users";
    }

    @DeleteMapping("/users/{id}")
    public String getAllUsers(@PathVariable Long id, Model model) {
        if (security(id)) {
            return "redirect:/welcome";
        }
        userService.deleteUser(id);
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
