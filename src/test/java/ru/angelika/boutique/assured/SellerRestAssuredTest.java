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
import ru.angelika.boutique.dto.UpdateEntityDto;
import ru.angelika.boutique.exception.ResourceExistsException;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.Seller;
import ru.angelika.boutique.service.SellerService;

import java.util.List;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class SellerRestAssuredTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SellerService sellerService;

    private static final String NUMBER = "89137650976";
    private static final String USER_ROLE = "USER";
    private static final String SELLER_ROLE = "SELLER";
    private static final Long SELLER_ID = 1L;

    private Seller testSeller;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);
        testSeller = new Seller();
        testSeller.setId(SELLER_ID);
        testSeller.setName("seller_test");
        testSeller.setNumber(NUMBER);
        testSeller.setEmail("seller@test.com");
    }


    @Test
    @WithMockUser(username = NUMBER, roles = SELLER_ROLE)
    void sellerPage_Success() {
        when(sellerService.getByNumber(NUMBER)).thenReturn(testSeller);
        when(sellerService.getById(SELLER_ID)).thenReturn(testSeller);

        given()
                .when()
                .get("/seller/profile/{id}", SELLER_ID)
                .then()
                .statusCode(200);
    }

    @Test
    void sellerPage_Unauthenticated_RedirectToLogin() {
        given()
                .when()
                .get("/seller/profile/{id}", SELLER_ID)
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }

    @Test
    @WithMockUser(username = NUMBER, roles = USER_ROLE)
    void sellerPage_UserRole_ShouldReturnForbidden() {
        given()
                .when()
                .get("/seller/profile/{id}", SELLER_ID)
                .then()
                .statusCode(403);
    }

    @Test
    @WithMockUser(username = NUMBER, roles = SELLER_ROLE)
    void sellerPage_DifferentId_RedirectToWelcome() {
        when(sellerService.getByNumber(NUMBER)).thenReturn(testSeller);
        given()
                .when()
                .get("/seller/profile/{id}", 9L)
                .then()
                .statusCode(302)
                .header("Location", "/welcome");
    }

    @Test
    @WithMockUser(username = NUMBER, roles = SELLER_ROLE)
    void sellerPage_SellerNotFoundByNumber_ThrowsException() {
        when(sellerService.getByNumber(NUMBER)).thenThrow(new ResourceNotFoundException(Seller.class, NUMBER));
        given()
                .when()
                .get("/seller/profile/{id}", SELLER_ID)
                .then()
                .statusCode(404);
    }

    @Test
    @WithMockUser(username = NUMBER, roles = SELLER_ROLE)
    void sellerPage_SellerNotFoundById_ThrowsException() {
        when(sellerService.getByNumber(NUMBER)).thenReturn(testSeller);
        when(sellerService.getById(SELLER_ID)).thenThrow(new ResourceNotFoundException(Seller.class, SELLER_ID));
        given()
                .when()
                .get("/seller/profile/{id}", SELLER_ID)
                .then()
                .statusCode(404);
    }

    @Test
    @WithMockUser(username = NUMBER, roles = USER_ROLE)
    void getPublicPage_Success() {
        when(sellerService.getById(SELLER_ID)).thenReturn(testSeller);
        given()
                .queryParam("role", "USER")
                .when()
                .get("/public-seller/{id}", SELLER_ID)
                .then()
                .statusCode(200);
    }

    @Test
    void getPublicPage_Unauthenticated_RedirectToLogin() {
        given()
                .queryParam("role", "USER")
                .when()
                .get("/public-seller/{id}", SELLER_ID)
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void getAll_AdminSuccess() {
        when(sellerService.getAll()).thenReturn(List.of(testSeller));
        given()
                .when()
                .get("/admin/sellers")
                .then()
                .statusCode(200);
    }

    @Test
    @WithMockUser(username = NUMBER, roles = SELLER_ROLE)
    void getAll_Seller_ShouldReturnForbidden() {
        given()
                .when()
                .get("/admin/sellers")
                .then()
                .statusCode(403);
    }

    @Test
    @WithMockUser(username = NUMBER, roles = SELLER_ROLE)
    void update_Success() {
        when(sellerService.getByNumber(NUMBER)).thenReturn(testSeller);
        doNothing().when(sellerService).update(any(UpdateEntityDto.class), eq(SELLER_ID));

        given()
                .param("name", "new_name")
                .param("number", NUMBER)
                .param("email", "new@mail.com")
                .param("oldPassword", "1234")
                .when()
                .put("/seller/profile/{id}", SELLER_ID)
                .then()
                .statusCode(302)
                .header("Location", "/logout");
    }

    @Test
    @WithMockUser(username = NUMBER, roles = SELLER_ROLE)
    void update_OtherSeller_RedirectToWelcome() {
        Seller otherSeller = new Seller();
        otherSeller.setId(5L);
        when(sellerService.getByNumber(NUMBER)).thenReturn(otherSeller);

        given()
                .param("name", "new_name")
                .param("number", NUMBER)
                .param("email", "new@mail.com")
                .param("oldPassword", "1234")
                .when()
                .put("/seller/profile/{id}", SELLER_ID)
                .then()
                .statusCode(302)
                .header("Location", "/welcome");
        verify(sellerService, never()).update(any(), any());
    }

    @Test
    @WithMockUser(username = NUMBER, roles = USER_ROLE)
    void update_UserRole_ShouldReturnForbidden() {
        given()
                .param("name", "new_name")
                .param("number", NUMBER)
                .param("email", "new@mail.com")
                .param("oldPassword", "1234")
                .param("newPassword", "5678")
                .when()
                .put("/seller/profile/{id}", SELLER_ID)
                .then()
                .statusCode(403);
    }

    @Test
    @WithMockUser(username = NUMBER, roles = SELLER_ROLE)
    void update_ValidationFailure_EmptyName() {
        when(sellerService.getByNumber(NUMBER)).thenReturn(testSeller);
        given()
                .param("name", "")
                .param("number", NUMBER)
                .param("email", "new@mail.com")
                .param("oldPassword", "1234")
                .when()
                .put("/seller/profile/{id}", SELLER_ID)
                .then()
                .statusCode(302)
                .header("Location", "/seller/profile/1");
        verify(sellerService, never()).update(any(), any());
    }

    @Test
    @WithMockUser(username = NUMBER, roles = SELLER_ROLE)
    void update_ValidationFailure_InvalidNumber() {
        when(sellerService.getByNumber(NUMBER)).thenReturn(testSeller);
        given()
                .param("name", "new_name")
                .param("number", "123")
                .param("email", "new@mail.com")
                .param("oldPassword", "1234")
                .when()
                .put("/seller/profile/{id}", SELLER_ID)
                .then()
                .statusCode(302)
                .header("Location", "/seller/profile/1");
        verify(sellerService, never()).update(any(), any());
    }

    @Test
    @WithMockUser(username = NUMBER, roles = SELLER_ROLE)
    void update_ValidationFailure_InvalidEmail() {
        when(sellerService.getByNumber(NUMBER)).thenReturn(testSeller);
        given()
                .param("name", "new_name")
                .param("number", NUMBER)
                .param("email", "invalid-email")
                .param("oldPassword", "1234")
                .when()
                .put("/seller/profile/{id}", SELLER_ID)
                .then()
                .statusCode(302)
                .header("Location", "/seller/profile/1");
        verify(sellerService, never()).update(any(), any());
    }

    @Test
    @WithMockUser(username = NUMBER, roles = SELLER_ROLE)
    void update_ValidationFailure_EmptyOldPassword() {
        when(sellerService.getByNumber(NUMBER)).thenReturn(testSeller);
        given()
                .param("name", "new_name")
                .param("number", NUMBER)
                .param("email", "new@mail.com")
                .param("oldPassword", "")
                .when()
                .put("/seller/profile/{id}", SELLER_ID)
                .then()
                .statusCode(302)
                .header("Location", "/seller/profile/1");
        verify(sellerService, never()).update(any(), any());
    }

    @Test
    @WithMockUser(username = NUMBER, roles = SELLER_ROLE)
    void update_ValidationFailure_NewPasswordShort() {
        when(sellerService.getByNumber(NUMBER)).thenReturn(testSeller);
        given()
                .param("name", "new_name")
                .param("number", NUMBER)
                .param("email", "new@mail.com")
                .param("oldPassword", "1234")
                .param("newPassword", "123")
                .when()
                .put("/seller/profile/{id}", SELLER_ID)
                .then()
                .statusCode(302)
                .header("Location", "/seller/profile/1");
        verify(sellerService, never()).update(any(), any());
    }

    @Test
    @WithMockUser(username = NUMBER, roles = SELLER_ROLE)
    void update_ValidationFailure_NewPasswordLong() {
        when(sellerService.getByNumber(NUMBER)).thenReturn(testSeller);
        given()
                .param("name", "new_name")
                .param("number", NUMBER)
                .param("email", "new@mail.com")
                .param("oldPassword", "1234")
                .param("newPassword", "12345678900")
                .when()
                .put("/seller/profile/{id}", SELLER_ID)
                .then()
                .statusCode(302)
                .header("Location", "/seller/profile/1");
        verify(sellerService, never()).update(any(), any());
    }

    @Test
    @WithMockUser(username = NUMBER, roles = SELLER_ROLE)
    void update_SellerNotFound_ThrowsException() {
        when(sellerService.getByNumber(NUMBER)).thenThrow(new ResourceNotFoundException(Seller.class, NUMBER));

        given()
                .param("name", "new_name")
                .param("number", NUMBER)
                .param("email", "new@mail.com")
                .param("oldPassword", "1234")
                .when()
                .put("/seller/profile/{id}", SELLER_ID)
                .then()
                .statusCode(404);
        verify(sellerService, never()).update(any(), any());
    }

    @Test
    @WithMockUser(username = NUMBER, roles = SELLER_ROLE)
    void update_ResourceExists_ThrowsException() {
        when(sellerService.getByNumber(NUMBER)).thenReturn(testSeller);
        doThrow(new ResourceExistsException(Seller.class, NUMBER))
                .when(sellerService).update(any(UpdateEntityDto.class), eq(SELLER_ID));

        given()
                .param("name", "new_name")
                .param("number", NUMBER)
                .param("email", "new@mail.com")
                .param("oldPassword", "1234")
                .when()
                .put("/seller/profile/{id}", SELLER_ID)
                .then()
                .statusCode(302)
                .header("Location", "/seller/profile/1");
    }

    @Test
    @WithMockUser(username = NUMBER, roles = SELLER_ROLE)
    void delete_Success() {
        when(sellerService.getByNumber(NUMBER)).thenReturn(testSeller);
        doNothing().when(sellerService).delete(SELLER_ID);

        given()
                .when()
                .delete("/sellers/{id}", SELLER_ID)
                .then()
                .statusCode(302)
                .header("Location", "/registration");
    }

    @Test
    @WithMockUser(username = NUMBER, roles = SELLER_ROLE)
    void delete_OtherSeller_RedirectToWelcome() {
        Seller otherSeller = new Seller();
        otherSeller.setId(5L);
        when(sellerService.getByNumber(NUMBER)).thenReturn(otherSeller);

        given()
                .when()
                .delete("/sellers/{id}", SELLER_ID)
                .then()
                .statusCode(302)
                .header("Location", "/welcome");
        verify(sellerService, never()).delete(any());
    }

    @Test
    @WithMockUser(username = NUMBER, roles = USER_ROLE)
    void delete_UserRole_ShouldReturnForbidden() {
        given()
                .when()
                .delete("/sellers/{id}", SELLER_ID)
                .then()
                .statusCode(403);
    }

    @Test
    @WithMockUser(username = NUMBER, roles = SELLER_ROLE)
    void delete_SellerNotFound_ThrowsException() {
        when(sellerService.getByNumber(NUMBER)).thenThrow(new ResourceNotFoundException(Seller.class, NUMBER));
        given()
                .when()
                .delete("/sellers/{id}", SELLER_ID)
                .then()
                .statusCode(404);
        verify(sellerService, never()).delete(any());
    }

    @Test
    @WithMockUser(username = NUMBER, roles = SELLER_ROLE)
    void delete_SellerNotFoundById_ThrowsException() {
        when(sellerService.getByNumber(NUMBER)).thenReturn(testSeller);
        doThrow(new ResourceNotFoundException(Seller.class, SELLER_ID))
                .when(sellerService)
                .delete(eq(SELLER_ID));
        given()
                .when()
                .delete("/sellers/{id}", SELLER_ID)
                .then()
                .statusCode(404);
    }
}