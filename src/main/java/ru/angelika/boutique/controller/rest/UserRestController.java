package ru.angelika.boutique.controller.rest;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.angelika.boutique.dto.UserDto;
import ru.angelika.boutique.dto.UserGetDto;
import ru.angelika.boutique.model.User;
import ru.angelika.boutique.service.UserService;

@RestController
@RequestMapping("/users")
public class UserRestController {
    private final UserService userService;

    @Autowired
    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public void addUser(@Valid @RequestBody UserDto user) {
        userService.addUser(user);
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getById(id);
    }

}
