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
import ru.angelika.boutique.service.SupplyService;

import java.util.List;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class SupplyRestAssuredTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SupplyService supplyService;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllSupplies_Success() {
        when(supplyService.getAll()).thenReturn(List.of());

        given()
                .when()
                .get("/admin/supplies")
                .then()
                .statusCode(200);

        verify(supplyService).getAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllSupplies_ThrowsException() {
        when(supplyService.getAll()).thenThrow(new RuntimeException("Database error"));

        given()
                .when()
                .get("/admin/supplies")
                .then()
                .statusCode(500);

        verify(supplyService).getAll();
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void getAllSupplies_SellerRole_ShouldReturnForbidden() {
        given()
                .when()
                .get("/admin/supplies")
                .then()
                .statusCode(403);

        verify(supplyService, never()).getAll();
    }

    @Test
    void getAllSupplies_Unauthenticated_RedirectToLogin() {
        given()
                .when()
                .get("/admin/supplies")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));

        verify(supplyService, never()).getAll();
    }
}