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
import ru.angelika.boutique.dto.ItemsAtStorageDto;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.Item;
import ru.angelika.boutique.model.ItemsAtStorage;
import ru.angelika.boutique.model.Seller;
import ru.angelika.boutique.model.Storage;
import ru.angelika.boutique.service.ItemService;
import ru.angelika.boutique.service.ItemsAtStorageService;
import ru.angelika.boutique.service.SellerService;
import ru.angelika.boutique.service.StorageService;

import java.util.List;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ItemsAtStorageRestAssuredTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private SellerService sellerService;

    @MockitoBean
    private StorageService storageService;

    @MockitoBean
    private ItemsAtStorageService itemsAtStorageService;

    private static final String SELLER_NUMBER = "89137650976";
    private static final Long SELLER_ID = 1L;
    private static final Long ITEM_ID = 10L;
    private static final Long STORAGE_ID = 20L;
    private static final Long ITEMS_AT_STORAGE_ID = 30L;

    private Seller seller;
    private Item item;
    private Storage storage;
    private ItemsAtStorageDto dto;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);

        seller = new Seller();
        seller.setId(SELLER_ID);
        seller.setName("seller test");
        seller.setNumber(SELLER_NUMBER);

        item = new Item();
        item.setId(ITEM_ID);
        item.setName("Test Item");
        item.setSeller(seller);

        storage = new Storage();
        storage.setId(STORAGE_ID);

        dto = ItemsAtStorageDto.builder()
                .itemId(ITEM_ID)
                .storageId(STORAGE_ID)
                .count(5L)
                .build();
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void add_Success() {
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        when(itemService.getBySellerId(SELLER_ID)).thenReturn(List.of(item));
        when(storageService.getAll()).thenReturn(List.of(storage));
        when(itemService.getById(ITEM_ID)).thenReturn(item);
        doNothing().when(itemsAtStorageService).add(any(ItemsAtStorageDto.class));

        given()
                .param("itemId", ITEM_ID)
                .param("storageId", STORAGE_ID)
                .param("count", dto.getCount())
                .when()
                .post("/seller/send-to-storage")
                .then()
                .statusCode(200);

        verify(itemsAtStorageService).add(any(ItemsAtStorageDto.class));
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void add_OtherSeller_Success() {
        Seller otherSeller = new Seller();
        otherSeller.setId(3L);
        Item otherItem = new Item();
        otherItem.setSeller(otherSeller);
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        when(itemService.getBySellerId(SELLER_ID)).thenReturn(List.of());
        when(storageService.getAll()).thenReturn(List.of(storage));
        when(itemService.getById(ITEM_ID)).thenReturn(otherItem);

        given()
                .param("itemId", ITEM_ID)
                .param("storageId", STORAGE_ID)
                .param("count", dto.getCount())
                .when()
                .post("/seller/send-to-storage")
                .then()
                .statusCode(302)
                .header("Location", "/welcome");

        verify(itemsAtStorageService, never()).add(any(ItemsAtStorageDto.class));
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void add_ValidationFailure_NullItemId() {
        given()
                .param("storageId", STORAGE_ID)
                .param("count", dto.getCount())
                .when()
                .post("/seller/send-to-storage")
                .then()
                .statusCode(500);

        verify(itemsAtStorageService, never()).add(any());
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void add_ValidationFailure_NullStorageId() {
        given()
                .param("itemId", ITEM_ID)
                .param("count", dto.getCount())
                .when()
                .post("/seller/send-to-storage")
                .then()
                .statusCode(500);

        verify(itemsAtStorageService, never()).add(any());
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void add_ValidationFailure_NegativeCount() {
        given()
                .param("itemId", ITEM_ID)
                .param("storageId", STORAGE_ID)
                .param("count", "-1")
                .when()
                .post("/seller/send-to-storage")
                .then()
                .statusCode(500);

        verify(itemsAtStorageService, never()).add(any());
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void add_SellerNotFound_ThrowsException() {
        when(sellerService.getByNumber(SELLER_NUMBER))
                .thenThrow(new ResourceNotFoundException(Seller.class, SELLER_NUMBER));

        given()
                .param("itemId", ITEM_ID)
                .param("storageId", STORAGE_ID)
                .param("count", dto.getCount())
                .when()
                .post("/seller/send-to-storage")
                .then()
                .statusCode(404);

        verify(itemsAtStorageService, never()).add(any());
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void add_ItemNotFoundInGet_ThrowsException() {
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        when(itemService.getBySellerId(SELLER_ID)).thenReturn(List.of(item));
        when(storageService.getAll()).thenReturn(List.of(storage));
        when(itemService.getById(ITEM_ID)).thenThrow(new ResourceNotFoundException(Item.class, ITEM_ID));

        given()
                .param("itemId", ITEM_ID)
                .param("storageId", STORAGE_ID)
                .param("count", dto.getCount())
                .when()
                .post("/seller/send-to-storage")
                .then()
                .statusCode(404);

        verify(itemsAtStorageService, never()).add(any(ItemsAtStorageDto.class));
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void add_ItemNotFoundInAdd_ThrowsException() {
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        when(itemService.getBySellerId(SELLER_ID)).thenReturn(List.of(item));
        when(storageService.getAll()).thenReturn(List.of(storage));
        when(itemService.getById(ITEM_ID)).thenReturn(item);
        doThrow(new ResourceNotFoundException(Item.class, ITEM_ID))
                .when(itemsAtStorageService).add(any(ItemsAtStorageDto.class));

        given()
                .param("itemId", ITEM_ID)
                .param("storageId", STORAGE_ID)
                .param("count", dto.getCount())
                .when()
                .post("/seller/send-to-storage")
                .then()
                .statusCode(200);

        verify(itemsAtStorageService).add(any(ItemsAtStorageDto.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void add_UserRole_ShouldReturnForbidden() {
        given()
                .when()
                .post("/seller/send-to-storage")
                .then()
                .statusCode(403);

        verify(itemsAtStorageService, never()).add(any());
    }

    @Test
    void add_Unauthenticated_RedirectToLogin() {
        given()
                .when()
                .post("/seller/send-to-storage")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
        verify(itemsAtStorageService, never()).add(any());
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void getFormItemsAtStorage_Success() {
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        when(itemService.getBySellerId(SELLER_ID)).thenReturn(List.of(item));
        when(storageService.getAll()).thenReturn(List.of(storage));

        given()
                .when()
                .get("/seller/send-to-storage")
                .then()
                .statusCode(200);

        verify(sellerService).getByNumber(SELLER_NUMBER);
        verify(itemService).getBySellerId(SELLER_ID);
        verify(storageService).getAll();
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void getFormItemsAtStorage_SellerNotFound_ThrowsException() {
        when(sellerService.getByNumber(SELLER_NUMBER))
                .thenThrow(new ResourceNotFoundException(Seller.class, SELLER_NUMBER));

        given()
                .when()
                .get("/seller/send-to-storage")
                .then()
                .statusCode(404);

        verify(itemService, never()).getBySellerId(any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getFormItemsAtStorage_UserRole_ShouldReturnForbidden() {
        given()
                .when()
                .get("/seller/send-to-storage")
                .then()
                .statusCode(403);

        verify(sellerService, never()).getByNumber(any());
    }

    @Test
    void getFormItemsAtStorage_Unauthenticated_RedirectToLogin() {
        given()
                .when()
                .get("/seller/send-to-storage")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
        verify(sellerService, never()).getByNumber(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAll_Success() {
        when(itemsAtStorageService.getAll()).thenReturn(List.of());

        given()
                .when()
                .get("/admin/item-at-storage")
                .then()
                .statusCode(200);

        verify(itemsAtStorageService).getAll();
    }

    @Test
    @WithMockUser(roles = "SELLER")
    void getAll_SellerRole_ShouldReturnForbidden() {
        given()
                .when()
                .get("/admin/item-at-storage")
                .then()
                .statusCode(403);

        verify(itemsAtStorageService, never()).getAll();
    }

    @Test
    void getAll_Unauthenticated_RedirectToLogin() {
        given()
                .when()
                .get("/admin/item-at-storage")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
        verify(itemsAtStorageService, never()).getAll();
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void updateForm_OwnerSeller_Success() {
        when(itemService.getById(ITEM_ID)).thenReturn(item);
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        doNothing().when(itemsAtStorageService).update(ITEMS_AT_STORAGE_ID, ITEM_ID);

        given()
                .param("itemId", ITEM_ID)
                .param("count", 10)
                .when()
                .put("/send-to-storage/{id}", ITEMS_AT_STORAGE_ID)
                .then()
                .statusCode(302)
                .header("Location", "/items/" + ITEM_ID);

        verify(itemsAtStorageService).update(ITEMS_AT_STORAGE_ID, ITEM_ID);
    }

    @Test
    @WithMockUser(username = "89137650999", roles = "SELLER")
    void updateForm_OtherSeller_RedirectToWelcome() {
        Seller otherSeller = new Seller();
        otherSeller.setId(99L);
        otherSeller.setName("other");

        when(itemService.getById(ITEM_ID)).thenReturn(item);
        when(sellerService.getByNumber("89137650999")).thenReturn(otherSeller);

        given()
                .param("itemId", ITEM_ID)
                .param("count", 10)
                .when()
                .put("/send-to-storage/{id}", ITEMS_AT_STORAGE_ID)
                .then()
                .statusCode(302)
                .header("Location", "/welcome");

        verify(itemsAtStorageService, never()).update(anyLong(), anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateForm_Admin_Success() {
        when(itemService.getById(ITEM_ID)).thenReturn(item);
        doNothing().when(itemsAtStorageService).update(ITEMS_AT_STORAGE_ID, 5L);

        given()
                .param("itemId", ITEM_ID)
                .param("count", 5)
                .when()
                .put("/send-to-storage/{id}", ITEMS_AT_STORAGE_ID)
                .then()
                .statusCode(302)
                .header("Location", "/items/" + ITEM_ID);

        verify(itemsAtStorageService).update(ITEMS_AT_STORAGE_ID, 5L);
        verify(sellerService, never()).getByNumber(any());
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void updateForm_ItemNotFound_ThrowsException() {
        when(itemService.getById(ITEM_ID))
                .thenThrow(new ResourceNotFoundException(Item.class, ITEM_ID));

        given()
                .param("itemId", ITEM_ID)
                .param("count", 5)
                .when()
                .put("/send-to-storage/{id}", ITEMS_AT_STORAGE_ID)
                .then()
                .statusCode(404);

        verify(itemsAtStorageService, never()).update(anyLong(), anyLong());
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void updateForm_SellerNotFound_ThrowsException() {
        when(itemService.getById(ITEM_ID)).thenReturn(item);
        when(sellerService.getByNumber(SELLER_NUMBER))
                .thenThrow(new ResourceNotFoundException(Seller.class, SELLER_ID));

        given()
                .param("itemId", ITEM_ID)
                .param("count", 5)
                .when()
                .put("/send-to-storage/{id}", ITEMS_AT_STORAGE_ID)
                .then()
                .statusCode(404);

        verify(itemsAtStorageService, never()).update(anyLong(), anyLong());
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void updateForm_ItemsAtStorageNotFound_ThrowsException() {
        when(itemService.getById(ITEM_ID)).thenReturn(item);
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        doThrow(new ResourceNotFoundException(ItemsAtStorage.class, ITEMS_AT_STORAGE_ID))
                .when(itemsAtStorageService).update(ITEMS_AT_STORAGE_ID, 5L);

        given()
                .param("itemId", ITEM_ID)
                .param("count", 5)
                .when()
                .put("/send-to-storage/{id}", ITEMS_AT_STORAGE_ID)
                .then()
                .statusCode(302)
                .header("Location", "/items/" + ITEM_ID);

        verify(itemsAtStorageService).update(ITEMS_AT_STORAGE_ID, 5L);
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void updateForm_NullItemId_ThrowsException() {
        given()
                .param("count", 5)
                .when()
                .put("/send-to-storage/{id}", ITEMS_AT_STORAGE_ID)
                .then()
                .statusCode(500);

        verify(itemsAtStorageService, never()).update(anyLong(), anyLong());
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void updateForm_NegativeCount_ThrowsException() {
        given()
                .param("count", -5)
                .when()
                .put("/send-to-storage/{id}", ITEMS_AT_STORAGE_ID)
                .then()
                .statusCode(500);

        verify(itemsAtStorageService, never()).update(anyLong(), anyLong());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateForm_UserRole_ShouldReturnForbidden() {
        given()
                .param("itemId", ITEM_ID)
                .param("count", 5)
                .when()
                .put("/send-to-storage/{id}", ITEMS_AT_STORAGE_ID)
                .then()
                .statusCode(403);

        verify(itemsAtStorageService, never()).update(any(), any());
    }

    @Test
    void updateForm_Unauthenticated_RedirectToLogin() {
        given()
                .param("itemId", ITEM_ID.toString())
                .param("count", 5)
                .when()
                .put("/send-to-storage/{id}", ITEMS_AT_STORAGE_ID)
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }
}