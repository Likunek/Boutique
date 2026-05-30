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
import ru.angelika.boutique.dto.PickupPointDto;
import ru.angelika.boutique.exception.ResourceExistsException;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.PickupPoint;
import ru.angelika.boutique.model.Storage;
import ru.angelika.boutique.service.PickupPointService;
import ru.angelika.boutique.service.StorageService;

import java.util.List;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class PickupPointRestAssuredTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PickupPointService pickupPointService;

    @MockitoBean
    private StorageService storageService;

    private static final Long POINT_ID = 10L;
    private static final Long STORAGE_ID = 20L;

    private PickupPointDto dto;
    private Storage storage;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);

        storage = new Storage();
        storage.setId(STORAGE_ID);

        dto = PickupPointDto.builder()
                .city("City")
                .address("Address")
                .storageId(STORAGE_ID)
                .build();
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void addPoint_Success() {
        doNothing().when(pickupPointService).add(any(PickupPointDto.class));

        given()
                .param("city", dto.getCity())
                .param("address", dto.getAddress())
                .param("storageId", STORAGE_ID)
                .when()
                .post("/admin/points/add")
                .then()
                .statusCode(302)
                .header("Location", "/admin/points/add");

        verify(pickupPointService).add(any(PickupPointDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addPoint_ValidationFailure_EmptyCity() {
        given()
                .param("city", "")
                .param("address", dto.getAddress())
                .param("storageId", STORAGE_ID)
                .when()
                .post("/admin/points/add")
                .then()
                .statusCode(302)
                .header("Location", "/admin/points/add");

        verify(pickupPointService, never()).add(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addPoint_ValidationFailure_CityLong() {
        given()
                .param("city", "A".repeat(21))
                .param("address", dto.getAddress())
                .param("storageId", STORAGE_ID)
                .when()
                .post("/admin/points/add")
                .then()
                .statusCode(302)
                .header("Location", "/admin/points/add");

        verify(pickupPointService, never()).add(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addPoint_ValidationFailure_EmptyAddress() {
        given()
                .param("city", dto.getCity())
                .param("address", "")
                .param("storageId", STORAGE_ID)
                .when()
                .post("/admin/points/add")
                .then()
                .statusCode(302)
                .header("Location", "/admin/points/add");

        verify(pickupPointService, never()).add(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addPoint_ValidationFailure_AddressLong() {
        given()
                .param("city", dto.getCity())
                .param("address", "A".repeat(51))
                .param("storageId", STORAGE_ID)
                .when()
                .post("/admin/points/add")
                .then()
                .statusCode(302)
                .header("Location", "/admin/points/add");

        verify(pickupPointService, never()).add(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addPoint_ValidationFailure_NullStorageId() {
        given()
                .param("city", dto.getCity())
                .param("address", dto.getAddress())
                .when()
                .post("/admin/points/add")
                .then()
                .statusCode(302)
                .header("Location", "/admin/points/add");

        verify(pickupPointService, never()).add(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addPoint_PointExists_ThrowsException() {
        doThrow(new ResourceExistsException(PickupPoint.class, ""))
                .when(pickupPointService).add(any(PickupPointDto.class));

        given()
                .param("city", dto.getCity())
                .param("address", dto.getAddress())
                .param("storageId", STORAGE_ID)
                .when()
                .post("/admin/points/add")
                .then()
                .statusCode(302)
                .header("Location", "/admin/points/add");

        verify(pickupPointService).add(any(PickupPointDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addPoint_StorageNotFound_ThrowsException() {
        doThrow(new ResourceNotFoundException(Storage.class, storage.getId()))
                .when(pickupPointService).add(any(PickupPointDto.class));

        given()
                .param("city", dto.getCity())
                .param("address", dto.getAddress())
                .param("storageId", STORAGE_ID)
                .when()
                .post("/admin/points/add")
                .then()
                .statusCode(302)
                .header("Location", "/admin/points/add");

        verify(pickupPointService).add(any(PickupPointDto.class));
    }
    @Test
    @WithMockUser(roles = "SELLER")
    void addPoint_SellerRole_ShouldReturnForbidden() {
        given()
                .param("city", dto.getCity())
                .param("address", dto.getAddress())
                .param("storageId", STORAGE_ID)
                .when()
                .post("/admin/points/add")
                .then()
                .statusCode(403);

        verify(pickupPointService, never()).add(any());
    }

    @Test
    void addPoint_Unauthenticated_RedirectToLogin() {
        given()
                .param("city", dto.getCity())
                .param("address", dto.getAddress())
                .param("storageId", STORAGE_ID)
                .when()
                .post("/admin/points/add")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
        verify(pickupPointService, never()).add(any());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void getFormNewPoint_Success() {
        when(storageService.getAll()).thenReturn(List.of(storage));

        given()
                .when()
                .get("/admin/points/add")
                .then()
                .statusCode(200);

        verify(storageService).getAll();
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void getFormNewPoint_SellerRole_ShouldReturnForbidden() {
        given()
                .when()
                .get("/admin/points/add")
                .then()
                .statusCode(403);

        verify(storageService, never()).getAll();
    }

    @Test
    void getFormNewPoint_Unauthenticated_RedirectToLogin() {
        given()
                .when()
                .get("/admin/points/add")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
        verify(storageService, never()).getAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllPoints_Success() {
        when(pickupPointService.getAll()).thenReturn(List.of());
        when(storageService.getAll()).thenReturn(List.of(storage));

        given()
                .when()
                .get("/admin/points")
                .then()
                .statusCode(200);

        verify(pickupPointService).getAll();
        verify(storageService).getAll();
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void getAllPoints_SellerRole_ShouldReturnForbidden() {
        given()
                .when()
                .get("/admin/points")
                .then()
                .statusCode(403);

        verify(pickupPointService, never()).getAll();
    }

    @Test
    void getAllPoints_Unauthenticated_RedirectToLogin() {
        given()
                .when()
                .get("/admin/points")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
        verify(pickupPointService, never()).getAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePoint_Success() {
        doNothing().when(pickupPointService).update(eq(POINT_ID), any(PickupPointDto.class));

        given()
                .param("city", "Updated City")
                .param("address", "Updated Address")
                .param("storageId", STORAGE_ID.toString())
                .when()
                .put("/admin/points/{id}", POINT_ID)
                .then()
                .statusCode(302)
                .header("Location", "/admin/points");

        verify(pickupPointService).update(eq(POINT_ID), any(PickupPointDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePoint_ValidationFailure_EmptyCity() {
        given()
                .param("city", "")
                .param("address", dto.getAddress())
                .param("storageId", STORAGE_ID)
                .when()
                .put("/admin/points/add", POINT_ID)
                .then()
                .statusCode(500);

        verify(pickupPointService, never()).add(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePoint_ValidationFailure_CityLong() {
        given()
                .param("city", "A".repeat(21))
                .param("address", dto.getAddress())
                .param("storageId", STORAGE_ID)
                .when()
                .put("/admin/points/add", POINT_ID)
                .then()
                .statusCode(500);

        verify(pickupPointService, never()).add(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePoint_ValidationFailure_EmptyAddress() {
        given()
                .param("city", dto.getCity())
                .param("address", "")
                .param("storageId", STORAGE_ID)
                .when()
                .put("/admin/points/add", POINT_ID)
                .then()
                .statusCode(500);

        verify(pickupPointService, never()).add(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePoint_ValidationFailure_AddressLong() {
        given()
                .param("city", dto.getCity())
                .param("address", "A".repeat(51))
                .param("storageId", STORAGE_ID)
                .when()
                .put("/admin/points/add", POINT_ID)
                .then()
                .statusCode(500);

        verify(pickupPointService, never()).add(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePoint_ValidationFailure_NullStorageId() {
        given()
                .param("city", dto.getCity())
                .param("address", dto.getAddress())
                .when()
                .put("/admin/points/add", POINT_ID)
                .then()
                .statusCode(500);

        verify(pickupPointService, never()).add(any());
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePoint_PointNotFound_ThrowsException() {
        doThrow(new ResourceNotFoundException(PickupPoint.class, POINT_ID))
                .when(pickupPointService).update(eq(POINT_ID), any(PickupPointDto.class));

        given()
                .param("city", "City")
                .param("address", "Address")
                .param("storageId", STORAGE_ID)
                .when()
                .put("/admin/points/{id}", POINT_ID)
                .then()
                .statusCode(404);

        verify(pickupPointService).update(eq(POINT_ID), any(PickupPointDto.class));
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void updatePoint_SellerRole_ShouldReturnForbidden() {
        given()
                .param("city", "City")
                .when()
                .put("/admin/points/{id}", POINT_ID)
                .then()
                .statusCode(403);

        verify(pickupPointService, never()).update(anyLong(), any());
    }

    @Test
    void updatePoint_Unauthenticated_RedirectToLogin() {
        given()
                .param("city", "City")
                .when()
                .put("/admin/points/{id}", POINT_ID)
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
        verify(pickupPointService, never()).update(anyLong(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deletePoint_Success() {
        doNothing().when(pickupPointService).delete(POINT_ID);

        given()
                .when()
                .delete("/admin/points/{id}", POINT_ID)
                .then()
                .statusCode(302)
                .header("Location", "/admin/points");

        verify(pickupPointService).delete(POINT_ID);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deletePoint_PointNotFound_ThrowsException() {
        doThrow(new ResourceNotFoundException(PickupPoint.class, POINT_ID))
                .when(pickupPointService).delete(POINT_ID);

        given()
                .when()
                .delete("/admin/points/{id}", POINT_ID)
                .then()
                .statusCode(404);

        verify(pickupPointService).delete(POINT_ID);
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void deletePoint_SellerRole_ShouldReturnForbidden() {
        given()
                .when()
                .delete("/admin/points/{id}", POINT_ID)
                .then()
                .statusCode(403);

        verify(pickupPointService, never()).delete(anyLong());
    }

    @Test
    void deletePoint_Unauthenticated_RedirectToLogin() {
        given()
                .when()
                .delete("/admin/points/{id}", POINT_ID)
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
        verify(pickupPointService, never()).delete(anyLong());
    }
}