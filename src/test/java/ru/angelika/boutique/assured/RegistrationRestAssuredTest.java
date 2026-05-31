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
import ru.angelika.boutique.dto.UserDto;
import ru.angelika.boutique.exception.ResourceExistsException;
import ru.angelika.boutique.model.Authentication;
import ru.angelika.boutique.model.Role;
import ru.angelika.boutique.model.Seller;
import ru.angelika.boutique.model.User;
import ru.angelika.boutique.service.AuthenticationService;
import ru.angelika.boutique.service.SellerService;
import ru.angelika.boutique.service.UserService;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class RegistrationRestAssuredTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private SellerService sellerService;

    @MockitoBean
    private AuthenticationService authenticationService;

    private static final String NAME = "user";
    private static final String PASSWORD = "1234";
    private static final String NUMBER = "89137650976";
    private static final String EMAIL = "test@gmail.com";

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    @Test
    void loginPage_Success() {
        given()
                .when()
                .get("/login")
                .then()
                .statusCode(200);
    }

    @Test
    void registrationPage_Success() {
        given()
                .when()
                .get("/registration")
                .then()
                .statusCode(200);
    }

    @Test
    void addUser_UserRole_Success() {
        Authentication auth = new Authentication();
        auth.setNumber(NUMBER);
        auth.setPassword(PASSWORD);
        auth.setRole(Role.USER);

        UserDto userDto = UserDto.builder()
                .name(NAME)
                .number(NUMBER)
                .email(EMAIL)
                .role(Role.USER)
                .password(PASSWORD)
                .build();

        doNothing().when(authenticationService).add(auth);
        doNothing().when(userService).add(userDto);

        given()
                .param("name", NAME)
                .param("password", PASSWORD)
                .param("number", NUMBER)
                .param("email", EMAIL)
                .param("role", "USER")
                .when()
                .post("/registration")
                .then()
                .statusCode(302)
                .header("Location", "/login");
        verify(userService).add(userDto);
        verify(sellerService, never()).add(any());
    }

    @Test
    void addUser_SellerRole_Success() {
        Authentication auth = new Authentication();
        auth.setNumber(NUMBER);
        auth.setPassword(PASSWORD);
        auth.setRole(Role.USER);
        Seller seller = new Seller();
        seller.setName(NAME);
        seller.setNumber(NUMBER);
        seller.setEmail(EMAIL);

        doNothing().when(authenticationService).add(auth);
        doNothing().when(sellerService).add(seller);

        given()
                .param("name", NAME)
                .param("password", PASSWORD)
                .param("number", NUMBER)
                .param("email", EMAIL)
                .param("role", "SELLER")
                .when()
                .post("/registration")
                .then()
                .statusCode(302)
                .header("Location", "/login");
        verify(sellerService).add(seller);
        verify(userService, never()).add(any());
    }

    @Test
    void addUser_ValidationFailure_EmptyName() {
        given()
                .param("name", "")
                .param("password", PASSWORD)
                .param("number", NUMBER)
                .param("email", EMAIL)
                .param("role", "USER")
                .when()
                .post("/registration")
                .then()
                .statusCode(200);
        verify(authenticationService, never()).add(any());
        verify(userService, never()).add(any());
        verify(sellerService, never()).add(any());
    }

    @Test
    void addUser_ValidationFailure_InvalidNumber() {
        given()
                .param("name", NAME)
                .param("password", PASSWORD)
                .param("number", "123")
                .param("email", EMAIL)
                .param("role", "USER")
                .when()
                .post("/registration")
                .then()
                .statusCode(200);
        verify(authenticationService, never()).add(any());
        verify(userService, never()).add(any());
        verify(sellerService, never()).add(any());
    }

    @Test
    void addUser_ValidationFailure_InvalidEmail() {
        given()
                .param("name", NAME)
                .param("password", PASSWORD)
                .param("number", NUMBER)
                .param("email", "invalid")
                .param("role", "USER")
                .when()
                .post("/registration")
                .then()
                .statusCode(200);
        verify(authenticationService, never()).add(any());
        verify(userService, never()).add(any());
        verify(sellerService, never()).add(any());
    }

    @Test
    void addUser_ValidationFailure_PasswordShort() {
        given()
                .param("name", NAME)
                .param("password", "123")
                .param("number", NUMBER)
                .param("email", EMAIL)
                .param("role", "USER")
                .when()
                .post("/registration")
                .then()
                .statusCode(200);
        verify(authenticationService, never()).add(any());
        verify(userService, never()).add(any());
        verify(sellerService, never()).add(any());
    }

    @Test
    void addUser_ValidationFailure_PasswordLong() {
        given()
                .param("name", NAME)
                .param("password", "12345678900")
                .param("number", NUMBER)
                .param("email", EMAIL)
                .param("role", "USER")
                .when()
                .post("/registration")
                .then()
                .statusCode(200);
        verify(authenticationService, never()).add(any());
        verify(userService, never()).add(any());
        verify(sellerService, never()).add(any());
    }

    @Test
    void addUser_AuthenticationExists_ThrowsException() {
        doThrow(new ResourceExistsException(Authentication.class, NUMBER))
                .when(authenticationService).add(any(Authentication.class));

        given()
                .param("name", NAME)
                .param("password", PASSWORD)
                .param("number", NUMBER)
                .param("email", EMAIL)
                .param("role", "USER")
                .when()
                .post("/registration")
                .then()
                .statusCode(200);
        verify(userService, never()).add(any());
        verify(sellerService, never()).add(any());
    }

    @Test
    void addUser_UserExists_ThrowsException() {
        doNothing().when(authenticationService).add(any(Authentication.class));
        doThrow(new ResourceExistsException(User.class, NAME))
                .when(userService).add(any(UserDto.class));

        given()
                .param("name", NAME)
                .param("password", PASSWORD)
                .param("number", NUMBER)
                .param("email", EMAIL)
                .param("role", "USER")
                .when()
                .post("/registration")
                .then()
                .statusCode(200);
        verify(sellerService, never()).add(any());
    }

    @Test
    void addUser_SellerExists_ThrowsException() {
        doNothing().when(authenticationService).add(any(Authentication.class));
        doThrow(new ResourceExistsException(Seller.class, NAME))
                .when(sellerService).add(any(Seller.class));

        given()
                .param("name", NAME)
                .param("password", PASSWORD)
                .param("number", NUMBER)
                .param("email", EMAIL)
                .param("role", "SELLER")
                .when()
                .post("/registration")
                .then()
                .statusCode(200);
        verify(userService, never()).add(any());
    }

    @Test
    @WithMockUser(username = NUMBER, roles = "USER")
    void welcome_UserRole_Success() {
        User user = new User();
        user.setId(1L);
        user.setNumber(NUMBER);

        when(userService.getByNumber(NUMBER)).thenReturn(user);

        given()
                .when()
                .get("/welcome")
                .then()
                .statusCode(200);
    }

    @Test
    @WithMockUser(username = NUMBER, roles = "SELLER")
    void welcome_SellerRole_Success() {
        Seller seller = new Seller();
        seller.setId(1L);
        seller.setNumber(NUMBER);
        when(sellerService.getByNumber(NUMBER)).thenReturn(seller);

        given()
                .when()
                .get("/welcome")
                .then()
                .statusCode(200);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void welcome_AdminRole_Success() {
        given()
                .when()
                .get("/welcome")
                .then()
                .statusCode(200);
    }

    @Test
    void welcome_Unauthenticated_RedirectToLogin() {
        given()
                .when()
                .get("/welcome")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void accessDeniedPage_Success() {
        given()
                .when()
                .get("/access-denied")
                .then()
                .statusCode(200);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminPage_Success() {
        given()
                .when()
                .get("/admin/profile/1")
                .then()
                .statusCode(200);
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminPage_User_ShouldReturnForbidden() {
        given()
                .when()
                .get("/admin/profile/1")
                .then()
                .statusCode(403);
    }
}