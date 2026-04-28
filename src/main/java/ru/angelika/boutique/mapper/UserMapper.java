package ru.angelika.boutique.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.angelika.boutique.dto.UserDto;
import ru.angelika.boutique.dto.UserGetDto;
import ru.angelika.boutique.model.Cart;
import ru.angelika.boutique.model.User;

@Component
public class UserMapper {
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserMapper(PasswordEncoder passwordEncoder){
        this.passwordEncoder = passwordEncoder;
    }

    public User toUser(UserDto userDto){
        User user = new User();
        user.setName(userDto.getName());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setNumber(userDto.getNumber());
        if (userDto.getEmail().isBlank() || userDto.getEmail() == null) {
            user.setEmail(null);
        }
        user.setCart(new Cart());
        return user;
    }
    public UserGetDto toGetUser(User user) {
        return UserGetDto.builder()
                .name(user.getName())
                .number(user.getNumber())
                .email(user.getEmail())
                .build();
    }

}