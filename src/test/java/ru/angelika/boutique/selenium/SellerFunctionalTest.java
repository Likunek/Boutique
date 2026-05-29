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

public class SellerFunctionalTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:8080";
    private static final String SELLER_NUMBER = "09030765488";
    private static final String SELLER_PASSWORD = "00000";

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
    void testSellerLoginLogout() {
        login();
        driver.findElement(By.className("btn-logout")).click();
        wait.until(ExpectedConditions.urlContains("/login"));
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertEquals("🔐 Welcome Back", header.getText());
    }

    @Test
    void testSellerProfileAccess() {
        login();
        driver.findElement(By.partialLinkText("My account")).click();
        wait.until(ExpectedConditions.urlContains("/seller/profile/"));
        WebElement roleBadge = driver.findElement(By.className("role-badge"));
        assertEquals("SELLER", roleBadge.getText());
        assertTrue(driver.findElement(By.partialLinkText("Add Item")).isDisplayed());
        assertTrue(driver.findElement(By.partialLinkText("List Items")).isDisplayed());
        assertTrue(driver.findElement(By.partialLinkText("Send to Storage")).isDisplayed());
        assertTrue(driver.findElement(By.partialLinkText("Create Item's Card")).isDisplayed());
    }

    @Test
    void testSellerAddItemAndDeleteItem() {
        login();
        String uniqueName = "TestItem";
        driver.findElement(By.linkText("My account")).click();
        wait.until(ExpectedConditions.urlContains("/seller/profile/"));
        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Add Item"))).click();
        wait.until(ExpectedConditions.urlContains("/seller/add-item"));

        driver.findElement(By.id("name")).sendKeys(uniqueName);
        driver.findElement(By.id("costPrice")).sendKeys("100");
        driver.findElement(By.id("weight")).sendKeys("1.5");
        driver.findElement(By.id("square")).clear();
        driver.findElement(By.id("square")).sendKeys("0.5");
        driver.findElement(By.cssSelector("button.btn-primary")).click();

        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(7));
        longWait.until(ExpectedConditions.urlContains("/seller/add-item"));
        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Back to Profile"))).click();
        wait.until(ExpectedConditions.urlContains("/seller/profile/"));
        driver.findElement(By.partialLinkText("List Items")).click();
        wait.until(ExpectedConditions.urlContains("/seller/items"));
        WebElement table = driver.findElement(By.tagName("table"));
        assertTrue(table.getText().contains(uniqueName));

        driver.get(BASE_URL + "/seller/profile/5");
        wait.until(ExpectedConditions.urlContains("/seller/profile/"));
        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("List Items"))).click();
        wait.until(ExpectedConditions.urlContains("/seller/items"));

        WebElement element = driver.findElement(By.tagName("table"));
        if (element.getText().contains(uniqueName)) {
            driver.findElement(By.linkText(uniqueName)).click();
            wait.until(ExpectedConditions.urlContains("/items/"));
            WebElement deleteBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".delete-container .btn-danger")));
            deleteBtn.click();
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
            WebDriverWait time = new WebDriverWait(driver, Duration.ofSeconds(5));
            time.until(ExpectedConditions.urlContains("/seller/items"));
        }
    }


    @Test
    void testSellerEditItem() {
        login();
        driver.findElement(By.partialLinkText("My account")).click();
        wait.until(ExpectedConditions.urlContains("/seller/profile/"));
        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("List Items"))).click();
        wait.until(ExpectedConditions.urlContains("/seller/items"));
        driver.findElement(By.cssSelector("table tbody tr:first-child a")).click();
        wait.until(ExpectedConditions.urlContains("/items/"));

        WebElement nameHeader = driver.findElement(By.cssSelector(".form-header h1"));
        String originalName = nameHeader.getText();

        WebElement editBtn = wait
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[onclick*='enableItemEdit']")));
        editBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("itemEditMode")));
        WebElement nameField = driver.findElement(By.name("name"));
        nameField.clear();
        nameField.sendKeys("Test name");
        driver.findElement(By.cssSelector("#itemEditMode button.btn-primary")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("itemViewMode")));
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[onclick*='enableItemEdit']"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("itemEditMode")));
        WebElement name = driver.findElement(By.name("name"));
        name.clear();
        name.sendKeys(originalName);
        driver.findElement(By.cssSelector("#itemEditMode button.btn-primary")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("itemViewMode")));
        wait.until(ExpectedConditions
                .textToBePresentInElement(driver.findElement(By.cssSelector(".form-header h1")), originalName));
    }

    @Test
    void testSellerAddCard() {
        login();
        driver.findElement(By.partialLinkText("My account")).click();
        wait.until(ExpectedConditions.urlContains("/seller/profile/"));
        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Create Item's Card"))).click();
        wait.until(ExpectedConditions.urlContains("/seller/add-card"));

        Select itemSelect = new Select(wait.until(ExpectedConditions.presenceOfElementLocated(By.id("itemId"))));
        if (itemSelect.getOptions().size() <= 1) {
            return;
        }
      String selectedItemFullText = itemSelect.getOptions().get(1).getText();
        itemSelect.selectByIndex(1);

        String cardName = "Card_" + System.currentTimeMillis();
        driver.findElement(By.id("name")).sendKeys(cardName);
        driver.findElement(By.id("description")).sendKeys("Тестовое описание карточки");
        driver.findElement(By.cssSelector("button.btn-primary")).click();

        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Back to Profile"))).click();
        wait.until(ExpectedConditions.urlContains("/seller/profile/"));

        driver.findElement(By.partialLinkText("List Items")).click();
        wait.until(ExpectedConditions.urlContains("/seller/items"));

        String pureItemName = selectedItemFullText.split(" \\(")[0];
        driver.findElement(By.partialLinkText(pureItemName)).click();
        wait.until(ExpectedConditions.urlContains("/items/"));

        WebElement cardBlock = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("mini-card")));
        assertTrue(cardBlock.getText().contains(cardName));
    }

    @Test
    void testSellerEditCard() {
        login();
        driver.findElement(By.partialLinkText("My account")).click();
        wait.until(ExpectedConditions.urlContains("/seller/profile/"));
        openFirstItem();
        //driver.get(BASE_URL + "/items/15");
        //wait.until(ExpectedConditions.urlContains("/items/"));
        List<WebElement> editCardBtns = driver.findElements(By.cssSelector("button.btn-secondary[onclick*='enableCardEdit']"));
        if (editCardBtns.isEmpty()) {
            return;
        }
        WebElement editCardBtn = editCardBtns.get(0);
        editCardBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cardEditMode")));

        WebElement cardNameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#cardEditMode input[name='name']")));
        String newName = "UpdatedCard_" + System.currentTimeMillis();
        cardNameInput.clear();
        cardNameInput.sendKeys(newName);
        driver.findElement(By.cssSelector("#cardEditMode button.btn-primary")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("mini-card")));
        wait.until(ExpectedConditions.textToBePresentInElement(driver.findElement(By.className("mini-item-name")), newName));
    }

    @Test
    void testSellerSendToStorage() {
        login();
        driver.findElement(By.partialLinkText("My account")).click();
        wait.until(ExpectedConditions.urlContains("/seller/profile/"));

        WebElement addItemBtn = wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Send to Storage")));
        addItemBtn.click();
        wait.until(ExpectedConditions.urlContains("/seller/send-to-storage"));

        Select itemSelect = new Select(wait.until(ExpectedConditions.presenceOfElementLocated(By.id("itemId"))));
        if (itemSelect.getOptions().size() <= 1) {
            return;
        }
        Select storageSelect = new Select(driver.findElement(By.id("storageId")));
        if (storageSelect.getOptions().size() <= 1) {
            return;
        }

        String selectedItemFullText = itemSelect.getOptions().get(1).getText();
        itemSelect.selectByIndex(1);
        storageSelect.selectByIndex(1);

        driver.findElement(By.id("count")).sendKeys("3");
        driver.findElement(By.cssSelector("button.btn-primary")).click();

        wait.until(ExpectedConditions.urlContains("/seller/send-to-storage"));
        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Back to Profile"))).click();
        wait.until(ExpectedConditions.urlContains("/seller/profile/"));
        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("List Items"))).click();
        wait.until(ExpectedConditions.urlContains("/seller/items"));

        String pureItemName = selectedItemFullText.split(" \\(")[0];
        WebElement itemLink = wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText(pureItemName)));
        itemLink.click();
        wait.until(ExpectedConditions.urlContains("/items/"));

        WebElement storageTable = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("storage-table")));
        assertTrue(storageTable.getText().contains("3"));
    }

    @Test
    void testSellerEditStorageCount() {
        login();
        driver.findElement(By.partialLinkText("My account")).click();
        wait.until(ExpectedConditions.urlContains("/seller/profile/"));
        openFirstItem();
        //driver.get(BASE_URL + "/items/15");
        //wait.until(ExpectedConditions.urlContains("/items/"));

        List<WebElement> storageRows = driver.findElements(By.cssSelector(".storage-table tbody tr"));
        if (storageRows.isEmpty()) {
            return;
        }

        WebElement firstRow = storageRows.get(0);
        WebElement editBtn = firstRow.findElement(By.cssSelector("button.btn-text"));
        editBtn.click();

        WebElement countInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".storage-table input[name='count']")));
        String oldCount = countInput.getAttribute("value");
        int newCount = Integer.parseInt(oldCount) + 1;
        countInput.clear();
        countInput.sendKeys(String.valueOf(newCount));

        WebElement updateBtn = driver.findElement(By.cssSelector(".storage-table button.btn-primary"));
        updateBtn.click();

        wait.until(ExpectedConditions.invisibilityOf(countInput));
        WebElement updatedCountSpan = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".storage-table tbody tr:first-child span[id^='countView-']")));
        assertTrue(updatedCountSpan.getText().contains(String.valueOf(newCount)));
    }

    @Test
    void testSellerNoCartOnPublicCards() {
        login();
        driver.get(BASE_URL + "/item-card?role=ROLE_SELLER");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("cards-grid")));
        List<WebElement> cartButtons = driver.findElements(By.className("cart-btn"));
        assertTrue(cartButtons.isEmpty());
    }

    @Test
    void testSellerNoCartOnPublicDetail() {
        login();
        driver.get(BASE_URL + "/item-card?role=ROLE_SELLER");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("card")));
        driver.findElement(By.className("card")).click();
        wait.until(ExpectedConditions.urlContains("/item-card/"));
        List<WebElement> cartButtons = driver.findElements(By.className("btn-cart"));
        assertTrue(cartButtons.isEmpty());
        List<WebElement> deleteBtns = driver.findElements(By.cssSelector(".delete-container .btn-danger"));
        assertTrue(deleteBtns.isEmpty());
    }

    private void login() {
        driver.get(BASE_URL + "/login");
        driver.findElement(By.id("number")).sendKeys(SELLER_NUMBER);
        driver.findElement(By.id("password")).sendKeys(SELLER_PASSWORD);
        driver.findElement(By.className("btn-login")).click();
        wait.until(ExpectedConditions.urlContains("/welcome"));
    }
    private void openFirstItem() {
        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("List Items"))).click();
        wait.until(ExpectedConditions.urlContains("/seller/items"));
        List<WebElement> rows = driver.findElements(By.cssSelector("table tbody tr"));
        if (rows.isEmpty()) {
            return;
        }
        driver.findElement(By.cssSelector("table tbody tr:first-child a")).click();
        wait.until(ExpectedConditions.urlContains("/items/"));
    }
}