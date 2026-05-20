package ru.angelika.boutique.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.angelika.boutique.dto.AuthenticationDto;
import ru.angelika.boutique.dto.UpdateEntityDto;
import ru.angelika.boutique.dto.UserDto;
import ru.angelika.boutique.exception.ResourceExistsException;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.mapper.UserMapper;
import ru.angelika.boutique.model.*;
import ru.angelika.boutique.repository.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ItemCardRepository itemCardRepository;
    private final FeedbackRepository feedbackRepository;
    private final AuthenticationService authenticationService;

    public void add(UserDto userDto) {
        if (userRepository.findByName(userDto.getName()) != null) {
            log.error("User with name={} already exists", userDto.getName());
            throw new ResourceExistsException(User.class, userDto.getName());
        }
        if (userRepository.findByNumber(userDto.getNumber()) != null) {
            log.error("User with number={} already exists", userDto.getNumber());
            throw new ResourceExistsException(User.class, userDto.getNumber());
        }
        if (userRepository.findByEmail(userDto.getEmail()) != null) {
            log.error("User with email={} already exists", userDto.getEmail());
            throw new ResourceExistsException(User.class, userDto.getEmail());
        }
        Cart cart = cartRepository.save(new Cart());
        User user = UserMapper.toUser(userDto, cart);
        user.setAuthentication(authenticationService.findByNumber(user.getNumber()));
        userRepository.save(user);
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

    public Set<Long> checkItemIdWithOwnFeedbacks(Long id) {
        User user = userRepository.findByIdWithItemsAndFeedbacks(id);
        if (user == null) {
            log.error("User not found for get, id={}", id);
            throw new ResourceNotFoundException(User.class, id);
        }
        return user.getItems().stream()
                .filter(item -> item.getFeedbacks().stream().anyMatch(f -> f.getUser().getId().equals(id)))
                .map(ItemCard::getId)
                .collect(Collectors.toSet());
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

    public List<Order> getOrders(Long userId) {
        log.debug("Get orders by userId={}", userId);
        return orderRepository.findByUserIdAndStatusNot(userId, Status.RECEIVED);
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public List<Long> getCardsId(String number) {
        User user = getByNumber(number);
        return user.getCart().getItemCards()
                .stream()
                .map(ItemCard::getId)
                .toList();
    }

    @Transactional
    public void update(UpdateEntityDto userDto, Long id) {
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
        authenticationService.update(AuthenticationDto.builder()
                .oldPassword(userDto.getOldPassword())
                .newPassword(userDto.getNewPassword())
                .number(userDto.getNumber())
                .build(), user.getAuthentication());
        userRepository.save(user);
        log.info("Update user by id={}", id);
    }

    public void delete(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> {
            log.error("User not found for delete, id={}", id);
            return new ResourceNotFoundException(User.class, id);
        });
        authenticationService.delete(user.getNumber());
        orderRepository.deleteAll(orderRepository.findByUserId(id));
        List<Feedback> feedbacks = feedbackRepository.findByUserId(id);
        for (Feedback feedback : feedbacks) {
            ItemCard itemCard = itemCardRepository.findItemByFeedbackId(feedback.getId());
            if (itemCard != null) {
                itemCard.getFeedbacks().removeIf(f -> f.getId().equals(feedback.getId()));
                itemCardRepository.save(itemCard);
            }
        }
        feedbackRepository.deleteAll(feedbacks);
        userRepository.deleteById(id);
        log.info("Delete user by id={}", id);
    }

}
