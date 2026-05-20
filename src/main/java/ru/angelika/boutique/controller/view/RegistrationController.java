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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.angelika.boutique.dto.UserDto;
import ru.angelika.boutique.mapper.UserMapper;
import ru.angelika.boutique.service.AuthenticationService;
import ru.angelika.boutique.service.SellerService;
import ru.angelika.boutique.service.UserService;

import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping
@RequiredArgsConstructor
public class RegistrationController {

    private final UserService userService;
    private final SellerService sellerService;
    private final AuthenticationService authenticationService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registration")
    public String registration() {
        return "registration";
    }

    @PostMapping("/registration")
    public String adduser(@Valid UserDto user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("errorMessage", "Please correct the errors: " +
                    result.getAllErrors().stream()
                            .map(DefaultMessageSourceResolvable::getDefaultMessage)
                            .collect(Collectors.joining(", ")));
            return "registration";
        }
        try {
            authenticationService.addAuthentication(UserMapper.toAuthentication(user));
            switch (user.getRole()) {
                case USER -> userService.add(user);
                case SELLER -> sellerService.add(UserMapper.toSeller(user));
            }
            model.addAttribute("successMessage", "You have successfully registered!");
            return "redirect:/login";
        } catch (Exception e) {
            log.error("Error add account '{}': {}", user.getName(), e.getMessage(), e);
            model.addAttribute("message", "User exists");
            return "registration";
        }
    }

    @GetMapping("/welcome")
    public String welcome(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String phone = auth.getName();
        String roleName = auth.getAuthorities().iterator().next().getAuthority();
        model.addAttribute("role", roleName);
        switch (roleName) {
            case "ROLE_USER" -> {
                model.addAttribute("userId", userService.getByNumber(phone).getId());
                model.addAttribute("userRole", "user");
            }
            case "ROLE_SELLER" -> {
                model.addAttribute("userId", sellerService.getByNumber(phone).getId());
                model.addAttribute("userRole", "seller");
            }
            case "ROLE_ADMIN" -> {
                model.addAttribute("userId", 1);
                model.addAttribute("userRole", "admin");
            }
            default -> log.error("Error authentication account");
        }
        return "welcome";
    }

    @GetMapping("/admin/profile/1")
    public String adminPage() {
        return "admin";
    }

}