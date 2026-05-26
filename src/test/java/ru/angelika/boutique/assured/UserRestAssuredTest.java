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

import ru.angelika.boutique.dto.UpdateEntityDto;
import ru.angelika.boutique.exception.ResourceExistsException;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.User;
import ru.angelika.boutique.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserRestAssuredTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserService userService;

    private static final String NUMBER = "89137650976";
    private static final String USER_ROLE = "USER";
    private static final String SELLER_ROLE = "SELLER";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final Long USER_ID = 1L;

    private User testUser;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);
        testUser = new User();
        testUser.setId(USER_ID);
        testUser.setName("nadin");
        testUser.setNumber(NUMBER);
        testUser.setEmail("nadin@mail.ru");
    }

    @Test
    @WithMockUser(username = NUMBER, roles = USER_ROLE)
    void userPage_Success() {
        when(userService.getByNumber(NUMBER)).thenReturn(testUser);
        when(userService.getById(USER_ID)).thenReturn(testUser);
        when(userService.checkItemIdWithOwnFeedbacks(USER_ID)).thenReturn(Set.of());
        when(userService.getOrders(USER_ID)).thenReturn(Collections.emptyList());

        given()
                .when()
                .get("/user/profile/{id}", USER_ID)
                .then()
                .statusCode(200);
    }

    @Test
    void userPage_Unauthenticated_RedirectToLogin() {
        given()
                .when()
                .get("/user/profile/{id}", USER_ID)
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }

    @Test
    @WithMockUser(username = NUMBER, roles = SELLER_ROLE)
    void userPage_SellerRole_ShouldReturnForbidden() {
        given()
                .when()
                .get("/user/profile/{id}", USER_ID)
                .then()
                .statusCode(403);
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void getAll_AdminSuccess() {
        when(userService.getAll()).thenReturn(List.of(testUser));
        given()
                .when()
                .get("/admin/users")
                .then()
                .statusCode(200);
    }

    @Test
    @WithMockUser(username = NUMBER, roles = SELLER_ROLE)
    void getAll_Seller_ShouldReturnForbidden() {
        given()
                .when()
                .get("/admin/users")
                .then()
                .statusCode(403);
    }

    @Test
    @WithMockUser(username = NUMBER, roles = USER_ROLE)
    void update_Success() {
        when(userService.getByNumber(NUMBER)).thenReturn(testUser);
        doNothing().when(userService).update(any(UpdateEntityDto.class), eq(USER_ID));

        given()
                .param("name", "new_name")
                .param("number", NUMBER)
                .param("email", "new@mail.com")
                .param("oldPassword", "1234")
                .param("newPassword", "5678")
                .when()
                .put("/user/profile/{id}", USER_ID)
                .then()
                .statusCode(302)
                .header("Location", "/logout");
    }

    @Test
    @WithMockUser(username = NUMBER, roles = USER_ROLE)
    void update_ValidationFailure_EmptyName() {
        when(userService.getByNumber(NUMBER)).thenReturn(testUser);
        given()
                .param("name", "")
                .param("number", NUMBER)
                .param("email", "new@mail.com")
                .param("oldPassword", "1234")
                .param("newPassword", "5678")
                .when()
                .put("/user/profile/{id}", USER_ID)
                .then()
                .statusCode(302)
                .header("Location", "/user/profile/1");
        verify(userService, never()).update(any(), any());
    }

    @Test
    @WithMockUser(username = NUMBER, roles = USER_ROLE)
    void update_ValidationFailure_InvalidNumber() {
        when(userService.getByNumber(NUMBER)).thenReturn(testUser);
        given()
                .param("name", "new_name")
                .param("number", "4354")
                .param("email", "new@mail.com")
                .param("oldPassword", "1234")
                .when()
                .put("/user/profile/{id}", USER_ID)
                .then()
                .statusCode(302)
                .header("Location", "/user/profile/1");
        verify(userService, never()).update(any(), any());
    }

    @Test
    @WithMockUser(username = NUMBER, roles = USER_ROLE)
    void update_ValidationFailure_InvalidEmail() {
        when(userService.getByNumber(NUMBER)).thenReturn(testUser);
        given()
                .param("name", "new_name")
                .param("number", NUMBER)
                .param("email", "invalid-email")
                .param("oldPassword", "1234")
                .when()
                .put("/user/profile/{id}", USER_ID)
                .then()
                .statusCode(302)
                .header("Location", "/user/profile/1");
        verify(userService, never()).update(any(), any());
    }

    @Test
    @WithMockUser(username = NUMBER, roles = USER_ROLE)
    void update_ValidationFailure_EmptyOldPassword() {
        when(userService.getByNumber(NUMBER)).thenReturn(testUser);
        given()
                .param("name", "new_name")
                .param("number", NUMBER)
                .param("email", "new@mail.com")
                .param("oldPassword", "")
                .when()
                .put("/user/profile/{id}", USER_ID)
                .then()
                .statusCode(302)
                .header("Location", "/user/profile/1");
        verify(userService, never()).update(any(), any());
    }

    @Test
    @WithMockUser(username = NUMBER, roles = USER_ROLE)
    void update_ValidationFailure_NewPasswordShort() {
        when(userService.getByNumber(NUMBER)).thenReturn(testUser);
        given()
                .param("name", "new_name")
                .param("number", NUMBER)
                .param("email", "new@mail.com")
                .param("oldPassword", "1234")
                .param("newPassword", "123")
                .when()
                .put("/user/profile/{id}", USER_ID)
                .then()
                .statusCode(302)
                .header("Location", "/user/profile/1");
        verify(userService, never()).update(any(), any());
    }

    @Test
    @WithMockUser(username = NUMBER, roles = USER_ROLE)
    void update_ValidationFailure_NewPasswordLong() {
        when(userService.getByNumber(NUMBER)).thenReturn(testUser);
        given()
                .param("name", "new_name")
                .param("number", NUMBER)
                .param("email", "new@mail.com")
                .param("oldPassword", "1234")
                .param("newPassword", "12345678900")
                .when()
                .put("/user/profile/{id}", USER_ID)
                .then()
                .statusCode(302)
                .header("Location", "/user/profile/1");
        verify(userService, never()).update(any(), any());
    }

    @Test
    @WithMockUser(username = NUMBER, roles = USER_ROLE)
    void update_UserNotFound_ThrowsException() {
        when(userService.getByNumber(NUMBER)).thenThrow(new ResourceNotFoundException(User.class, ""));

        given()
                .param("name", "new_name")
                .param("number", NUMBER)
                .param("email", "new@mail.com")
                .param("oldPassword", "1234")
                .when()
                .put("/user/profile/{id}", USER_ID)
                .then()
                .statusCode(404);
        verify(userService, never()).update(any(), any());
    }

    @Test
    @WithMockUser(username = NUMBER, roles = USER_ROLE)
    void update_ResourceExists_ThrowsException() {
        when(userService.getByNumber(NUMBER)).thenReturn(testUser);
        doThrow(new ResourceExistsException(User.class, ""))
                .when(userService)
                .update(any(UpdateEntityDto.class), eq(USER_ID));

        given()
                .param("name", "new_name")
                .param("number", NUMBER)
                .param("email", "new@mail.com")
                .param("oldPassword", "1234")
                .when()
                .put("/user/profile/{id}", USER_ID)
                .then()
                .statusCode(302)
                .header("Location", "/user/profile/1");;
    }

    @Test
    @WithMockUser(username = NUMBER, roles = USER_ROLE)
    void update_elseUser_ShouldReturnForbidden() {
        User userOther = new User();
        userOther.setId(5L);
        when(userService.getByNumber(NUMBER)).thenReturn(userOther);

        given()
                .param("name", "new_name")
                .param("number", NUMBER)
                .param("email", "new@mail.com")
                .param("oldPassword", "1234")
                .when()
                .put("/user/profile/{id}", USER_ID)
                .then()
                .statusCode(302)
                .header("Location", "/welcome");
        verify(userService, never()).update(any(), any());
    }
    @Test
    @WithMockUser(username = NUMBER, roles = SELLER_ROLE)
    void update_Seller_ShouldReturnForbidden() {
        given()
                .param("name", "new_name")
                .param("number", NUMBER)
                .param("email", "new@example.com")
                .param("oldPassword", "1234")
                .param("newPassword", "5678")
                .when()
                .put("/user/profile/{id}", USER_ID)
                .then()
                .statusCode(403);
    }

    @Test
    @WithMockUser(username = NUMBER, roles = USER_ROLE)
    void delete_Success() {
        when(userService.getByNumber(NUMBER)).thenReturn(testUser);
        doNothing().when(userService).delete(USER_ID);
        given()
                .when()
                .delete("/users/{id}", USER_ID)
                .then()
                .statusCode(302)
                .header("Location", "/logout");
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void delete_AdminSuccess() {
        doNothing().when(userService).delete(USER_ID);
        given()
                .when()
                .delete("/users/{id}", USER_ID)
                .then()
                .statusCode(302)
                .header("Location", "/admin/users");
    }

    @Test
    @WithMockUser(username = NUMBER, roles = SELLER_ROLE)
    void delete_Seller_ShouldReturnForbidden() {
        given()
                .when()
                .delete("/users/{id}", USER_ID)
                .then()
                .statusCode(403);
    }

    @Test
    @WithMockUser(username = NUMBER, roles = USER_ROLE)
    void delete_UserTryingDeleteAnother_RedirectToWelcome() {
        when(userService.getByNumber(NUMBER)).thenReturn(testUser);
        given()
                .when()
                .delete("/users/{id}", 0L)
                .then()
                .statusCode(302)
                .header("Location", "/welcome");
    }
}