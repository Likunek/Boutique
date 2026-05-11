package ru.angelika.boutique.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.angelika.boutique.dto.StorageDto;
import ru.angelika.boutique.exception.ResourceExistsException;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.mapper.StorageMapper;
import ru.angelika.boutique.model.Item;
import ru.angelika.boutique.model.PickupPoint;
import ru.angelika.boutique.model.Storage;
import ru.angelika.boutique.repository.PickupPointRepository;
import ru.angelika.boutique.repository.StorageRepository;

import java.util.List;

@Slf4j
@Service
public class StorageService {
    private final StorageRepository storageRepository;
    private final ItemsAtStorageService itemsAtStorageService;
    private final PickupPointService pickupPointService;

    @Autowired
    public StorageService(StorageRepository storageRepository,
                          ItemsAtStorageService itemsAtStorageService, PickupPointService pickupPointService) {
        this.storageRepository = storageRepository;
        this.itemsAtStorageService = itemsAtStorageService;
        this.pickupPointService = pickupPointService;
    }

    public void addStorage(StorageDto storageDto) {
        checkDuplicate(storageDto.getAddress(), storageDto.getCity());
        storageRepository.save(StorageMapper.toStorage(storageDto));
        log.info("Add new storage: address={}, city={}, MaxCapacity={}",
                storageDto.getAddress(), storageDto.getCity(), storageDto.getMaxCapacity());
    }

    public Storage getStorage(Long id) {
        return storageRepository.findById(id).orElseThrow(() -> {
            log.error("Storage not found for get, id={}", id);
            return new ResourceNotFoundException(Storage.class, id);
        });
    }

    public List<Storage> getAllStorages() {
        return storageRepository.findAll();
    }

    public void updateStorage(StorageDto storageDto, Long id) {
        Storage storage = storageRepository.findById(id).orElseThrow(() -> {
            log.error("Storage not found for update, id={}", id);
            return new ResourceNotFoundException(Storage.class, id);
        });
        checkDuplicate(storageDto.getAddress(), storageDto.getCity());
        storageRepository.save(StorageMapper.updateStorage(storage, storageDto));
        log.info("Update storage by id={}", id);
    }

    public void deleteStorage(Long id) {
        storageRepository.findById(id).orElseThrow(() -> {
            log.error("Storage not found for delete, id={}", id);
            return new ResourceNotFoundException(Storage.class, id);
        });
        itemsAtStorageService.getItemsAtStorageByStorageId(id)
                .forEach(s -> itemsAtStorageService.deleteItemsAtStorage(s.getId()));
        replaceStorage(id);
        storageRepository.deleteById(id);
        log.info("Delete storage by id={}", id);
    }

    private void replaceStorage(Long id) {
        List<PickupPoint> points = pickupPointService.getAllPointsByStorage(id);
        for (PickupPoint point : points) {
            List<Storage> storages = storageRepository.findByCity(point.getCity());
            if (storages.size() == 0) {
                pickupPointService.deletePickupPoint(point.getId());
            } else {
                storages.stream()
                        .filter(s -> !s.getId().equals(id))
                        .findFirst()
                        .ifPresentOrElse(
                                storage -> {
                                    point.setStorage(storage);
                                    pickupPointService.updateStorage(point);
                                },
                                () -> pickupPointService.deletePickupPoint(point.getId())
                        );
            }
        }
    }

    private void checkDuplicate(String address, String city) {
        List<Storage> storagesByAddress = storageRepository.findByAddress(address);
        List<Storage> storagesByCity = storageRepository.findByCity(city);
        if (storagesByAddress.size() > 0 && storagesByCity.size() > 0) {
            log.error("Storage with address={}, city={} already exists", address, city);
            throw new ResourceExistsException(Storage.class, city + " : " + address);
        }
    }
}
