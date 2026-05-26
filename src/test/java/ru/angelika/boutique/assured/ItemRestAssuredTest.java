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
import ru.angelika.boutique.dto.ItemDto;
import ru.angelika.boutique.exception.ResourceExistsException;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.Item;
import ru.angelika.boutique.model.Seller;
import ru.angelika.boutique.service.ItemService;
import ru.angelika.boutique.service.SellerService;

import java.util.List;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ItemRestAssuredTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private SellerService sellerService;

    private static final String SELLER_NUMBER = "89137650976";
    private static final String USER_ROLE = "USER";
    private static final String SELLER_ROLE = "SELLER";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final Long SELLER_ID = 1L;
    private static final Long ITEM_ID = 10L;
    private static final Long OTHER_ID = 9L;

    private Seller seller;
    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);

        seller = new Seller();
        seller.setId(SELLER_ID);
        seller.setName("seller_test");
        seller.setNumber(SELLER_NUMBER);
        seller.setEmail("seller@test.com");

        item = new Item();
        item.setId(ITEM_ID);
        item.setName("Test Item");
        item.setCostPrice(100.0);
        item.setWeight(1.5);
        item.setSquare(0.5);
        item.setSeller(seller);

        itemDto = ItemDto.builder()
                .name("New Item")
                .costPrice(200.0)
                .weight(2.0)
                .square(1.0)
                .build();
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void getFormNewItem_Success() {
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        given()
                .when()
                .get("/seller/add-item")
                .then()
                .statusCode(200);
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void getFormNewItem_SellerNotFound_ThrowsException() {
        when(sellerService.getByNumber(SELLER_NUMBER)).thenThrow(new ResourceNotFoundException(Seller.class, SELLER_NUMBER));
        given()
                .when()
                .get("/seller/add-item")
                .then()
                .statusCode(404);
    }
    @Test
    void getFormNewItem_Unauthenticated_RedirectToLogin() {
        given()
                .when()
                .get("/seller/add-item")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = USER_ROLE)
    void getFormNewItem_UserRole_ShouldReturnForbidden() {
        given()
                .when()
                .get("/seller/add-item")
                .then()
                .statusCode(403);
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void addItem_Success() {
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        doNothing().when(itemService).add(any(ItemDto.class), eq(seller));

        given()
                .param("name", itemDto.getName())
                .param("costPrice", itemDto.getCostPrice())
                .param("weight", itemDto.getWeight())
                .param("square", itemDto.getSquare())
                .when()
                .post("/seller/add-item")
                .then()
                .statusCode(200);
        verify(itemService).add(any(ItemDto.class), eq(seller));
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void addItem_ValidationFailure_EmptyName() {
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        given()
                .param("name", "")
                .param("costPrice", itemDto.getCostPrice())
                .param("weight", itemDto.getWeight())
                .param("square", itemDto.getSquare())
                .when()
                .post("/seller/add-item")
                .then()
                .statusCode(200);
        verify(itemService, never()).add(any(), any());
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void addItem_ValidationFailure_WeightLow() {
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        given()
                .param("name", itemDto.getName())
                .param("costPrice", itemDto.getCostPrice())
                .param("weight", "0.05")
                .param("square", itemDto.getSquare())
                .when()
                .post("/seller/add-item")
                .then()
                .statusCode(200);
        verify(itemService, never()).add(any(), any());
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void addItem_ValidationFailure_WeightHigh() {
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        given()
                .param("name", itemDto.getName())
                .param("costPrice", itemDto.getCostPrice())
                .param("weight", "25")
                .param("square", itemDto.getSquare())
                .when()
                .post("/seller/add-item")
                .then()
                .statusCode(200);
        verify(itemService, never()).add(any(), any());
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void addItem_ValidationFailure_SquareLow() {
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        given()
                .param("name", itemDto.getName())
                .param("costPrice", itemDto.getCostPrice())
                .param("weight", itemDto.getWeight())
                .param("square", "0.005")
                .when()
                .post("/seller/add-item")
                .then()
                .statusCode(200);
        verify(itemService, never()).add(any(), any());
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void addItem_ValidationFailure_SquareHigh() {
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        given()
                .param("name", itemDto.getName())
                .param("costPrice", itemDto.getCostPrice())
                .param("weight", itemDto.getWeight())
                .param("square", "5")
                .when()
                .post("/seller/add-item")
                .then()
                .statusCode(200);
        verify(itemService, never()).add(any(), any());
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void addItem_SellerNotFound_ThrowsException() {
        when(sellerService.getByNumber(SELLER_NUMBER)).thenThrow(new ResourceNotFoundException(Seller.class, SELLER_NUMBER));

        given()
                .param("name", itemDto.getName())
                .param("costPrice", itemDto.getCostPrice())
                .param("weight", itemDto.getWeight())
                .param("square", itemDto.getSquare())
                .when()
                .post("/seller/add-item")
                .then()
                .statusCode(404);
        verify(itemService, never()).add(any(ItemDto.class), eq(seller));
    }
    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void addItem_ResourceExists_ThrowsException() {
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        doThrow(new ResourceExistsException(Item.class, ""))
                .when(itemService).add(any(ItemDto.class), eq(seller));

        given()
                .param("name", itemDto.getName())
                .param("costPrice", itemDto.getCostPrice())
                .param("weight", itemDto.getWeight())
                .param("square", itemDto.getSquare())
                .when()
                .post("/seller/add-item")
                .then()
                .statusCode(200);
        verify(itemService).add(any(ItemDto.class), eq(seller));
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void getAllItemsSeller_Success() {
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        when(itemService.getAllBySellerWithStorages(seller)).thenReturn(List.of(item));
        given()
                .when()
                .get("/seller/items")
                .then()
                .statusCode(200);
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void getAllItemsSeller_SellerNotFound_ThrowsException() {
        when(sellerService.getByNumber(SELLER_NUMBER)).thenThrow(new ResourceNotFoundException(Seller.class, SELLER_ID));
        given()
                .when()
                .get("/seller/items")
                .then()
                .statusCode(404);

        verify(itemService, never()).getAllBySellerWithStorages(seller);
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = USER_ROLE)
    void getAllItemsSeller_UserRole_ShouldReturnForbidden() {
        given()
                .when()
                .get("/seller/items")
                .then()
                .statusCode(403);
        verify(itemService, never()).getAllBySellerWithStorages(seller);
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void getById_Success() {
        when(itemService.getById(ITEM_ID)).thenReturn(item);
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        given()
                .when()
                .get("/items/{id}", ITEM_ID)
                .then()
                .statusCode(200);
        verify(itemService).getById(ITEM_ID);
        verify(sellerService).getByNumber(SELLER_NUMBER);
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void getById_OtherSeller_RedirectToWelcome() {
        Seller otherSeller = new Seller();
        otherSeller.setId(OTHER_ID);
        when(itemService.getById(ITEM_ID)).thenReturn(item);
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(otherSeller);
        given()
                .when()
                .get("/items/{id}", ITEM_ID)
                .then()
                .statusCode(302)
                .header("Location", "/welcome");
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void getById_ItemNotFound_ThrowsException() {
        when(itemService.getById(ITEM_ID)).thenThrow(new ResourceNotFoundException(Item.class, ITEM_ID));
        given()
                .when()
                .get("/items/{id}", ITEM_ID)
                .then()
                .statusCode(404);
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void getById_SellerNotFound_ThrowsException() {
        when(itemService.getById(ITEM_ID)).thenReturn(item);
        when(sellerService.getByNumber(SELLER_NUMBER)).thenThrow(new ResourceNotFoundException(Seller.class, SELLER_ID));
        given()
                .when()
                .get("/items/{id}", ITEM_ID)
                .then()
                .statusCode(404);
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void getAllItemsAdmin__Success() {
        when(itemService.getAll()).thenReturn(List.of(item));
        given()
                .queryParam("unverified", "true")
                .when()
                .get("/admin/items")
                .then()
                .statusCode(200);
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void getAllItemsAdmin_WithUnverifiedFalse_Success() {
        when(itemService.getAllVerifyFalse()).thenReturn(List.of(item));
        given()
                .queryParam("unverified", "false")
                .when()
                .get("/admin/items")
                .then()
                .statusCode(200);
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void getAllItemsAdmin_Seller_ShouldReturnForbidden() {
        given()
                .when()
                .get("/admin/items")
                .then()
                .statusCode(403);
        verify(itemService, never()).getAll();
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void updateVerify_Success() {
        doNothing().when(itemService).updateVerify(ITEM_ID, true);
        given()
                .queryParam("verify", "true")
                .when()
                .put("/admin/items/{id}", ITEM_ID)
                .then()
                .statusCode(302)
                .header("Location", "/admin/items");
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void updateVerify_Seller_ShouldReturnForbidden() {
        given()
                .queryParam("verify", "true")
                .when()
                .put("/admin/items/{id}", ITEM_ID)
                .then()
                .statusCode(403);
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void updateItem_Success() {
        when(itemService.getById(ITEM_ID)).thenReturn(item);
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        doNothing().when(itemService).update(any(ItemDto.class), eq(ITEM_ID), eq(SELLER_ID));

        given()
                .param("name", "Updated Name")
                .param("costPrice", 150.0)
                .param("weight", 2.5)
                .param("square", 1.2)
                .when()
                .put("/items/{id}", ITEM_ID)
                .then()
                .statusCode(302)
                .header("Location", "/items/10");
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void updateItem_ValidationFailure_EmptyName() {
        when(itemService.getById(ITEM_ID)).thenReturn(item);
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        given()
                .param("name", "")
                .param("costPrice", 2)
                .param("weight", 0.2)
                .param("square", 1.2)
                .when()
                .put("/items/{id}", ITEM_ID)
                .then()
                .statusCode(302)
                .header("Location", "/items/10");
        verify(itemService, never()).update(any(), any(), any());
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void updateItem_ValidationFailure_SmallCostPrice() {
        when(itemService.getById(ITEM_ID)).thenReturn(item);
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        given()
                .param("name", "Updated Name")
                .param("costPrice", 0)
                .param("weight", 0.2)
                .param("square", 1.2)
                .when()
                .put("/items/{id}", ITEM_ID)
                .then()
                .statusCode(302)
                .header("Location", "/items/10");
        verify(itemService, never()).update(any(), any(), any());
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void updateItem_ValidationFailure_SmallWeight() {
        when(itemService.getById(ITEM_ID)).thenReturn(item);
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        given()
                .param("name", "Updated Name")
                .param("costPrice", 150.0)
                .param("weight", 0.05)
                .param("square", 1.2)
                .when()
                .put("/items/{id}", ITEM_ID)
                .then()
                .statusCode(302)
                .header("Location", "/items/10");
        verify(itemService, never()).update(any(), any(), any());
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void updateItem_ValidationFailure_BigWeight() {
        when(itemService.getById(ITEM_ID)).thenReturn(item);
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        given()
                .param("name", "Updated Name")
                .param("costPrice", 150.0)
                .param("weight", 22.0)
                .param("square", 1.2)
                .when()
                .put("/items/{id}", ITEM_ID)
                .then()
                .statusCode(302)
                .header("Location", "/items/10");
        verify(itemService, never()).update(any(), any(), any());
    }


    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void updateItem_ValidationFailure_SmallSquare() {
        when(itemService.getById(ITEM_ID)).thenReturn(item);
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        given()
                .param("name", "Updated Name")
                .param("costPrice", 150.0)
                .param("weight", 0.2)
                .param("square", 0)
                .when()
                .put("/items/{id}", ITEM_ID)
                .then()
                .statusCode(302)
                .header("Location", "/items/10");
        verify(itemService, never()).update(any(), any(), any());
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void updateItem_ValidationFailure_BigSquare() {
        when(itemService.getById(ITEM_ID)).thenReturn(item);
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        given()
                .param("name", "Updated Name")
                .param("costPrice", 150.0)
                .param("weight", 0.2)
                .param("square", 5)
                .when()
                .put("/items/{id}", ITEM_ID)
                .then()
                .statusCode(302)
                .header("Location", "/items/10");
        verify(itemService, never()).update(any(), any(), any());
    }
    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void updateItem_OtherSeller_RedirectToWelcome() {
        Seller otherSeller = new Seller();
        otherSeller.setId(OTHER_ID);
        when(itemService.getById(ITEM_ID)).thenReturn(item);
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(otherSeller);
        given()
                .param("name", "Updated Name")
                .param("costPrice", 150.0)
                .param("weight", 2.5)
                .param("square", 1.2)
                .when()
                .put("/items/{id}", ITEM_ID)
                .then()
                .statusCode(302)
                .header("Location", "/welcome");
        verify(itemService, never()).update(any(), any(), any());
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void updateItem_ItemNotFound_ThrowsException() {
        when(itemService.getById(ITEM_ID)).thenThrow(new ResourceNotFoundException(Item.class, ITEM_ID));

        given()
                .param("name", "Updated Name")
                .param("costPrice", 150.0)
                .param("weight", 2.5)
                .param("square", 1.2)
                .when()
                .put("/items/{id}", ITEM_ID)
                .then()
                .statusCode(404);
        verify(itemService).getById(ITEM_ID);
        verify(sellerService, never()).getByNumber(any());
        verify(itemService, never()).update(any(), any(), any());
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void updateItem_SellerNotFound_ThrowsException() {
        when(itemService.getById(ITEM_ID)).thenReturn(item);
        when(sellerService.getByNumber(SELLER_NUMBER)).thenThrow(new ResourceNotFoundException(Seller.class, SELLER_NUMBER));

        given()
                .param("name", "Updated Name")
                .param("costPrice", 150.0)
                .param("weight", 2.5)
                .param("square", 1.2)
                .when()
                .put("/items/{id}", ITEM_ID)
                .then()
                .statusCode(404);
        verify(sellerService).getByNumber(any());
        verify(itemService, never()).update(any(), any(), any());
    }
    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void updateItem_ResourceExists_ThrowsException() {
        when(itemService.getById(ITEM_ID)).thenReturn(item);
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        doThrow(new ResourceExistsException(Item.class, "Duplicate"))
                .when(itemService).update(any(ItemDto.class), eq(ITEM_ID), eq(SELLER_ID));

        given()
                .param("name", "Updated Name")
                .param("costPrice", 150.0)
                .param("weight", 2.5)
                .param("square", 1.2)
                .when()
                .put("/items/{id}", ITEM_ID)
                .then()
                .statusCode(302)
                .header("Location", "/items/10");
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = USER_ROLE)
    void updateItem_UserRole_ShouldReturnForbidden() {
        given()
                .param("name", "Updated Name")
                .param("costPrice", 150.0)
                .param("weight", 2.5)
                .param("square", 1.2)
                .when()
                .put("/items/{id}", ITEM_ID)
                .then()
                .statusCode(403);
        verify(itemService, never()).update(any(), any(), any());
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void deleteItem_SellerOwn_Success() {
        when(itemService.getById(ITEM_ID)).thenReturn(item);
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        doNothing().when(itemService).delete(ITEM_ID);

        given()
                .when()
                .delete("/items/{id}", ITEM_ID)
                .then()
                .statusCode(302)
                .header("Location", "/seller/items");
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void deleteItem_SellerOther_RedirectToWelcome() {
        Seller otherSeller = new Seller();
        otherSeller.setId(OTHER_ID);
        when(itemService.getById(ITEM_ID)).thenReturn(item);
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(otherSeller);

        given()
                .when()
                .delete("/items/{id}", ITEM_ID)
                .then()
                .statusCode(302)
                .header("Location", "/welcome");
        verify(itemService, never()).delete(any());
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void deleteItem_Admin_Success() {
        doNothing().when(itemService).delete(ITEM_ID);
        given()
                .when()
                .delete("/items/{id}", ITEM_ID)
                .then()
                .statusCode(302)
                .header("Location", "/admin/items");
        verify(itemService).delete(ITEM_ID);
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = USER_ROLE)
    void deleteItem_UserRole_ShouldReturnForbidden() {
        given()
                .when()
                .delete("/items/{id}", ITEM_ID)
                .then()
                .statusCode(403);
        verify(itemService, never()).delete(any());
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void deleteItem_ItemNotFound_ThrowsException() {
        when(itemService.getById(ITEM_ID)).thenThrow(new ResourceNotFoundException(Item.class, ITEM_ID));
        given()
                .when()
                .delete("/items/{id}", ITEM_ID)
                .then()
                .statusCode(404);
        verify(itemService, never()).delete(any());
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = SELLER_ROLE)
    void deleteItem_SellerNotFound_ThrowsException() {
        when(itemService.getById(ITEM_ID)).thenReturn(item);
        when(sellerService.getByNumber(SELLER_NUMBER)).thenThrow(new ResourceNotFoundException(Seller.class, SELLER_ID));
        given()
                .when()
                .delete("/items/{id}", ITEM_ID)
                .then()
                .statusCode(404);
        verify(itemService, never()).delete(any());
    }

}