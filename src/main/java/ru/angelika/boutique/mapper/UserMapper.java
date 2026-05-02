package ru.angelika.boutique.mapper;

import ru.angelika.boutique.dto.UserDto;
import ru.angelika.boutique.dto.UserGetDto;
import ru.angelika.boutique.model.Authentication;
import ru.angelika.boutique.model.Cart;
import ru.angelika.boutique.model.Seller;
import ru.angelika.boutique.model.User;


public class UserMapper {

    public static User toUser(UserDto userDto){
        User user = new User();
        user.setName(userDto.getName());
        user.setNumber(userDto.getNumber());
        user.setEmail(userDto.getEmail());
        if (userDto.getEmail().isBlank() || userDto.getEmail() == null) {
            user.setEmail(null);
        }
        user.setCart(new Cart());
        return user;
    }

    public static Authentication toAuthentication(UserDto userDto) {
        Authentication authentication = new Authentication();
        authentication.setNumber(userDto.getNumber());
        authentication.setPassword(userDto.getPassword());
        authentication.setNumber(userDto.getNumber());
        authentication.setRole(userDto.getRole());
        return authentication;
    }

    public static Seller toSeller(UserDto userDto) {
        Seller seller = new Seller();
        seller.setName(userDto.getName());
        seller.setNumber(userDto.getNumber());
        seller.setEmail(userDto.getEmail());
        return seller;
    }
    public static UserGetDto toGetUser(User user) {
        return UserGetDto.builder()
                .id(user.getId())
                .name(user.getName())
                .number(user.getNumber())
                .email(user.getEmail())
                .build();
    }

}