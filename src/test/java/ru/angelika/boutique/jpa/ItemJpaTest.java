package ru.angelika.boutique.jpa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.angelika.boutique.model.*;
import ru.angelika.boutique.repository.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ItemJpaTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private ItemCardRepository itemCardRepository;

    @Autowired
    private ItemsAtStorageRepository itemsAtStorageRepository;

    @Autowired
    private StorageRepository storageRepository;

    private Seller testSeller;
    private Item testItem;
    private ItemCard testItemCard;
    private Storage testStorage;
    private ItemsAtStorage testItemsAtStorage;

    @BeforeEach
    void setUp() {
        testSeller = new Seller();
        testSeller.setName("Item Seller");
        testSeller.setNumber("11122233355");
        testSeller.setEmail("seller@mail.com");
        testSeller = sellerRepository.save(testSeller);

        testItemCard = new ItemCard();
        testItemCard.setName("Test Item Card");
        testItemCard.setPrice(100.0);
        testItemCard.setSeller(testSeller.getName());
        testItemCard = itemCardRepository.save(testItemCard);

        testItem = new Item();
        testItem.setName("Test Item");
        testItem.setCostPrice(80.0);
        testItem.setWeight(1.5);
        testItem.setSquare(0.5);
        testItem.setVerify(true);
        testItem.setSeller(testSeller);
        testItem.setItemCard(testItemCard);
        testItem = itemRepository.save(testItem);

        testStorage = new Storage();
        testStorage.setAddress("Address");
        testStorage.setCity("City");
        testStorage.setMaxCapacity(1000L);
        testStorage = storageRepository.save(testStorage);

        testItemsAtStorage = new ItemsAtStorage();
        testItemsAtStorage.setItem(testItem);
        testItemsAtStorage.setStorage(testStorage);
        testItemsAtStorage.setCount(50L);
        testItemsAtStorage = itemsAtStorageRepository.save(testItemsAtStorage);
    }

    @AfterEach
    void tearDown() {
        itemsAtStorageRepository.deleteAll();
        itemRepository.deleteAll();
        itemCardRepository.deleteAll();
        storageRepository.deleteAll();
        sellerRepository.deleteAll();
    }

    @Test
    void findByItemCardId_ShouldReturnItem_WhenExists() {
        Item found = itemRepository.findByItemCardId(testItemCard.getId());
        assertNotNull(found);
        assertEquals(testItem.getId(), found.getId());
    }

    @Test
    void findByItemCardId_ShouldReturnNull_WhenNotExists() {
        Item found = itemRepository.findByItemCardId(999L);
        assertNull(found);
    }

    @Test
    void findBySellerIdAndName_ShouldReturnItem_WhenExists() {
        Item found = itemRepository.findBySellerIdAndName(testSeller.getId(), "Test Item");
        assertNotNull(found);
        assertEquals(testItem.getId(), found.getId());
    }

    @Test
    void findBySellerIdAndName_ShouldReturnNull_WhenNotExists() {
        Item found = itemRepository.findBySellerIdAndName(testSeller.getId(), "Non");
        assertNull(found);
    }

    @Test
    void findBySellerId_ShouldReturnListOfItems() {
        List<Item> items = itemRepository.findBySellerId(testSeller.getId());
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(testItem.getId(), items.get(0).getId());
    }

    @Test
    void findByVerifyFalse_ShouldReturnUnverifiedItems() {
        Item item = new Item();
        item.setName("Unverified Item");
        item.setCostPrice(10.0);
        item.setWeight(0.5);
        item.setSquare(0.1);
        item.setVerify(false);
        item.setSeller(testSeller);
        item = itemRepository.save(item);

        List<Item> items = itemRepository.findByVerifyFalse();
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(item.getId(), items.get(0).getId());
    }

    @Test
    void findBySellerIdAndVerifyTrue_ShouldReturnVerifiedItems() {
        List<Item> items = itemRepository.findBySellerIdAndVerifyTrue(testSeller.getId());
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(testItem.getId(), items.get(0).getId());
    }

    @Test
    void findBySellerIdAndVerifyTrueAndItemCardIsNull_ShouldReturnItemsWithoutCard() {
        Item noCardItem = new Item();
        noCardItem.setName("No Card Item");
        noCardItem.setCostPrice(20.0);
        noCardItem.setWeight(0.8);
        noCardItem.setSquare(0.2);
        noCardItem.setVerify(true);
        noCardItem.setSeller(testSeller);
        noCardItem.setItemCard(null);
        noCardItem = itemRepository.save(noCardItem);

        List<Item> items = itemRepository.findBySellerIdAndVerifyTrueAndItemCardIsNull(testSeller.getId());
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(noCardItem.getId(), items.get(0).getId());
    }

    @Test
    void findItemByItemsAtStorageId_ShouldReturnItem_WhenExists() {
        Item found = itemRepository.findItemByItemsAtStorageId(testItemsAtStorage.getId());
        assertNotNull(found);
        assertEquals(testItem.getId(), found.getId());
    }

    @Test
    void findItemByItemsAtStorageId_ShouldReturnNull_WhenNotExists() {
        Item found = itemRepository.findItemByItemsAtStorageId(999L);
        assertNull(found);
    }

    @Test
    void findBySellerWithStorages_ShouldLoadItemsWithStorages() {
        testItem.getItemsAtStorages().add(testItemsAtStorage);
        itemRepository.save(testItem);
        List<Item> items = itemRepository.findBySellerWithStorages(testSeller);
        assertNotNull(items);
        assertEquals(1, items.size());
        Item loadedItem = items.get(0);
        assertNotNull(loadedItem.getItemsAtStorages());
        assertFalse(loadedItem.getItemsAtStorages().isEmpty());
        assertEquals(testItemsAtStorage.getId(), loadedItem.getItemsAtStorages().get(0).getId());
    }
}