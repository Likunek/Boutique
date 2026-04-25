package ru.angelika.boutique.mapper;

import org.springframework.stereotype.Component;
import ru.angelika.boutique.dto.UserDto;
import ru.angelika.boutique.dto.UserGetDto;
import ru.angelika.boutique.model.User;

@Component
public class UserMapper {

    public User toUser(UserDto userDto){
        return new User();
    }
    public UserGetDto toGetUser(User user) {
        return UserGetDto.builder()
                .name(user.getName())
                .number(user.getNumber())
                .email(user.getEmail())
                .build();
    }

}