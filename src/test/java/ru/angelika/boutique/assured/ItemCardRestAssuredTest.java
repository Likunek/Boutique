package ru.angelika.boutique.assured;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.angelika.boutique.dto.FeedbackDto;
import ru.angelika.boutique.dto.ItemCardDto;
import ru.angelika.boutique.dto.ItemCardUpdateDto;
import ru.angelika.boutique.exception.ResourceExistsException;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.Item;
import ru.angelika.boutique.model.ItemCard;
import ru.angelika.boutique.model.Seller;
import ru.angelika.boutique.model.User;
import ru.angelika.boutique.service.ItemCardService;
import ru.angelika.boutique.service.ItemService;
import ru.angelika.boutique.service.SellerService;
import ru.angelika.boutique.service.UserService;

import java.util.List;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ItemCardRestAssuredTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private SellerService sellerService;

    @MockitoBean
    private ItemCardService itemCardService;

    private static final String SELLER_NUMBER = "89137650976";
    private static final String USER_NUMBER = "89137650977";
    private static final Long SELLER_ID = 1L;
    private static final Long ITEM_ID = 10L;
    private static final Long CARD_ID = 8L;
    private static final Long USER_ID = 2L;

    private Seller seller;
    private Item item;
    private ItemCard itemCard;
    private ItemCardDto itemCardDto;
    private ItemCardUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);

        seller = new Seller();
        seller.setId(SELLER_ID);
        seller.setName("seller_test");
        seller.setNumber(SELLER_NUMBER);

        item = new Item();
        item.setId(ITEM_ID);
        item.setName("Test Item");

        itemCard = new ItemCard();
        itemCard.setId(CARD_ID);
        itemCard.setName("Test Card");
        itemCard.setSeller(seller.getName());

        itemCardDto = ItemCardDto.builder()
                .itemId(ITEM_ID)
                .name("New Card")
                .description("Description")
                .build();

        updateDto = ItemCardUpdateDto.builder()
                .name("Updated Card")
                .description("Updated Description")
                .build();
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void getFormNewCard_Success() {
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        when(itemService.getBySellerIdItemCardNull(SELLER_ID)).thenReturn(List.of(item));

        given()
                .when()
                .get("/seller/add-card")
                .then()
                .statusCode(200);
        verify(itemService).getBySellerIdItemCardNull(SELLER_ID);
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void getFormNewCard_SellerNotFound_ThrowsException() {
        when(sellerService.getByNumber(SELLER_NUMBER)).thenThrow(new ResourceNotFoundException(Seller.class, SELLER_NUMBER));
        given()
                .when()
                .get("/seller/add-card")
                .then()
                .statusCode(404);
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void getFormNewCard_UserRole_ShouldReturnForbidden() {
        given()
                .when()
                .get("/seller/add-card")
                .then()
                .statusCode(403);
        verify(itemService, never()).getBySellerIdItemCardNull(any());
    }

    @Test
    void getFormNewCard_Unauthenticated_RedirectToLogin() {
        given()
                .when()
                .get("/seller/add-card")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void addCard_Success() {
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        doNothing().when(itemCardService).add(any(ItemCardDto.class), eq(seller.getName()));
        when(itemService.getBySellerIdItemCardNull(SELLER_ID)).thenReturn(List.of(item));

        given()
                .param("itemId", itemCardDto.getItemId())
                .param("name", itemCardDto.getName())
                .param("description", itemCardDto.getDescription())
                .when()
                .post("/seller/add-card")
                .then()
                .statusCode(200);
        verify(itemCardService).add(any(ItemCardDto.class), eq(seller.getName()));
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void addCard_ValidationFailure_EmptyName() {
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        given()
                .param("itemId", itemCardDto.getItemId())
                .param("name", "")
                .param("description", itemCardDto.getDescription())
                .when()
                .post("/seller/add-card")
                .then()
                .statusCode(500);
        verify(itemService, never()).getBySellerIdItemCardNull(SELLER_ID);
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void addCard_ValidationFailure_NullItemId() {
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        given()
                .param("name", itemCardDto.getName())
                .param("description", itemCardDto.getDescription())
                .when()
                .post("/seller/add-card")
                .then()
                .statusCode(500);
        verify(itemService, never()).getBySellerIdItemCardNull(SELLER_ID);
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void addCard_SellerNotFound_ThrowsException() {
        when(sellerService.getByNumber(SELLER_NUMBER)).thenThrow(new ResourceNotFoundException(Seller.class, SELLER_NUMBER));

        given()
                .param("itemId", itemCardDto.getItemId())
                .param("name", itemCardDto.getName())
                .param("description", itemCardDto.getDescription())
                .when()
                .post("/seller/add-card")
                .then()
                .statusCode(404);
        verify(itemService, never()).getBySellerIdItemCardNull(SELLER_ID);
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void addCard_ItemCardExists_ThrowsException() {
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        doThrow(new ResourceExistsException(ItemCard.class, ""))
                .when(itemCardService).add(any(ItemCardDto.class), eq(seller.getName()));

        given()
                .param("itemId", itemCardDto.getItemId())
                .param("name", itemCardDto.getName())
                .param("description", itemCardDto.getDescription())
                .when()
                .post("/seller/add-card")
                .then()
                .statusCode(409);
        verify(itemCardService).add(any(ItemCardDto.class), eq(seller.getName()));
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void updateCard_Success() {
        when(itemCardService.get(CARD_ID)).thenReturn(itemCard);
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        doNothing().when(itemCardService).update(any(ItemCardUpdateDto.class), eq(CARD_ID), eq(seller.getName()));

        given()
                .param("itemId", ITEM_ID)
                .param("name", updateDto.getName())
                .param("description", updateDto.getDescription())
                .when()
                .put("/cards/{id}", CARD_ID)
                .then()
                .statusCode(302)
                .header("Location", "/items/" + ITEM_ID);
        verify(itemCardService).update(any(ItemCardUpdateDto.class), eq(CARD_ID), eq(seller.getName()));
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void updateCard_OtherSeller_RedirectToWelcome() {
        Seller otherSeller = new Seller();
        otherSeller.setName("other");
        when(itemCardService.get(CARD_ID)).thenReturn(itemCard);
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(otherSeller);
        given()
                .param("itemId", ITEM_ID)
                .param("name", updateDto.getName())
                .when()
                .put("/cards/{id}", CARD_ID)
                .then()
                .statusCode(302)
                .header("Location", "/welcome");
        verify(itemCardService, never()).update(any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCard_Admin_Success() {
        when(itemCardService.get(CARD_ID)).thenReturn(itemCard);
        doNothing().when(itemCardService).update(any(ItemCardUpdateDto.class), eq(CARD_ID), eq(itemCard.getSeller()));

        given()
                .param("itemId", ITEM_ID)
                .param("name", updateDto.getName())
                .when()
                .put("/cards/{id}", CARD_ID)
                .then()
                .statusCode(302)
                .header("Location", "/items/" + ITEM_ID);
        verify(itemCardService).update(any(ItemCardUpdateDto.class), eq(CARD_ID), eq(itemCard.getSeller()));
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void updateCard_UserRole_ShouldReturnForbidden() {
        given()
                .param("itemId", ITEM_ID)
                .param("name", updateDto.getName())
                .when()
                .put("/cards/{id}", CARD_ID)
                .then()
                .statusCode(403);
        verify(itemCardService, never()).update(any(), any(), any());
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void updateCard_ItemCardNotFound_ThrowsException() {
        when(itemCardService.get(CARD_ID)).thenThrow(new ResourceNotFoundException(ItemCard.class, CARD_ID));

        given()
                .param("itemId", ITEM_ID)
                .param("name", updateDto.getName())
                .param("description", updateDto.getDescription())
                .when()
                .put("/cards/{id}", CARD_ID)
                .then()
                .statusCode(404);
        verify(itemCardService, never()).update(any(), any(), any());
    }
    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void updateCard_SellerNotFound_ThrowsException() {
        when(itemCardService.get(CARD_ID)).thenReturn(itemCard);
        when(sellerService.getByNumber(SELLER_NUMBER)).thenThrow(new ResourceNotFoundException(Seller.class, SELLER_NUMBER));

        given()
                .param("itemId", ITEM_ID)
                .param("name", updateDto.getName())
                .param("description", updateDto.getDescription())
                .when()
                .put("/cards/{id}", CARD_ID)
                .then()
                .statusCode(404);
        verify(itemCardService, never()).update(any(), any(), any());
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void updateCard_ItemCardExists_ThrowsException() {
        when(itemCardService.get(CARD_ID)).thenReturn(itemCard);
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        doThrow(new ResourceExistsException(ItemCard.class, ""))
                .when(itemCardService).update(any(ItemCardUpdateDto.class), eq(CARD_ID), eq(seller.getName()));

        given()
                .param("itemId", ITEM_ID)
                .param("name", updateDto.getName())
                .param("description", updateDto.getDescription())
                .when()
                .put("/cards/{id}", CARD_ID)
                .then()
                .statusCode(302)
                .header("Location", "/items/" + ITEM_ID);
        verify(itemCardService).update(any(), any(), any());
    }
    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void addFeedback_Success() {
        doNothing().when(itemCardService).addFeedback(any(FeedbackDto.class), eq(CARD_ID));

        given()
                .param("userId", USER_ID)
                .param("rating", 5)
                .param("text", "Great")
                .when()
                .post("/user/item-card/feedback/{id}", CARD_ID)
                .then()
                .statusCode(302)
                .header("Location", "/user/profile/" + USER_ID);
        verify(itemCardService).addFeedback(any(FeedbackDto.class), eq(CARD_ID));
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void addFeedback_ValidationFailure_RatingNull() {
        given()
                .param("userId", USER_ID)
                .param("text", "Great")
                .when()
                .post("/user/item-card/feedback/{id}", CARD_ID)
                .then()
                .statusCode(500);
        verify(itemCardService, never()).addFeedback(any(), any());
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void addFeedback_ValidationFailure_RatingBig() {
        given()
                .param("userId", USER_ID)
                .param("rating", 6)
                .param("text", "Great")
                .when()
                .post("/user/item-card/feedback/{id}", CARD_ID)
                .then()
                .statusCode(500);
        verify(itemCardService, never()).addFeedback(any(), any());
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void addFeedback_ValidationFailure_UserIdNull() {
        given()
                .param("rating", 4)
                .param("text", "Great")
                .when()
                .post("/user/item-card/feedback/{id}", CARD_ID)
                .then()
                .statusCode(500);
        verify(itemCardService, never()).addFeedback(any(), any());
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void addFeedback_UserNotFound_ThrowsException() {
        doThrow(new ResourceNotFoundException(User.class, USER_ID))
                .when(itemCardService).addFeedback(any(FeedbackDto.class), eq(CARD_ID));

        given()
                .param("userId", USER_ID)
                .param("rating", 5)
                .param("text", "Great")
                .when()
                .post("/user/item-card/feedback/{id}", CARD_ID)
                .then()
                .statusCode(404);
        verify(itemCardService).addFeedback(any(FeedbackDto.class), eq(CARD_ID));
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void getAllCards_User_Success() {
        when(userService.getCardsId(USER_NUMBER)).thenReturn(List.of(1L, 2L));
        when(itemCardService.getAll(any(Pageable.class))).thenReturn(Page.empty());

        given()
                .queryParam("role", "ROLE_USER")
                .queryParam("page", "0")
                .queryParam("size", "12")
                .when()
                .get("/item-card")
                .then()
                .statusCode(200);
        verify(userService).getCardsId(USER_NUMBER);
        verify(itemCardService, never()).getAllBySearch(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCards_Admin_Success() {
        when(itemCardService.getAll(any(Pageable.class))).thenReturn(Page.empty());

        given()
                .queryParam("role", "ROLE_ADMIN")
                .queryParam("page", "0")
                .queryParam("size", "12")
                .when()
                .get("/item-card")
                .then()
                .statusCode(200);
        verify(userService, never()).getCardsId(any());
        verify(sellerService, never()).getById(any());
        verify(itemCardService, never()).getAllBySearch(any(), any());
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void getAllCards_SellerPage_Success() {
        when(userService.getCardsId(USER_NUMBER)).thenReturn(List.of());
        when(sellerService.getById(SELLER_ID)).thenReturn(seller);
        when(itemCardService.getAllBySeller(any(Pageable.class), eq(seller.getName()))).thenReturn(Page.empty());
        when(itemCardService.getAll(any(Pageable.class))).thenReturn(Page.empty());

        given()
                .queryParam("role", "ROLE_USER")
                .queryParam("page", "0")
                .queryParam("size", "12")
                .queryParam("seller", true)
                .queryParam("sellerId", SELLER_ID)
                .when()
                .get("/item-card")
                .then()
                .statusCode(200);

        verify(userService).getCardsId(USER_NUMBER);
        verify(sellerService).getById(SELLER_ID);
        verify(itemCardService).getAllBySeller(any(), any());
        verify(itemCardService, never()).getAllBySearch(any(), any());
    }

    @Test
    void getAllCards_Unauthenticated_Success() {
        given()
                .queryParam("role", "ROLE_USER")
                .when()
                .get("/item-card")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllCards_WithSearch_Success() {
        when(itemCardService.getAllBySearch(any(Pageable.class), eq("test"))).thenReturn(Page.empty());
        given()
                .queryParam("role", "ROLE_USER")
                .queryParam("search", "test")
                .when()
                .get("/item-card")
                .then()
                .statusCode(200);
        verify(sellerService, never()).getById(SELLER_ID);
        verify(itemCardService, never()).getAllBySeller(any(), any());
        verify(itemCardService).getAllBySearch(any(), eq("test"));
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void getCardById_OwnerSeller_Success() {
        when(itemCardService.get(CARD_ID)).thenReturn(itemCard);
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        when(sellerService.getByName(itemCard.getSeller())).thenReturn(seller);

        given()
                .queryParam("role", "ROLE_SELLER")
                .when()
                .get("/item-card/{id}", CARD_ID)
                .then()
                .statusCode(200);
        verify(itemCardService).get(CARD_ID);
        verify(sellerService).getByNumber(SELLER_NUMBER);
        verify(sellerService).getByName(itemCard.getSeller());
        verify(userService, never()).getCardsId(any());
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void getCardById_OtherSeller_NotOwner() {
        Seller otherSeller = new Seller();
        otherSeller.setName("other");
        when(itemCardService.get(CARD_ID)).thenReturn(itemCard);
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(otherSeller);
        when(sellerService.getByName(itemCard.getSeller())).thenReturn(seller);

        given()
                .queryParam("role", "ROLE_SELLER")
                .when()
                .get("/item-card/{id}", CARD_ID)
                .then()
                .statusCode(200);

        verify(itemCardService).get(CARD_ID);
        verify(sellerService).getByNumber(SELLER_NUMBER);
        verify(sellerService).getByName(itemCard.getSeller());
        verify(userService, never()).getCardsId(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCardById_Admin_Success() {
        when(itemCardService.get(CARD_ID)).thenReturn(itemCard);
        when(sellerService.getByName(itemCard.getSeller())).thenReturn(seller);
        given()
                .queryParam("role", "ROLE_ADMIN")
                .when()
                .get("/item-card/{id}", CARD_ID)
                .then()
                .statusCode(200);
        verify(itemCardService).get(CARD_ID);
        verify(sellerService).getByName(itemCard.getSeller());
        verify(sellerService, never()).getByNumber(any());
        verify(userService, never()).getCardsId(any());
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void getCardById_User_Success() {
        when(itemCardService.get(CARD_ID)).thenReturn(itemCard);
        when(userService.getCardsId(USER_NUMBER)).thenReturn(List.of());
        when(sellerService.getByName(itemCard.getSeller())).thenReturn(seller);
        given()
                .queryParam("role", "ROLE_USER")
                .when()
                .get("/item-card/{id}", CARD_ID)
                .then()
                .statusCode(200);
        verify(itemCardService).get(CARD_ID);
        verify(sellerService).getByName(itemCard.getSeller());
        verify(sellerService, never()).getByNumber(any());
        verify(userService).getCardsId(USER_NUMBER);
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void getCardById_OwnerSellerNotFound_ThrowException() {
        when(itemCardService.get(CARD_ID)).thenReturn(itemCard);
        when(userService.getCardsId(USER_NUMBER)).thenReturn(List.of());
        when(sellerService.getByName(itemCard.getSeller()))
                .thenThrow(new ResourceNotFoundException(Seller.class, seller.getName()));
        given()
                .queryParam("role", "ROLE_USER")
                .when()
                .get("/item-card/{id}", CARD_ID)
                .then()
                .statusCode(404);
        verify(itemCardService).get(CARD_ID);
        verify(userService).getCardsId(USER_NUMBER);
        verify(sellerService).getByName(itemCard.getSeller());
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void getCardById_UserNotFound_ThrowException() {
        when(itemCardService.get(CARD_ID)).thenReturn(itemCard);
        when(userService.getCardsId(USER_NUMBER))
                .thenThrow(new ResourceNotFoundException(User.class, USER_NUMBER));
        given()
                .queryParam("role", "ROLE_USER")
                .when()
                .get("/item-card/{id}", CARD_ID)
                .then()
                .statusCode(404);
        verify(itemCardService).get(CARD_ID);
        verify(userService).getCardsId(USER_NUMBER);
        verify(sellerService, never()).getByName(any());
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void getCardById_ItemCardNotFound_ThrowException() {
        when(itemCardService.get(CARD_ID))
                .thenThrow(new ResourceNotFoundException(ItemCard.class, CARD_ID));
        given()
                .queryParam("role", "ROLE_USER")
                .when()
                .get("/item-card/{id}", CARD_ID)
                .then()
                .statusCode(404);
        verify(itemCardService).get(CARD_ID);
        verify(userService, never()).getCardsId(any());
        verify(sellerService, never()).getByName(any());
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void getCardById_SellerNotFoundByNumber_ThrowException() {
        when(itemCardService.get(CARD_ID)).thenReturn(itemCard);
        when(sellerService.getByNumber(SELLER_NUMBER))
                .thenThrow(new ResourceNotFoundException(Seller.class, SELLER_NUMBER));

        given()
                .queryParam("role", "ROLE_SELLER")
                .when()
                .get("/item-card/{id}", CARD_ID)
                .then()
                .statusCode(404);
        verify(itemCardService).get(CARD_ID);
        verify(sellerService).getByNumber(SELLER_NUMBER);
        verify(sellerService, never()).getByName(any());
    }

    @Test
    void getCardById_Unauthenticated_Success() {
        when(itemCardService.get(CARD_ID)).thenReturn(itemCard);
        when(sellerService.getByName(itemCard.getSeller())).thenReturn(seller);
        given()
                .queryParam("role", "ROLE_USER")
                .when()
                .get("/item-card/{id}", CARD_ID)
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard_Admin_Success() {
        when(itemCardService.get(CARD_ID)).thenReturn(itemCard);
        doNothing().when(itemCardService).delete(CARD_ID);
        given()
                .queryParam("role", "ROLE_ADMIN")
                .when()
                .delete("/cards/{id}", CARD_ID)
                .then()
                .statusCode(302)
                .header("Location", "/welcome");
        verify(itemCardService).delete(CARD_ID);
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void deleteCard_OwnerSeller_Success() {
        when(itemCardService.get(CARD_ID)).thenReturn(itemCard);
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(seller);
        doNothing().when(itemCardService).delete(CARD_ID);
        given()
                .queryParam("role", "ROLE_SELLER")
                .when()
                .delete("/cards/{id}", CARD_ID)
                .then()
                .statusCode(302)
                .header("Location", "/welcome");
        verify(itemCardService).delete(CARD_ID);
    }

    @Test
    @WithMockUser(username = SELLER_NUMBER, roles = "SELLER")
    void deleteCard_OtherSeller_NoDelete() {
        Seller otherSeller = new Seller();
        otherSeller.setName("other");
        when(itemCardService.get(CARD_ID)).thenReturn(itemCard);
        when(sellerService.getByNumber(SELLER_NUMBER)).thenReturn(otherSeller);
        given()
                .queryParam("role", "ROLE_SELLER")
                .when()
                .delete("/cards/{id}", CARD_ID)
                .then()
                .statusCode(302)
                .header("Location", "/welcome");
        verify(itemCardService, never()).delete(any());
    }

    @Test
    @WithMockUser(username = USER_NUMBER, roles = "USER")
    void deleteCard_User_ShouldReturnForbidden() {
        given()
                .queryParam("role", "ROLE_USER")
                .when()
                .delete("/cards/{id}", CARD_ID)
                .then()
                .statusCode(403);
        verify(itemCardService, never()).delete(any());
    }
}