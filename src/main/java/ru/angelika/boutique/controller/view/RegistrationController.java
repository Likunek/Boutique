package ru.angelika.boutique.controller.view;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.angelika.boutique.dto.UserDto;
import ru.angelika.boutique.mapper.UserMapper;
import ru.angelika.boutique.service.AuthenticationService;
import ru.angelika.boutique.service.SellerService;
import ru.angelika.boutique.service.UserService;

@Controller
@RequestMapping()
public class RegistrationController {

    private final UserService userService;
    private final SellerService sellerService;
    private final AuthenticationService authenticationService;

    @Autowired
    public RegistrationController(UserService userService, SellerService sellerService,
                                  AuthenticationService authenticationService) {
        this.userService = userService;
        this.sellerService = sellerService;
        this.authenticationService = authenticationService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
    @GetMapping("/registration")
    public String registration()
    {
        return "registration";
    }

    @PostMapping("/registration")
    public String adduser(UserDto user, Model model)
    {
        try
        {
            switch (user.getRole()) {
                case USER -> userService.addUser(user);
                case SELLER -> sellerService.addSeller(UserMapper.toSeller(user));
            }
            authenticationService.addAuthentication(UserMapper.toAuthentication(user));
            return "redirect:/login";
        }
        catch (Exception ex)
        {
            model.addAttribute("message", "User exists");
            return "registration";
        }
    }

}