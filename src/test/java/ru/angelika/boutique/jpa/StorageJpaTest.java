package ru.angelika.boutique.jpa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.angelika.boutique.model.Storage;
import ru.angelika.boutique.repository.StorageRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class StorageJpaTest {

    @Autowired
    private StorageRepository storageRepository;

    private Storage testStorage;

    @BeforeEach
    void setUp() {
        testStorage = new Storage();
        testStorage.setAddress("Address");
        testStorage.setCity("City");
        testStorage.setMaxCapacity(2000L);
        testStorage = storageRepository.save(testStorage);
    }

    @AfterEach
    void tearDown() {
        storageRepository.deleteAll();
    }

    @Test
    void findByAddress_ShouldReturnStorages() {
        List<Storage> storages = storageRepository.findByAddress("Address");
        assertNotNull(storages);
        assertEquals(1, storages.size());
        assertEquals(testStorage.getId(), storages.get(0).getId());
    }

    @Test
    void findByAddress_ShouldReturnEmptyList_WhenNotExists() {
        List<Storage> storages = storageRepository.findByAddress("Non");
        assertTrue(storages.isEmpty());
    }

    @Test
    void findByCity_ShouldReturnStorages() {
        List<Storage> storages = storageRepository.findByCity("City");
        assertNotNull(storages);
        assertEquals(1, storages.size());
        assertEquals(testStorage.getId(), storages.get(0).getId());
    }

    @Test
    void findByCity_ShouldReturnEmptyList_WhenNotExists() {
        List<Storage> storages = storageRepository.findByCity("Non City");
        assertTrue(storages.isEmpty());
    }
}