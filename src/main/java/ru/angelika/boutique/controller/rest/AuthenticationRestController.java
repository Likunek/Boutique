package ru.angelika.boutique.controller.rest;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.angelika.boutique.model.Authentication;
import ru.angelika.boutique.service.AuthenticationService;

@RestController
@RequestMapping("/authentications")
public class AuthenticationRestController {
    private final AuthenticationService authenticationService;

    @Autowired
    public AuthenticationRestController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
    @PostMapping
    public void addAuthentication(@Valid @RequestBody Authentication authentication) {
        authenticationService.addAuthentication(authentication);
    }
}
