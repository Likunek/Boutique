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

public class AdminFunctionalTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:8080";
    private static final String ADMIN_NUMBER = "89538921200";
    private static final String ADMIN_PASSWORD = "0000";

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setUp() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    void testAdminLoginLogout() {
        login();
        driver.findElement(By.className("btn-logout")).click();
        wait.until(ExpectedConditions.urlContains("/login"));
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertEquals("🔐 Welcome Back", header.getText());
    }

    @Test
    void testAdminDashboardAccess() {
        login();
        driver.findElement(By.partialLinkText("My account")).click();
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(20));
        longWait.until(ExpectedConditions.urlContains("/admin/profile/1"));
        assertTrue(driver.findElement(By.partialLinkText("All sellers")).isDisplayed());
        assertTrue(driver.findElement(By.partialLinkText("All users")).isDisplayed());
        assertTrue(driver.findElement(By.partialLinkText("Storages")).isDisplayed());
        assertTrue(driver.findElement(By.partialLinkText("Add storage")).isDisplayed());
        assertTrue(driver.findElement(By.partialLinkText("All items")).isDisplayed());
        assertTrue(driver.findElement(By.partialLinkText("Items in storage")).isDisplayed());
        assertTrue(driver.findElement(By.partialLinkText("Pickup points")).isDisplayed());
        assertTrue(driver.findElement(By.partialLinkText("Add pickup point")).isDisplayed());
        assertTrue(driver.findElement(By.partialLinkText("Orders")).isDisplayed());
        assertTrue(driver.findElement(By.partialLinkText("Supplies")).isDisplayed());
    }

    @Test
    void testAdminViewAllSellers() {
        login();
        driver.findElement(By.partialLinkText("My account")).click();
        wait.until(ExpectedConditions.urlContains("/admin/profile/1"));
        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("All sellers"))).click();
        wait.until(ExpectedConditions.urlContains("/admin/sellers"));
        WebElement table = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));
        assertTrue(table.isDisplayed());
    }

    @Test
    void testAdminViewAllUsers() {
        login();
        driver.findElement(By.partialLinkText("My account")).click();
        wait.until(ExpectedConditions.urlContains("/admin/profile/1"));
        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("All users"))).click();
        wait.until(ExpectedConditions.urlContains("/admin/users"));
        WebElement table = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));
        assertTrue(table.isDisplayed());
    }

    @Test
    void testAdminViewAllStorages() {
        login();
        driver.findElement(By.partialLinkText("My account")).click();
        wait.until(ExpectedConditions.urlContains("/admin/profile/1"));
        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Storages"))).click();
        wait.until(ExpectedConditions.urlContains("/admin/storages"));
        WebElement table = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));
        assertTrue(table.isDisplayed());
    }

    @Test
    void testAdminAddAndDeleteStorage() {
        login();
        driver.findElement(By.partialLinkText("My account")).click();
        wait.until(ExpectedConditions.urlContains("/admin/profile/1"));
        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Add storage"))).click();
        wait.until(ExpectedConditions.urlContains("/admin/storages/add"));

        String uniqueAddress = "TestAddress";
        driver.findElement(By.id("address")).sendKeys(uniqueAddress);
        driver.findElement(By.id("city")).sendKeys("TestCity");
        driver.findElement(By.id("maxCapacity")).sendKeys("1000");
        driver.findElement(By.cssSelector("button.btn-primary")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Back to Admin"))).click();
        wait.until(ExpectedConditions.urlContains("/admin/profile/1"));
        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Storages"))).click();
        wait.until(ExpectedConditions.urlContains("/admin/storages"));
        WebElement table = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));
        System.out.println(table.getText());
        assertTrue(table.getText().contains(uniqueAddress));

        WebElement row = wait.until(d -> {
            WebElement tbl = d.findElement(By.tagName("table"));
            for (WebElement r : tbl.findElements(By.cssSelector("tbody tr"))) {
                if (r.getText().contains(uniqueAddress)) {
                    return r;
                }
            }
            return null;
        });
        WebElement deleteBtn = row.findElement(By.cssSelector(".btn-danger"));
        deleteBtn.click();
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();
        wait.until(ExpectedConditions.stalenessOf(row));
    }

    @Test
    void testAdminEditStorage() {
        login();
        driver.findElement(By.partialLinkText("My account")).click();
        wait.until(ExpectedConditions.urlContains("/admin/profile/1"));
        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Storages"))).click();
        wait.until(ExpectedConditions.urlContains("/admin/storages"));

        List<WebElement> rows = driver.findElements(By.cssSelector("table tbody tr"));
        if (rows.isEmpty()) return;
        WebElement firstRow = rows.get(0);
        WebElement editBtn = firstRow.findElement(By.cssSelector(".btn-edit"));
        editBtn.click();

        WebElement addressInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='text']")));
        String originalAddress = addressInput.getAttribute("value");
        String newAddress = originalAddress + "_edited";
        addressInput.clear();
        addressInput.sendKeys(newAddress);
        driver.findElement(By.cssSelector("button.btn-save")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("table"), newAddress));

        WebElement targetRow = null;
        WebElement table = driver.findElement(By.tagName("table"));
        for (WebElement row : table.findElements(By.cssSelector("tbody tr"))) {
            if (row.getText().contains(newAddress)) {
                targetRow = row;
                break;
            }
        }
        assertNotNull(targetRow);

        editBtn = targetRow.findElement(By.cssSelector(".btn-edit"));
        editBtn.click();
        addressInput = wait.until(ExpectedConditions.visibilityOf(targetRow.findElement(By.cssSelector("input[type='text']"))));
        addressInput.clear();
        addressInput.sendKeys(originalAddress);
        targetRow.findElement(By.cssSelector(".btn-save")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("table"), originalAddress));
    }

    @Test
    void testAdminViewAllItems() {
        login();
        driver.findElement(By.partialLinkText("My account")).click();
        wait.until(ExpectedConditions.urlContains("/admin/profile/1"));
        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("All items"))).click();
        wait.until(ExpectedConditions.urlContains("/admin/items"));
        WebElement table = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));
        assertTrue(table.isDisplayed());
    }

    @Test
    void testAdminVerifyItem() {
        login();
        driver.findElement(By.partialLinkText("My account")).click();
        wait.until(ExpectedConditions.urlContains("/admin/profile/1"));
        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("All items"))).click();
        wait.until(ExpectedConditions.urlContains("/admin/items"));
        List<WebElement> noButtons = driver.findElements(By.cssSelector(".verify-btn:not(.verified)"));
        if (noButtons.isEmpty()) {return;}
        WebElement firstNoBtn = noButtons.get(0);
        String oldText = firstNoBtn.getText();
        firstNoBtn.click();

        WebElement yesBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".verify-btn.verified")));
        assertEquals("Yes", yesBtn.getText());

        yesBtn.click();
        WebElement noBtnAgain = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".verify-btn:not(.verified)")));
        assertEquals("No", noBtnAgain.getText());
    }

    @Test
    void testAdminFilterUnverifiedItems() {
        login();
        driver.findElement(By.partialLinkText("My account")).click();
        wait.until(ExpectedConditions.urlContains("/admin/profile/1"));
        driver.findElement(By.partialLinkText("All items")).click();
        wait.until(ExpectedConditions.urlContains("/admin/items"));

        WebElement unverifiedLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Unverified only")));
        unverifiedLink.click();
        wait.until(ExpectedConditions.urlContains("unverified=false"));

        WebElement table = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));
        List<WebElement> rows = table.findElements(By.cssSelector("tbody tr"));
        if (rows.isEmpty()) return;

        for (WebElement row : rows) {
            WebElement verifyBtn = row.findElement(By.cssSelector(".verify-btn"));
            assertEquals("No", verifyBtn.getText());
        }
    }

    @Test
    void testAdminAllItemsSwitchingToItemPage() {
        login();
        driver.findElement(By.partialLinkText("My account")).click();
        wait.until(ExpectedConditions.urlContains("/admin/profile/1"));
        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("All items"))).click();
        wait.until(ExpectedConditions.urlContains("/admin/items"));

        List<WebElement> itemLinks = driver.findElements(By.cssSelector(".item-link"));
        if (itemLinks.isEmpty()) return;

        itemLinks.get(0).click();
        wait.until(ExpectedConditions.urlContains("/items/"));
        WebElement itemPageBlock = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("profile-card")));
        assertTrue(itemPageBlock.isDisplayed());
    }

    @Test
    void testAdminViewItemsAtStorage() {
        login();
        driver.findElement(By.partialLinkText("My account")).click();
        wait.until(ExpectedConditions.urlContains("/admin/profile/1"));
        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Items in storage"))).click();
        wait.until(ExpectedConditions.urlContains("/admin/item-at-storage"));
        WebElement table = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));
        assertTrue(table.isDisplayed());
    }

    @Test
    void testAdminItemsAtStorageSwitchingToItemPage() {
        login();
        driver.findElement(By.partialLinkText("My account")).click();
        wait.until(ExpectedConditions.urlContains("/admin/profile/1"));
        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Items in storage"))).click();
        wait.until(ExpectedConditions.urlContains("/admin/item-at-storage"));

        List<WebElement> itemLinks = driver.findElements(By.cssSelector("a.btn-link"));
        if (itemLinks.isEmpty()) {return;}

        itemLinks.get(0).click();
        wait.until(ExpectedConditions.urlContains("/items/"));
        WebElement itemPageBlock = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("profile-card")));
        assertTrue(itemPageBlock.isDisplayed());
    }

    @Test
    void testAdminViewPickupPoints() {
        login();
        driver.findElement(By.partialLinkText("My account")).click();
        wait.until(ExpectedConditions.urlContains("/admin/profile/1"));
        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Pickup points"))).click();
        wait.until(ExpectedConditions.urlContains("/admin/points"));
        WebElement table = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));
        assertTrue(table.isDisplayed());
    }

    @Test
    void testAdminAddAndDeletePickupPoint() {
        login();
        driver.findElement(By.partialLinkText("My account")).click();
        wait.until(ExpectedConditions.urlContains("/admin/profile/1"));
        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Add pickup point"))).click();
        wait.until(ExpectedConditions.urlContains("/admin/points/add"));

        Select storageSelect = new Select(wait.until(ExpectedConditions.presenceOfElementLocated(By.id("storageId"))));
        if (storageSelect.getOptions().size() <= 1) return;

        String uniqueAddress = "TestPoint";
        driver.findElement(By.id("address")).sendKeys(uniqueAddress);
        driver.findElement(By.id("city")).sendKeys("TestCity");
        storageSelect.selectByIndex(1);
        driver.findElement(By.cssSelector("button.btn-primary")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Back to Admin"))).click();
        wait.until(ExpectedConditions.urlContains("/admin/profile/1"));
        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Pickup points"))).click();
        wait.until(ExpectedConditions.urlContains("/admin/points"));
        WebElement table = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));
        System.out.println(table.getText());
        assertTrue(table.getText().contains(uniqueAddress));

        WebElement row = wait.until(d -> {
            WebElement tbl = d.findElement(By.tagName("table"));
            for (WebElement r : tbl.findElements(By.cssSelector("tbody tr"))) {
                if (r.getText().contains(uniqueAddress)) {
                    return r;
                }
            }
            return null;
        });
        WebElement deleteBtn = row.findElement(By.cssSelector(".btn-danger"));
        deleteBtn.click();
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();
        wait.until(ExpectedConditions.stalenessOf(row));
    }

    @Test
    void testAdminEditPickupPoint() {
        login();
        driver.findElement(By.partialLinkText("My account")).click();
        wait.until(ExpectedConditions.urlContains("/admin/profile/1"));
        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Pickup points"))).click();
        wait.until(ExpectedConditions.urlContains("/admin/points"));
        List<WebElement> rows = driver.findElements(By.cssSelector("table tbody tr"));
        if (rows.isEmpty()) return;
        WebElement firstRow = rows.get(0);
        WebElement editBtn = firstRow.findElement(By.cssSelector(".btn-edit"));
        editBtn.click();

        WebElement addressInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='text']")));
        String originalAddress = addressInput.getAttribute("value");
        String newAddress = originalAddress + "_edited";
        addressInput.clear();
        addressInput.sendKeys(newAddress);
        driver.findElement(By.cssSelector("button.btn-save")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("table"), newAddress));

        WebElement targetRow = null;
        WebElement table = driver.findElement(By.tagName("table"));
        for (WebElement row : table.findElements(By.cssSelector("tbody tr"))) {
            if (row.getText().contains(newAddress)) {
                targetRow = row;
                break;
            }
        }
        assertNotNull(targetRow);

        editBtn = targetRow.findElement(By.cssSelector(".btn-edit"));
        editBtn.click();
        addressInput = wait.until(ExpectedConditions.visibilityOf(targetRow.findElement(By.cssSelector("input[type='text']"))));
        addressInput.clear();
        addressInput.sendKeys(originalAddress);
        targetRow.findElement(By.cssSelector(".btn-save")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("table"), originalAddress));
    }

    @Test
    void testAdminViewOrders() {
        login();
        driver.findElement(By.partialLinkText("My account")).click();
        wait.until(ExpectedConditions.urlContains("/admin/profile/1"));
        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Orders"))).click();
        wait.until(ExpectedConditions.urlContains("/admin/orders"));
        WebElement table = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));
        assertTrue(table.isDisplayed());
    }

    @Test
    void testAdminViewSupplies() {
        login();
        driver.findElement(By.partialLinkText("My account")).click();
        wait.until(ExpectedConditions.urlContains("/admin/profile/1"));
        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Supplies"))).click();
        wait.until(ExpectedConditions.urlContains("/admin/supplies"));
        WebElement table = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));
        assertTrue(table.isDisplayed());
    }

    @Test
    void testAdminNoCartOnPublicCards() {
        login();
        driver.findElement(By.className("view-items-btn")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("cards-grid")));
        List<WebElement> cartButtons = driver.findElements(By.className("cart-btn"));
        assertTrue(cartButtons.isEmpty());
    }

    @Test
    void testAdminNoCartOnPublicDetail() {
        login();
        driver.findElement(By.className("view-items-btn")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("card")));
        driver.findElement(By.className("card")).click();
        wait.until(ExpectedConditions.urlContains("/item-card/"));
        List<WebElement> cartButtons = driver.findElements(By.className("btn-cart"));
        assertTrue(cartButtons.isEmpty());
    }

    private void login() {
        driver.get(BASE_URL + "/login");
        driver.findElement(By.id("number")).sendKeys(ADMIN_NUMBER);
        driver.findElement(By.id("password")).sendKeys(ADMIN_PASSWORD);
        driver.findElement(By.className("btn-login")).click();
        wait.until(ExpectedConditions.urlContains("/welcome"));
    }

}