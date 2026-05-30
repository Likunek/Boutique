package ru.angelika.boutique.assured;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.Cart;
import ru.angelika.boutique.model.ItemCard;
import ru.angelika.boutique.model.User;
import ru.angelika.boutique.service.CartService;
import ru.angelika.boutique.service.UserService;

import java.util.List;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class CartRestAssuredTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private UserService userService;

    private static final String USER_NUMBER = "89137650977";
    private static final Long CART_ID = 1L;
    private static final Long CARD_ID = 20L;

    private User user;
    private Cart cart;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);

        cart = new Cart();
        cart.setId(CART_ID);

        user = new User();
        user.setId(2L);
        user.setNumber(USER_NUMBER);
        user.setCart(cart);
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void getCart_Success() {
        when(userService.getByNumber(USER_NUMBER)).thenReturn(user);
        when(cartService.getById(CART_ID)).thenReturn(cart);

        given()
                .when()
                .get("/user/cart/{id}", CART_ID)
                .then()
                .statusCode(200);

        verify(cartService).getById(CART_ID);
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void getCart_OtherUser_RedirectToWelcome() {
        Cart otherCart = new Cart();
        otherCart.setId(9L);
        user.setCart(otherCart);
        when(userService.getByNumber(USER_NUMBER)).thenReturn(user);

        given()
                .when()
                .get("/user/cart/{id}", CART_ID)
                .then()
                .statusCode(302)
                .header("Location", "/welcome");

        verify(cartService, never()).getById(anyLong());
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void getCart_UserNotFound_ThrowsException() {
        when(userService.getByNumber(USER_NUMBER))
                .thenThrow(new ResourceNotFoundException(User.class, USER_NUMBER));

        given()
                .when()
                .get("/user/cart/{id}", CART_ID)
                .then()
                .statusCode(404);

        verify(cartService, never()).getById(anyLong());
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void getCart_CartNotFound_ThrowsException() {
        when(userService.getByNumber(USER_NUMBER)).thenReturn(user);
        when(cartService.getById(CART_ID))
                .thenThrow(new ResourceNotFoundException(Cart.class, CART_ID));

        given()
                .when()
                .get("/user/cart/{id}", CART_ID)
                .then()
                .statusCode(404);

        verify(cartService).getById(CART_ID);
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void getCart_SellerRole_ShouldReturnForbidden() {
        given()
                .when()
                .get("/user/cart/{id}", CART_ID)
                .then()
                .statusCode(403);

        verify(userService, never()).getByNumber(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCart_AdminRole_ShouldReturnForbidden() {
        given()
                .when()
                .get("/user/cart/{id}", CART_ID)
                .then()
                .statusCode(403);

        verify(userService, never()).getByNumber(any());
    }

    @Test
    void getCart_Unauthenticated_RedirectToLogin() {
        given()
                .when()
                .get("/user/cart/{id}", CART_ID)
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void addItemInCart_Success() {
        when(userService.getByNumber(USER_NUMBER)).thenReturn(user);
        doNothing().when(cartService).addCardInCart(CART_ID, CARD_ID);

        given()
                .when()
                .post("/user/cart/add-card/{id}", CARD_ID)
                .then()
                .statusCode(302)
                .header("Location", "/item-card?role=ROLE_USER");

        verify(cartService).addCardInCart(CART_ID, CARD_ID);
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void addItemInCart_CardTrueRedirect_Success() {
        when(userService.getByNumber(USER_NUMBER)).thenReturn(user);
        doNothing().when(cartService).addCardInCart(CART_ID, CARD_ID);

        given()
                .queryParam("card", true)
                .when()
                .post("/user/cart/add-card/{id}", CARD_ID)
                .then()
                .statusCode(302)
                .header("Location", "/item-card/" + CARD_ID + "?role=ROLE_USER");

        verify(cartService).addCardInCart(CART_ID, CARD_ID);
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void addItemInCart_UserNotFound_ThrowsException() {
        when(userService.getByNumber(USER_NUMBER))
                .thenThrow(new ResourceNotFoundException(User.class, USER_NUMBER));

        given()
                .when()
                .post("/user/cart/add-card/{id}", CARD_ID)
                .then()
                .statusCode(404);

        verify(cartService, never()).addCardInCart(anyLong(), anyLong());
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void addItemInCart_CardNotFound_ThrowsException() {
        when(userService.getByNumber(USER_NUMBER)).thenReturn(user);
        doThrow(new ResourceNotFoundException(ItemCard.class , CARD_ID))
                .when(cartService).addCardInCart(CART_ID, CARD_ID);

        given()
                .when()
                .post("/user/cart/add-card/{id}", CARD_ID)
                .then()
                .statusCode(404);

        verify(cartService).addCardInCart(CART_ID, CARD_ID);
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void addItemInCart_SellerRole_ShouldReturnForbidden() {
        given()
                .when()
                .post("/user/cart/add-card/{id}", CARD_ID)
                .then()
                .statusCode(403);

        verify(cartService, never()).addCardInCart(anyLong(), anyLong());
    }

    @Test
    void addItemInCart_Unauthenticated_RedirectToLogin() {
        given()
                .when()
                .post("/user/cart/add-card/{id}", CARD_ID)
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void deleteItemsFromCart_Success() {
        when(userService.getByNumber(USER_NUMBER)).thenReturn(user);
        doNothing().when(cartService).deleteCardsFromCart(CART_ID, List.of(CARD_ID));

        given()
                .queryParam("cards", CARD_ID.toString())
                .when()
                .post("/user/cart/card")
                .then()
                .statusCode(302)
                .header("Location", "/user/cart/" + CART_ID);

        verify(cartService).deleteCardsFromCart(CART_ID, List.of(CARD_ID));
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void deleteItemsFromCart_TwoCards_Success() {
        when(userService.getByNumber(USER_NUMBER)).thenReturn(user);
        doNothing().when(cartService).deleteCardsFromCart(CART_ID, List.of(1L, 2L));

        given()
                .queryParam("cards", "1", "2")
                .when()
                .post("/user/cart/card")
                .then()
                .statusCode(302)
                .header("Location", "/user/cart/" + CART_ID);

        verify(cartService).deleteCardsFromCart(CART_ID, List.of(1L, 2L));
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void deleteItemsFromCart_UserNotFound_ThrowsException() {
        when(userService.getByNumber(USER_NUMBER))
                .thenThrow(new ResourceNotFoundException(User.class, USER_NUMBER));

        given()
                .queryParam("cards", CARD_ID.toString())
                .when()
                .post("/user/cart/card")
                .then()
                .statusCode(404);

        verify(cartService, never()).deleteCardsFromCart(anyLong(), any());
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void deleteItemsFromCart_CartNotFound_ThrowsException() {
        when(userService.getByNumber(USER_NUMBER)).thenReturn(user);
        doThrow(new ResourceNotFoundException(Cart.class, CART_ID))
                .when(cartService).deleteCardsFromCart(CART_ID, List.of(CARD_ID));

        given()
                .queryParam("cards", CARD_ID.toString())
                .when()
                .post("/user/cart/card")
                .then()
                .statusCode(404);

        verify(cartService).deleteCardsFromCart(CART_ID, List.of(CARD_ID));
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void deleteItemsFromCart_SellerRole_ShouldReturnForbidden() {
        given()
                .queryParam("cards", CARD_ID.toString())
                .when()
                .post("/user/cart/card")
                .then()
                .statusCode(403);

        verify(cartService, never()).deleteCardsFromCart(anyLong(), any());
    }

    @Test
    void deleteItemsFromCart_Unauthenticated_RedirectToLogin() {
        given()
                .queryParam("cards", CARD_ID.toString())
                .when()
                .post("/user/cart/card")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }


    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void deleteItemFromCart_Success() {
        when(userService.getByNumber(USER_NUMBER)).thenReturn(user);
        doNothing().when(cartService).deleteCardFromCart(CART_ID, CARD_ID);

        given()
                .when()
                .delete("/user/cart/card/{id}", CARD_ID)
                .then()
                .statusCode(302)
                .header("Location", "/item-card?role=ROLE_USER");

        verify(cartService).deleteCardFromCart(CART_ID, CARD_ID);
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void deleteItemFromCart_CardTrue_Success() {
        when(userService.getByNumber(USER_NUMBER)).thenReturn(user);
        doNothing().when(cartService).deleteCardFromCart(CART_ID, CARD_ID);

        given()
                .queryParam("card", true)
                .when()
                .delete("/user/cart/card/{id}", CARD_ID)
                .then()
                .statusCode(302)
                .header("Location", "/item-card/" + CARD_ID + "?role=ROLE_USER");

        verify(cartService).deleteCardFromCart(CART_ID, CARD_ID);
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void deleteItemFromCart_UserNotFound_ThrowsException() {
        when(userService.getByNumber(USER_NUMBER))
                .thenThrow(new ResourceNotFoundException(User.class, USER_NUMBER));

        given()
                .when()
                .delete("/user/cart/card/{id}", CARD_ID)
                .then()
                .statusCode(404);

        verify(cartService, never()).deleteCardFromCart(anyLong(), anyLong());
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void deleteItemFromCart_CardNotFound_ThrowsException() {
        when(userService.getByNumber(USER_NUMBER)).thenReturn(user);
        doThrow(new ResourceNotFoundException(ItemCard.class, CARD_ID))
                .when(cartService).deleteCardFromCart(CART_ID, CARD_ID);

        given()
                .when()
                .delete("/user/cart/card/{id}", CARD_ID)
                .then()
                .statusCode(404);

        verify(cartService).deleteCardFromCart(CART_ID, CARD_ID);
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void deleteItemFromCart_SellerRole_ShouldReturnForbidden() {
        given()
                .when()
                .delete("/user/cart/card/{id}", CARD_ID)
                .then()
                .statusCode(403);

        verify(cartService, never()).deleteCardFromCart(anyLong(), anyLong());
    }

    @Test
    void deleteItemFromCart_Unauthenticated_RedirectToLogin() {
        given()
                .when()
                .delete("/user/cart/card/{id}", CARD_ID)
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }
}