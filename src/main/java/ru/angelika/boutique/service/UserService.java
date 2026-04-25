package ru.angelika.boutique.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.angelika.boutique.dto.UserDto;
import ru.angelika.boutique.dto.UserGetDto;
import ru.angelika.boutique.exception.UserExistsException;
import ru.angelika.boutique.exception.UserNotFoundException;
import ru.angelika.boutique.mapper.UserMapper;
import ru.angelika.boutique.model.User;
import ru.angelika.boutique.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;


    @Autowired
    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public void addUser(UserDto user) {
        if (userRepository.findByName(user.getName()) != null) {
            throw new UserExistsException("the user already exists");
        }
        if (userRepository.findByNumber(user.getNumber()) != null) {
            throw new UserExistsException("the user with number already exists");
        }
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new UserExistsException("the user with email already exists");
        }
        userRepository.save(userMapper.toUser(user));
    }

    public UserGetDto getUserByName(String name) {
        User user = userRepository.findByName(name);
        if (user == null) {
            throw new UserNotFoundException("the user not found");
        }
        return userMapper.toGetUser(user);
    }

    public void updateUser(UserDto userDto, Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("the user not found"));

        if (!user.getName().equals(userDto.getName())) {
            if (userRepository.findByName(userDto.getName()) != null) {
                throw new UserExistsException("the user already exists");
            }
            user.setName(userDto.getName());
        }
        if (!user.getNumber().equals(userDto.getNumber())) {
            if (userRepository.findByNumber(userDto.getNumber()) != null)
                throw new UserExistsException("the user with number already exists");
            user.setNumber(userDto.getNumber());
        }
        if (!user.getEmail().equals(userDto.getEmail())) {
            if (userRepository.findByEmail(userDto.getEmail()) != null) {
                throw new UserExistsException("the user with email already exists");
            }
            user.setEmail(userDto.getEmail());
        }
        user.setPassword(userDto.getPassword());
    }

    public void deleteUser(Long id) {
        userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("the user not found"));
        userRepository.deleteById(id);
    }
}
