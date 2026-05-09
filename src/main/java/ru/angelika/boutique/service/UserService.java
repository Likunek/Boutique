package ru.angelika.boutique.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.angelika.boutique.dto.UserDto;
import ru.angelika.boutique.dto.UserGetDto;
import ru.angelika.boutique.exception.ResourceExistsException;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.mapper.UserMapper;
import ru.angelika.boutique.model.Cart;
import ru.angelika.boutique.model.User;
import ru.angelika.boutique.repository.CartRepository;
import ru.angelika.boutique.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final AuthenticationService authenticationService;

    @Autowired
    public UserService(UserRepository userRepository, CartRepository cartRepository, AuthenticationService authenticationService) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.authenticationService = authenticationService;
    }

    public void addUser(UserDto user) {
        if (userRepository.findByName(user.getName()) != null) {
            log.error("User with name={} already exists", user.getName());
            throw new ResourceExistsException(User.class, user.getName());
        }
        if (userRepository.findByNumber(user.getNumber()) != null) {
            log.error("User with number={} already exists", user.getNumber());
            throw new ResourceExistsException(User.class, user.getNumber());
        }
        if (userRepository.findByEmail(user.getEmail()) != null) {
            log.error("User with email={} already exists", user.getEmail());
            throw new ResourceExistsException(User.class, user.getEmail());
        }
        Cart cart = cartRepository.save(new Cart());
        userRepository.save(UserMapper.toUser(user, cart));
        log.info("Add new user: name={}, number={}, email={}, cartId={}",
                user.getName(), user.getNumber(), user.getEmail(), cart.getId());
    }

    public User getById(Long id) {
        log.debug("Get user by id={}", id);
        return userRepository.findById(id).orElseThrow(() -> {
            log.error("User not found for get, id={}", id);
            return new ResourceNotFoundException(User.class, id);
        });
    }

    public User getByNumber(String number) {
        log.debug("Get user by number={}", number);
        User user = userRepository.findByNumber(number);
        if (user == null) {
            log.error("User not found for get, number={}", number);
            throw new ResourceNotFoundException(User.class, number);
        }
        return user;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public UserGetDto getUserByName(String name) {
        User user = userRepository.findByName(name);
        if (user == null) {
            throw new ResourceNotFoundException(User.class, name);
        }
        return UserMapper.toGetUser(user);
    }

    public void updateUser(UserDto userDto, Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> {
            log.error("User not found for update, id={}", id);
            return new ResourceNotFoundException(User.class, id);
        });
        if (!user.getName().equals(userDto.getName())) {
            if (userRepository.findByName(userDto.getName()) != null) {
                log.error("User with name={} already exists", userDto.getName());
                throw new ResourceExistsException(User.class, userDto.getName());
            }
            user.setName(userDto.getName());
        }
        if (!user.getNumber().equals(userDto.getNumber())) {
            if (userRepository.findByNumber(userDto.getNumber()) != null) {
                log.error("User with number={} already exists", userDto.getNumber());
                throw new ResourceExistsException(User.class, userDto.getNumber());
            }
            user.setNumber(userDto.getNumber());
        }
        if (!user.getEmail().equals(userDto.getEmail())) {
            if (userRepository.findByEmail(userDto.getEmail()) != null) {
                log.error("User with email={} already exists", userDto.getEmail());
                throw new ResourceExistsException(User.class, userDto.getEmail());
            }
            user.setEmail(userDto.getEmail());
        }
        userRepository.save(user);
        log.info("Update user by id={}", id);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> {
            log.error("User not found for delete, id={}", id);
            return new ResourceNotFoundException(User.class, id);
        });
        authenticationService.deleteAuthentication(user.getNumber());
        userRepository.deleteById(id);
        log.info("Delete user by id={}", id);
    }
}
