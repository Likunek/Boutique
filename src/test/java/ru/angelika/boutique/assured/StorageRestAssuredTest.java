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
import ru.angelika.boutique.dto.StorageDto;
import ru.angelika.boutique.exception.ResourceExistsException;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.Storage;
import ru.angelika.boutique.service.StorageService;

import java.util.List;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class StorageRestAssuredTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StorageService storageService;

    private static final Long STORAGE_ID = 1L;

    private StorageDto dto;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);

        dto = StorageDto.builder()
                .city("City")
                .address("address")
                .maxCapacity(2000L)
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addStorage_Success() {
        doNothing().when(storageService).add(any(StorageDto.class));

        given()
                .param("city", dto.getCity())
                .param("address", dto.getAddress())
                .param("maxCapacity", dto.getMaxCapacity())
                .when()
                .post("/admin/storages/add")
                .then()
                .statusCode(200);

        verify(storageService).add(any(StorageDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addStorage_ValidationFailure_EmptyCity() {
        given()
                .param("city", "")
                .param("address", dto.getAddress())
                .param("maxCapacity", dto.getMaxCapacity())
                .when()
                .post("/admin/storages/add")
                .then()
                .statusCode(200);

        verify(storageService, never()).add(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addStorage_ValidationFailure_CityLong() {
        given()
                .param("city", "C".repeat(21))
                .param("address", dto.getAddress())
                .param("maxCapacity", dto.getMaxCapacity())
                .when()
                .post("/admin/storages/add")
                .then()
                .statusCode(200);

        verify(storageService, never()).add(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addStorage_ValidationFailure_EmptyAddress() {
        given()
                .param("city", dto.getCity())
                .param("address", "")
                .param("maxCapacity", dto.getMaxCapacity())
                .when()
                .post("/admin/storages/add")
                .then()
                .statusCode(200);

        verify(storageService, never()).add(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addStorage_ValidationFailure_AddressLong() {
        given()
                .param("city", dto.getCity())
                .param("address", "A".repeat(51))
                .param("maxCapacity", dto.getMaxCapacity())
                .when()
                .post("/admin/storages/add")
                .then()
                .statusCode(200);

        verify(storageService, never()).add(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addStorage_ValidationFailure_NullMaxCapacity() {
        given()
                .param("city", dto.getCity())
                .param("address", dto.getAddress())
                .when()
                .post("/admin/storages/add")
                .then()
                .statusCode(200);

        verify(storageService, never()).add(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addStorage_ValidationFailure_MaxCapacityMin() {
        given()
                .param("city", dto.getCity())
                .param("address", dto.getAddress())
                .param("maxCapacity", 500)
                .when()
                .post("/admin/storages/add")
                .then()
                .statusCode(200);

        verify(storageService, never()).add(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addStorage_StorageNotFound_ThrowsException() {
        doThrow(new ResourceExistsException(Storage.class, ""))
                .when(storageService).add(any(StorageDto.class));

        given()
                .param("city", dto.getCity())
                .param("address", dto.getAddress())
                .param("maxCapacity", dto.getMaxCapacity().toString())
                .when()
                .post("/admin/storages/add")
                .then()
                .statusCode(200);

        verify(storageService).add(any(StorageDto.class));
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void addStorage_SellerRole_ShouldReturnForbidden() {
        given()
                .param("city", dto.getCity())
                .param("address", dto.getAddress())
                .param("maxCapacity", dto.getMaxCapacity().toString())
                .when()
                .post("/admin/storages/add")
                .then()
                .statusCode(403);

        verify(storageService, never()).add(any());
    }

    @Test
    void addStorage_Unauthenticated_RedirectToLogin() {
        given()
                .param("city", dto.getCity())
                .param("address", dto.getAddress())
                .param("maxCapacity", dto.getMaxCapacity().toString())
                .when()
                .post("/admin/storages/add")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
        verify(storageService, never()).add(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getFormNewStorage_Success() {
        given()
                .when()
                .get("/admin/storages/add")
                .then()
                .statusCode(200);
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void getFormNewStorage_SellerRole_ShouldReturnForbidden() {
        given()
                .when()
                .get("/admin/storages/add")
                .then()
                .statusCode(403);
    }

    @Test
    void getFormNewStorage_Unauthenticated_RedirectToLogin() {
        given()
                .when()
                .get("/admin/storages/add")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllStorage_Success() {
        when(storageService.getAll()).thenReturn(List.of());

        given()
                .when()
                .get("/admin/storages")
                .then()
                .statusCode(200);

        verify(storageService).getAll();
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void getAllStorage_SellerRole_ShouldReturnForbidden() {
        given()
                .when()
                .get("/admin/storages")
                .then()
                .statusCode(403);

        verify(storageService, never()).getAll();
    }

    @Test
    void getAllStorage_Unauthenticated_RedirectToLogin() {
        given()
                .when()
                .get("/admin/storages")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
        verify(storageService, never()).getAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateStorage_Success() {
        doNothing().when(storageService).update(any(StorageDto.class), eq(STORAGE_ID));

        given()
                .param("city", "Updated City")
                .param("address", "Updated Address")
                .param("maxCapacity", 3000)
                .when()
                .put("/admin/storages/{id}", STORAGE_ID)
                .then()
                .statusCode(302)
                .header("Location", "/admin/storages");

        verify(storageService).update(any(StorageDto.class), eq(STORAGE_ID));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateStorage_StorageNotFound_ThrowsException() {
        doThrow(new ResourceNotFoundException(Storage.class, STORAGE_ID))
                .when(storageService).update(any(StorageDto.class), eq(STORAGE_ID));

        given()
                .param("city", "City")
                .param("address", "Address")
                .param("maxCapacity", 1500)
                .when()
                .put("/admin/storages/{id}", STORAGE_ID)
                .then()
                .statusCode(404);

        verify(storageService).update(any(StorageDto.class), eq(STORAGE_ID));
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void updateStorage_SellerRole_ShouldReturnForbidden() {
        given()
                .param("city", "City")
                .param("address", "Address")
                .param("maxCapacity", 1500)
                .when()
                .put("/admin/storages/{id}", STORAGE_ID)
                .then()
                .statusCode(403);

        verify(storageService, never()).update(any(), anyLong());
    }

    @Test
    void updateStorage_Unauthenticated_RedirectToLogin() {
        given()
                .param("city", "City")
                .param("address", "Address")
                .param("maxCapacity", 1500)
                .when()
                .put("/admin/storages/{id}", STORAGE_ID)
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
        verify(storageService, never()).update(any(), anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteStorage_Success() {
        doNothing().when(storageService).delete(STORAGE_ID);

        given()
                .when()
                .delete("/admin/storages/{id}", STORAGE_ID)
                .then()
                .statusCode(302)
                .header("Location", "/admin/storages");

        verify(storageService).delete(STORAGE_ID);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteStorage_StorageNotFound_ThrowsException() {
        doThrow(new ResourceNotFoundException(Storage.class, STORAGE_ID))
                .when(storageService).delete(STORAGE_ID);

        given()
                .when()
                .delete("/admin/storages/{id}", STORAGE_ID)
                .then()
                .statusCode(404);

        verify(storageService).delete(STORAGE_ID);
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void deleteStorage_SellerRole_ShouldReturnForbidden() {
        given()
                .when()
                .delete("/admin/storages/{id}", STORAGE_ID)
                .then()
                .statusCode(403);

        verify(storageService, never()).delete(anyLong());
    }

    @Test
    void deleteStorage_Unauthenticated_RedirectToLogin() {
        given()
                .when()
                .delete("/admin/storages/{id}", STORAGE_ID)
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
        verify(storageService, never()).delete(anyLong());
    }
}