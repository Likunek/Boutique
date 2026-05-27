package ru.angelika.boutique.assured;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.angelika.boutique.dto.OrderDto;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.Cart;
import ru.angelika.boutique.model.ItemCard;
import ru.angelika.boutique.model.PickupPoint;
import ru.angelika.boutique.model.User;
import ru.angelika.boutique.service.CartService;
import ru.angelika.boutique.service.OrderService;
import ru.angelika.boutique.service.PickupPointService;
import ru.angelika.boutique.service.UserService;

import java.util.List;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class OrdersRestAssuredTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PickupPointService pickupPointService;

    private static final String USER_NUMBER = "89137650977";
    private static final Long CART_ID = 100L;
    private static final Long USER_ID = 2L;

    private User user;
    private Cart cart;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);

        cart = new Cart();
        cart.setId(CART_ID);
        cart.setTotalPrice(5000.0);

        user = new User();
        user.setId(USER_ID);
        user.setNumber(USER_NUMBER);
        user.setBalance(10000.0);
        user.setCart(cart);
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void addOrder_Success() {
        when(userService.getByNumber(USER_NUMBER)).thenReturn(user);
        when(orderService.checkPaymentUser(any(OrderDto.class), eq(user))).thenReturn(1.0);
        doNothing().when(orderService).add(any(OrderDto.class), eq(user), eq(CART_ID));

        given()
                .param("cartId", CART_ID)
                .param("itemCards", 10, 20)
                .param("pointId", 1)
                .when()
                .post("/user/order")
                .then()
                .statusCode(302)
                .header("Location", "/user/cart/" + CART_ID);

        verify(orderService).add(any(OrderDto.class), eq(user), eq(CART_ID));
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void addOrder_OtherUserCart_RedirectToWelcome() {
        Cart otherCart = new Cart();
        otherCart.setId(9L);
        user.setCart(otherCart);
        when(userService.getByNumber(USER_NUMBER)).thenReturn(user);

        given()
                .param("cartId", CART_ID)
                .param("itemCards", 10)
                .param("pointId", 1)
                .when()
                .post("/user/order")
                .then()
                .statusCode(302)
                .header("Location", "/welcome");

        verify(orderService, never()).checkPaymentUser(any(), any());
        verify(orderService, never()).add(any(), any(), anyLong());
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void addOrder_ValidationFailure_EmptyItemCards() {
        given()
                .param("cartId", CART_ID)
                .param("pointId", 1)
                .when()
                .post("/user/order")
                .then()
                .statusCode(500);

        verify(orderService, never()).add(any(), any(), anyLong());
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void addOrder_ValidationFailure_NullPointId() {
        given()
                .param("cartId", CART_ID)
                .param("itemCards", 10)
                .when()
                .post("/user/order")
                .then()
                .statusCode(500);

        verify(orderService, never()).add(any(), any(), anyLong());
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void addOrder_UserNotFound_ThrowsException() {
        when(userService.getByNumber(USER_NUMBER))
                .thenThrow(new ResourceNotFoundException(User.class, USER_NUMBER));

        given()
                .param("cartId", CART_ID)
                .param("itemCards", 10)
                .param("pointId", 1)
                .when()
                .post("/user/order")
                .then()
                .statusCode(404);

        verify(orderService, never()).add(any(), any(), anyLong());
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void addOrder_ItemCardNotFound_ThrowsException() {
        when(userService.getByNumber(USER_NUMBER)).thenReturn(user);
        when(orderService.checkPaymentUser(any(OrderDto.class), eq(user))).thenThrow(new ResourceNotFoundException(ItemCard.class, 10L));

        given()
                .param("cartId", CART_ID)
                .param("itemCards", 10)
                .param("pointId", 1)
                .when()
                .post("/user/order")
                .then()
                .statusCode(404);

        verify(orderService, never()).add(any(), any(), anyLong());
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void addOrder_InsufficientFunds_RedirectWithError() {
        when(userService.getByNumber(USER_NUMBER)).thenReturn(user);
        when(orderService.checkPaymentUser(any(OrderDto.class), eq(user))).thenReturn(-1.0);

        given()
                .param("cartId", CART_ID)
                .param("itemCards", 10)
                .param("pointId", 1)
                .when()
                .post("/user/order")
                .then()
                .statusCode(302)
                .header("Location", "/user/order/" + CART_ID);

        verify(orderService, never()).add(any(), any(), anyLong());
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void addOrder_ServiceThrowsException() {
        when(userService.getByNumber(USER_NUMBER)).thenReturn(user);
        when(orderService.checkPaymentUser(any(OrderDto.class), eq(user))).thenReturn(1.0);
        doThrow(new ResourceNotFoundException(PickupPoint.class, 1L))
                .when(orderService).add(any(OrderDto.class), eq(user), eq(CART_ID));

        given()
                .param("cartId", CART_ID)
                .param("itemCards", 10)
                .param("pointId", 1)
                .when()
                .post("/user/order")
                .then()
                .statusCode(404);

        verify(orderService).add(any(OrderDto.class), eq(user), eq(CART_ID));
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void addOrder_SellerRole_ShouldReturnForbidden() {
        given()
                .param("cartId", CART_ID)
                .param("itemCards", 10)
                .param("pointId", 1)
                .when()
                .post("/user/order")
                .then()
                .statusCode(403);

        verify(orderService, never()).add(any(), any(), anyLong());
    }

    @Test
    void addOrder_Unauthenticated_RedirectToLogin() {
        given()
                .param("cartId", CART_ID)
                .param("itemCards", 10)
                .param("pointId", 1)
                .when()
                .post("/user/order")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
        verify(orderService, never()).add(any(), any(), anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllOrders_Success() {
        when(orderService.getAll()).thenReturn(List.of());

        given()
                .when()
                .get("/admin/orders")
                .then()
                .statusCode(200);

        verify(orderService).getAll();
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void getAllOrders_SellerRole_ShouldReturnForbidden() {
        given()
                .when()
                .get("/admin/orders")
                .then()
                .statusCode(403);

        verify(orderService, never()).getAll();
    }

    @Test
    void getAllOrders_Unauthenticated_RedirectToLogin() {
        given()
                .when()
                .get("/admin/orders")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
        verify(orderService, never()).getAll();
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void getFormNewOrder_Success() {
        when(userService.getByNumber(USER_NUMBER)).thenReturn(user);
        when(cartService.getById(CART_ID)).thenReturn(cart);
        when(pickupPointService.getAll()).thenReturn(List.of());

        given()
                .when()
                .get("/user/order/{cartId}", CART_ID)
                .then()
                .statusCode(200);

        verify(cartService).getById(CART_ID);
        verify(pickupPointService).getAll();
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void getFormNewOrder_UserNotFound_ThrowsException() {
        when(userService.getByNumber(USER_NUMBER))
                .thenThrow(new ResourceNotFoundException(User.class, USER_NUMBER));

        given()
                .when()
                .get("/user/order/{cartId}", CART_ID)
                .then()
                .statusCode(404);

        verify(cartService, never()).getById(anyLong());
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void getFormNewOrder_OtherUserCart_RedirectToWelcome() {
        Cart otherCart = new Cart();
        otherCart.setId(9L);
        user.setCart(otherCart);
        when(userService.getByNumber(USER_NUMBER)).thenReturn(user);

        given()
                .when()
                .get("/user/order/{cartId}", CART_ID)
                .then()
                .statusCode(302)
                .header("Location", "/welcome");

        verify(cartService, never()).getById(anyLong());
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void getFormNewOrder_CartNotFound_ThrowsException() {
        when(userService.getByNumber(USER_NUMBER)).thenReturn(user);
        when(cartService.getById(CART_ID))
                .thenThrow(new ResourceNotFoundException(Cart.class, CART_ID));

        given()
                .when()
                .get("/user/order/{cartId}", CART_ID)
                .then()
                .statusCode(404);

        verify(cartService).getById(CART_ID);
        verify(pickupPointService, never()).getAll();
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void getFormNewOrder_SellerRole_ShouldReturnForbidden() {
        given()
                .when()
                .get("/user/order/{cartId}", CART_ID)
                .then()
                .statusCode(403);

        verify(userService, never()).getByNumber(any());
    }

    @Test
    void getFormNewOrder_Unauthenticated_RedirectToLogin() {
        given()
                .when()
                .get("/user/order/{cartId}", CART_ID)
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
        verify(userService, never()).getByNumber(any());
    }
}