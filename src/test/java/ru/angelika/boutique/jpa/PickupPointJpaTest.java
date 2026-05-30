package ru.angelika.boutique.jpa;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.angelika.boutique.model.PickupPoint;
import ru.angelika.boutique.model.Storage;
import ru.angelika.boutique.repository.PickupPointRepository;
import ru.angelika.boutique.repository.StorageRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class PickupPointJpaTest {

    @Autowired
    private PickupPointRepository pickupPointRepository;

    @Autowired
    private StorageRepository storageRepository;

    private Storage testStorage;
    private PickupPoint testPoint;

    @BeforeEach
    void setUp() {
        testStorage = new Storage();
        testStorage.setAddress("Address");
        testStorage.setCity("City");
        testStorage.setMaxCapacity(10000L);
        testStorage = storageRepository.save(testStorage);

        testPoint = new PickupPoint();
        testPoint.setAddress("Point 1");
        testPoint.setCity("City");
        testPoint.setRating(4.5);
        testPoint.setStorage(testStorage);
        testPoint = pickupPointRepository.save(testPoint);
    }

    @AfterEach
    void tearDown() {
        pickupPointRepository.deleteAll();
        storageRepository.deleteAll();
    }

    @Test
    void findByStorageId_ShouldReturnPoints() {
        PickupPoint anotherPoint = new PickupPoint();
        anotherPoint.setAddress("Pickup Point 2");
        anotherPoint.setCity("Central City");
        anotherPoint.setRating(4.0);
        anotherPoint.setStorage(testStorage);
        final PickupPoint savedAnotherPoint = pickupPointRepository.save(anotherPoint);

        List<PickupPoint> points = pickupPointRepository.findByStorageId(testStorage.getId());
        assertNotNull(points);
        assertEquals(2, points.size());
        assertTrue(points.stream().anyMatch(p -> p.getId().equals(testPoint.getId())));
        assertTrue(points.stream().anyMatch(p -> p.getId().equals(savedAnotherPoint.getId())));
    }
    @Test
    void findByStorageId_ShouldReturnEmptyList_WhenNoPoints() {
        List<PickupPoint> points = pickupPointRepository.findByStorageId(999L);
        assertNotNull(points);
        assertTrue(points.isEmpty());
    }

    @Test
    void findByAddress_ShouldReturnPoints() {
        List<PickupPoint> points = pickupPointRepository.findByAddress("Point 1");
        assertNotNull(points);
        assertEquals(1, points.size());
        assertEquals(testPoint.getId(), points.get(0).getId());
    }

    @Test
    void findByAddress_ShouldReturnEmptyList_WhenNotExists() {
        List<PickupPoint> points = pickupPointRepository.findByAddress("Unknown Address");
        assertTrue(points.isEmpty());
    }

    @Test
    void findByCity_ShouldReturnPoints() {
        List<PickupPoint> points = pickupPointRepository.findByCity("City");
        assertNotNull(points);
        assertEquals(1, points.size());
        assertEquals(testPoint.getId(), points.get(0).getId());
    }

    @Test
    void findByCity_ShouldReturnEmptyList_WhenNotExists() {
        List<PickupPoint> points = pickupPointRepository.findByCity("Unknown City");
        assertTrue(points.isEmpty());
    }

    @Test
    void findByIdWithStorage_ShouldLoadPointWithStorage() {
        PickupPoint found = pickupPointRepository.findByIdWithStorage(testPoint.getId());
        assertNotNull(found);
        assertEquals(testPoint.getId(), found.getId());
        assertNotNull(found.getStorage());
        assertEquals(testStorage.getId(), found.getStorage().getId());
    }

    @Test
    void findByIdWithStorage_ShouldReturnNull_WhenNotExists() {
        PickupPoint found = pickupPointRepository.findByIdWithStorage(999L);
        assertNull(found);
    }
}