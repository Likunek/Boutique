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
public class ItemsAtStorageJpaTest {

    @Autowired
    private ItemsAtStorageRepository itemsAtStorageRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private SellerRepository sellerRepository;

    private Storage testStorage;
    private Item testItem;
    private ItemsAtStorage testItemsAtStorage;
    private Seller testSeller;

    @BeforeEach
    void setUp() {
        testSeller = new Seller();
        testSeller.setName("Seller");
        testSeller.setNumber("89567432388");
        testSeller.setEmail("seller@mail.com");
        testSeller = sellerRepository.save(testSeller);

        testStorage = new Storage();
        testStorage.setAddress("Address");
        testStorage.setCity("City");
        testStorage.setMaxCapacity(5000L);
        testStorage = storageRepository.save(testStorage);

        testItem = new Item();
        testItem.setName("Item");
        testItem.setCostPrice(45.0);
        testItem.setWeight(2.0);
        testItem.setSquare(0.3);
        testItem.setVerify(true);
        testItem.setSeller(testSeller);
        testItem = itemRepository.save(testItem);

        testItemsAtStorage = new ItemsAtStorage();
        testItemsAtStorage.setItem(testItem);
        testItemsAtStorage.setStorage(testStorage);
        testItemsAtStorage.setCount(100L);
        testItemsAtStorage = itemsAtStorageRepository.save(testItemsAtStorage);
    }

    @AfterEach
    void tearDown() {
        itemsAtStorageRepository.deleteAll();
        itemRepository.deleteAll();
        storageRepository.deleteAll();
        sellerRepository.deleteAll();
    }

    @Test
    void findByStorageId_ShouldReturnItemsAtStorage() {
        List<ItemsAtStorage> items = itemsAtStorageRepository.findByStorageId(testStorage.getId());
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(testItemsAtStorage.getId(), items.get(0).getId());
        assertEquals(testStorage.getId(), items.get(0).getStorage().getId());
    }

    @Test
    void findByStorageId_ShouldReturnEmptyList_WhenNoRecords() {
        List<ItemsAtStorage> items = itemsAtStorageRepository.findByStorageId(999L);
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    void findByItemIdAndStorageId_ShouldReturnItemAtStorage() {
        ItemsAtStorage found = itemsAtStorageRepository.findByItemIdAndStorageId(testItem.getId(), testStorage.getId());
        assertNotNull(found);
        assertEquals(testItemsAtStorage.getId(), found.getId());
        assertEquals(testItem.getId(), found.getItem().getId());
        assertEquals(testStorage.getId(), found.getStorage().getId());
    }

    @Test
    void findByItemIdAndStorageId_ShouldReturnNull_WhenNotExists() {
        ItemsAtStorage found = itemsAtStorageRepository.findByItemIdAndStorageId(999L, testStorage.getId());
        assertNull(found);
    }
}