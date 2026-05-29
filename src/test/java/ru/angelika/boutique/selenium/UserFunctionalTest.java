package ru.angelika.boutique.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserFunctionalTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:8080";
    private static final String USER_NUMBER = "89137650920";
    private static final String USER_PASSWORD = "9999";

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setUp() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    void testLogin() {
        driver.get(BASE_URL + "/login");
        driver.findElement(By.id("number")).sendKeys(USER_NUMBER);
        driver.findElement(By.id("password")).sendKeys(USER_PASSWORD);
        driver.findElement(By.className("btn-login")).click();
        wait.until(ExpectedConditions.urlContains("/welcome"));
        WebElement welcome = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("welcome-title")));
        assertEquals("Welcome to Boutique", welcome.getText());
    }

    @Test
    void testRegistrationPageAccess() {
        driver.get(BASE_URL + "/login");

        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Create one here"))).click();
        wait.until(ExpectedConditions.urlContains("/registration"));
        WebElement tagName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertEquals("📝 Create Account", tagName.getText());

        driver.findElement(By.linkText("Sign in here")).click();
        wait.until(ExpectedConditions.urlContains("/login"));
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertEquals("🔐 Welcome Back", header.getText());
    }

    @Test
    void testViewProfile() {
        login();
        driver.findElement(By.linkText("My account")).click();
        wait.until(ExpectedConditions.urlContains("/user/profile/2"));
        assertTrue(driver.findElement(By.className("profile-header")).isDisplayed());
        assertTrue(driver.findElement(By.className("balance-amount")).isDisplayed());
    }

    @Test
    void testLogout() {
        login();
        driver.findElement(By.className("btn-logout")).click();
        wait.until(ExpectedConditions.urlContains("/login"));
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertEquals("🔐 Welcome Back", header.getText());
    }

    @Test
    void testEditProfile() {
        login();
        driver.findElement(By.linkText("My account")).click();
        wait.until(ExpectedConditions.urlContains("/user/profile/2"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("profile-header")));
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#profileViewMode .btn-primary"))).click();

        WebElement nameField = driver.findElement(By.name("name"));
        nameField.clear();
        nameField.sendKeys("Bob Edited");
        driver.findElement(By.name("oldPassword")).sendKeys(USER_PASSWORD);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#profileEditMode .btn-primary"))).click();

        login();
        driver.findElement(By.linkText("My account")).click();
        wait.until(ExpectedConditions.urlContains("/user/profile/2"));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("profile-header")));
        assertEquals("Bob Edited", driver.findElement(By.cssSelector(".profile-header h1")).getText());
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#profileViewMode .btn-primary"))).click();

        WebElement nameOld = driver.findElement(By.name("name"));
        nameOld.clear();
        nameOld.sendKeys("maria");
        driver.findElement(By.name("oldPassword")).sendKeys(USER_PASSWORD);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#profileEditMode .btn-primary"))).click();

        login();
        driver.findElement(By.linkText("My account")).click();
        wait.until(ExpectedConditions.urlContains("/user/profile/2"));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("profile-header")));
        assertEquals("maria", driver.findElement(By.cssSelector(".profile-header h1")).getText());
    }

    @Test
    void testOpenCart() {
        login();
        clearCart();
        driver.findElement(By.linkText("My account")).click();
        wait.until(ExpectedConditions.urlContains("/profile/"));
        ;
        WebElement myCartLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.btn-primary")));
        myCartLink.click();

        assertTrue(driver.findElement(By.className("empty-cart")).getText().contains("empty"));
    }

    @Test
    void testViewItemsAsUser() {
        login();
        driver.findElement(By.className("view-items-btn")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("cards-grid")));
        assertTrue(driver.findElement(By.className("card")).isDisplayed());
    }

    @Test
    void testSearchItem() {
        login();
        driver.findElement(By.className("view-items-btn")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("search-input")));
        driver.findElement(By.className("search-input")).sendKeys("gold");
        driver.findElement(By.className("search-btn")).click();
        WebElement searchInfo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("search-info")));
        assertTrue(searchInfo.getText().contains("gold"));
        driver.findElement(By.linkText("✖ Clear")).click();
        wait.until(ExpectedConditions.invisibilityOf(searchInfo));
    }

    @Test
    void testSortItemsByPriceAsc() {
        login();
        driver.findElement(By.className("view-items-btn")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Price ↑")));
        driver.findElement(By.linkText("Price ↑")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("cards-grid")));
        assertTrue(driver.getCurrentUrl().contains("sortBy=price&sort=asc"));
    }

    @Test
    void testSortItemsByPriceDesc() {
        login();
        driver.findElement(By.className("view-items-btn")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Price ↓")));
        driver.findElement(By.linkText("Price ↓")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("cards-grid")));
        assertTrue(driver.getCurrentUrl().contains("sortBy=price&sort=desc"));
    }

    @Test
    void testSortItemsByRatingDesc() {
        login();
        driver.findElement(By.className("view-items-btn")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Rating ↓")));
        driver.findElement(By.linkText("Rating ↓")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("cards-grid")));
        assertTrue(driver.getCurrentUrl().contains("sortBy=rating&sort=desc"));
    }

    @Test
    void testSortItemsByRatingAsc() {
        login();
        driver.findElement(By.className("view-items-btn")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Rating ↑")));
        driver.findElement(By.linkText("Rating ↑")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("cards-grid")));
        assertTrue(driver.getCurrentUrl().contains("sortBy=rating&sort=asc"));
    }

    @Test
    void testViewSellerPublicProfile() {
        login();
        driver.findElement(By.className("view-items-btn")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("card-seller")));
        driver.findElement(By.className("card")).click();
        wait.until(ExpectedConditions.urlContains("/item-card/"));
        WebElement sellerLink = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("seller-link")));
        sellerLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card-header h1")));
        assertTrue(driver.getCurrentUrl().contains("public-seller"));
        driver.findElement(By.linkText("\uD83D\uDECD️ View items")).click();
        wait.until(ExpectedConditions.urlContains("/item-card?sellerId="));
        assertTrue(driver.findElement(By.className("cards-grid")).isDisplayed());
    }

    @Test
    void testPagination() {
        login();
        driver.get(BASE_URL + "/item-card?role=ROLE_USER&size=2");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("pagination")));
        WebElement next = driver.findElement(By.linkText("→"));
        if (!next.getAttribute("class").contains("disabled")) {
            next.click();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("cards-grid")));
            assertTrue(driver.getCurrentUrl().contains("page=1"));
        } else {
            assertTrue(driver.findElement(By.className("pagination")).isDisplayed());
        }
    }

    @Test
    void testAddToCartFromList() {
        login();
        clearCart();
        driver.findElement(By.className("view-items-btn")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("cards-grid")));

        WebElement firstCard = driver.findElement(By.className("card"));
        String itemName = firstCard.findElement(By.className("card-title")).getText();

        WebElement addBtn = firstCard.findElement(By.className("cart-btn"));
        addBtn.click();

        driver.findElement(By.linkText("← Back to Home")).click();
        wait.until(ExpectedConditions.urlContains("/welcome"));
        driver.findElement(By.linkText("My account")).click();
        wait.until(ExpectedConditions.urlContains("/user/profile/"));

        WebElement myCartLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.btn-primary")));
        myCartLink.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("cart-item")));

        List<WebElement> cartItems = driver.findElements(By.className("cart-item"));
        boolean found = false;
        for (WebElement cartItem : cartItems) {
            String name = cartItem.findElement(By.className("item-name")).getText();
            if (name.contains(itemName)) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    void testAddToCartFromDetail() {
        login();
        clearCart();
        driver.findElement(By.className("view-items-btn")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("cards-grid")));

        driver.findElement(By.className("card")).click();
        wait.until(ExpectedConditions.urlContains("/item-card/"));

        String itemName = driver.findElement(By.className("item-name")).getText();

        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(By.className("btn-cart")));
        addBtn.click();
        driver.get(BASE_URL + "/user/profile/2");
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(5));
        longWait.until(ExpectedConditions.urlContains("/profile/"));
        WebElement myCartLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.btn-primary")));
        myCartLink.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("cart-item")));

        List<WebElement> cartItems = driver.findElements(By.className("cart-item"));
        boolean found = false;
        for (WebElement cartItem : cartItems) {
            String nameInCart = cartItem.findElement(By.className("item-name")).getText();
            if (nameInCart.contains(itemName)) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    void testMakeOrder() {
        login();
        clearCart();

        driver.findElement(By.className("view-items-btn")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("cards-grid")));

        WebElement firstCard = driver.findElement(By.className("card"));
        firstCard.findElement(By.className("card-title")).getText();

        WebElement addBtn = firstCard.findElement(By.className("cart-btn"));
        addBtn.click();

        driver.findElement(By.linkText("← Back to Home")).click();
        wait.until(ExpectedConditions.urlContains("/welcome"));
        driver.findElement(By.linkText("My account")).click();
        wait.until(ExpectedConditions.urlContains("/user/profile/"));

        WebElement myCartLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.btn-primary")));
        myCartLink.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("cart-item")));

        driver.findElement(By.className("btn-checkout-large")).click();
        wait.until(ExpectedConditions.urlContains("/user/order"));

        Select pointSelect = new Select(wait.until(ExpectedConditions.presenceOfElementLocated(By.id("pickupPoint"))));
        pointSelect.selectByIndex(1);
        driver.findElement(By.cssSelector("button.btn-primary")).click();

        WebElement emptyCart = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("empty-cart")));
        assertTrue(emptyCart.getText().contains("empty"));
    }
    @Test
    void testWriteReview() {
        login();
        driver.findElement(By.linkText("My account")).click();
        wait.until(ExpectedConditions.urlContains("/profile/"));

        List<WebElement> reviews = driver.findElements(By.className("review-btn"));
        if (!reviews.isEmpty()) {
            reviews.get(0).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("reviewModal")));
            new Select(driver.findElement(By.id("rating"))).selectByValue("5");
            driver.findElement(By.id("text")).sendKeys("Отлично!");
            driver.findElement(By.cssSelector("#reviewModal button.btn-primary")).click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("reviewModal")));
            WebElement alreadyReviewed = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("already-reviewed")));
            assertTrue(alreadyReviewed.getText().contains("already reviewed"));
        } else {
            List<WebElement> noItems = driver.findElements(By.className("no-items"));
            List<WebElement> alreadyReviewed = driver.findElements(By.className("already-reviewed"));
            if (!noItems.isEmpty()) {
                assertTrue(noItems.get(0).getText().contains("You haven't purchased any items yet"));
            } else {
                assertTrue(alreadyReviewed.get(0).getText().contains("already reviewed"));
            }
        }
    }

    private void login() {
        driver.get(BASE_URL + "/login");
        driver.findElement(By.id("number")).sendKeys(USER_NUMBER);
        driver.findElement(By.id("password")).sendKeys(USER_PASSWORD);
        driver.findElement(By.className("btn-login")).click();
        wait.until(ExpectedConditions.urlContains("/welcome"));
    }

    private void clearCart() {
        driver.findElement(By.linkText("My account")).click();
        wait.until(ExpectedConditions.urlContains("/profile/"));
        driver.findElement(By.cssSelector("a.btn-primary")).click();
        wait.until(ExpectedConditions.urlContains("/user/cart/"));
        List<WebElement> cartItems = driver.findElements(By.className("cart-item"));
        if (cartItems.isEmpty()) {
            driver.get(BASE_URL + "/welcome");
            return;
        }

        for (WebElement cb : driver.findElements(By.cssSelector(".cart-item input"))) {
            if (!cb.isSelected()) cb.click();
        }
        driver.findElement(By.cssSelector("button.btn-danger")).click();
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();
        wait.until(ExpectedConditions.numberOfElementsToBe(By.className("cart-item"), 0));
        driver.get(BASE_URL + "/welcome");
    }
}