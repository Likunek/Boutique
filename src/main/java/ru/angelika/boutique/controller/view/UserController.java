package ru.angelika.boutique.controller.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.angelika.boutique.model.User;
import ru.angelika.boutique.service.UserService;

@Controller
@RequestMapping("profile/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("{id}")
    public String userPage(@PathVariable Long id, Model model)
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String phone = auth.getName();
        User currentUser = userService.getByNumber(phone);
        if (!currentUser.getId().equals(id)) {
            return "redirect:/welcome";
        }

        User user = userService.getById(id);
        model.addAttribute("user", user);
        return "user";
    }
}
