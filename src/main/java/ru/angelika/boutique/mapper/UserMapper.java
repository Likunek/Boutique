package ru.angelika.boutique.mapper;

import ru.angelika.boutique.dto.UserDto;
import ru.angelika.boutique.model.Authentication;
import ru.angelika.boutique.model.Cart;
import ru.angelika.boutique.model.Seller;
import ru.angelika.boutique.model.User;

/**
 * Маппер для преобразования UserDto в сущности {@link User}, {@link Authentication} и {@link Seller}.
 */
public class UserMapper {

    /**
     * Создаёт нового пользователя (покупателя) на основе DTO и корзины.
     * <p>Если email в DTO пустой или null, то в сущности он будет установлен как null.</p>
     *
     * @param userDto DTO с данными (имя, номер, email)
     * @param cart    созданная ранее корзина (пустая)
     * @return заполненный объект {@link User}
     */
    public static User toUser(UserDto userDto, Cart cart) {
        User user = new User();
        user.setName(userDto.getName());
        user.setNumber(userDto.getNumber());
        user.setEmail(userDto.getEmail());
        if (userDto.getEmail().isBlank() || userDto.getEmail() == null) {
            user.setEmail(null);
        }
        user.setCart(cart);
        return user;
    }

    /**
     * Создаёт сущность {@link Authentication} из UserDto для последующего сохранения.
     *
     * @param userDto DTO с номером телефона, паролем и ролью
     * @return объект {@link Authentication} (пароль пока не закодирован)
     */
    public static Authentication toAuthentication(UserDto userDto) {
        Authentication authentication = new Authentication();
        authentication.setNumber(userDto.getNumber());
        authentication.setPassword(userDto.getPassword());
        authentication.setNumber(userDto.getNumber());
        authentication.setRole(userDto.getRole());
        return authentication;
    }

    /**
     * Создаёт сущность {@link Seller} из UserDto (используется при регистрации продавца).
     *
     * @param userDto DTO с именем, номером телефона и email
     * @return объект {@link Seller}
     */
    public static Seller toSeller(UserDto userDto) {
        Seller seller = new Seller();
        seller.setName(userDto.getName());
        seller.setNumber(userDto.getNumber());
        seller.setEmail(userDto.getEmail());
        return seller;
    }
}