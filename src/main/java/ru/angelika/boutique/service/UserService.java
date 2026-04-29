package ru.angelika.boutique.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.angelika.boutique.dto.UserDto;
import ru.angelika.boutique.dto.UserGetDto;
import ru.angelika.boutique.exception.ResourceExistsException;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.mapper.UserMapper;
import ru.angelika.boutique.model.User;
import ru.angelika.boutique.repository.UserRepository;


@Service
public class UserService {
    private final UserRepository userRepository;


    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void addUser(UserDto user) {
        if (userRepository.findByName(user.getName()) != null) {
            throw new ResourceExistsException(User.class, user.getName());
        }
        if (userRepository.findByNumber(user.getNumber()) != null) {
            throw new ResourceExistsException(User.class, user.getNumber());
        }
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new ResourceExistsException(User.class, user.getEmail());
        }
        userRepository.save(UserMapper.toUser(user));
    }

    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(User.class, id));
    }

    public User getByNumber(String number) {
        User user = userRepository.findByNumber(number);
        if (user == null) {
           throw new ResourceNotFoundException(User.class, number);
        }
        return user;
    }

    public UserGetDto getUserByName(String name) {
        User user = userRepository.findByName(name);
        if (user == null) {
            throw new ResourceNotFoundException(User.class, name);
        }return UserMapper.toGetUser(user);
    }

    public void updateUser(UserDto userDto, Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(User.class, id));

        if (!user.getName().equals(userDto.getName())) {
            if (userRepository.findByName(userDto.getName()) != null) {
                throw new ResourceExistsException(User.class, userDto.getName());
            }
            user.setName(userDto.getName());
        }
        if (!user.getNumber().equals(userDto.getNumber())) {
            if (userRepository.findByNumber(userDto.getNumber()) != null)
                throw new ResourceExistsException(User.class, userDto.getNumber());
            user.setNumber(userDto.getNumber());
        }
        if (!user.getEmail().equals(userDto.getEmail())) {
            if (userRepository.findByEmail(userDto.getEmail()) != null) {
                throw new ResourceExistsException(User.class, userDto.getEmail());
            }
            user.setEmail(userDto.getEmail());
        }
    }

    public void deleteUser(Long id) {
        userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(User.class, id));
        userRepository.deleteById(id);
    }
}
