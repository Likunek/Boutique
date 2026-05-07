package ru.angelika.boutique.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.angelika.boutique.dto.StorageDto;
import ru.angelika.boutique.exception.ResourceExistsException;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.mapper.StorageMapper;
import ru.angelika.boutique.model.Storage;
import ru.angelika.boutique.repository.StorageRepository;

import java.util.List;

@Slf4j
@Service
public class StorageService {
    private final StorageRepository storageRepository;
    private final ItemsAtStorageService itemsAtStorageService;

    @Autowired
    public StorageService(StorageRepository storageRepository, ItemsAtStorageService itemsAtStorageService) {
        this.storageRepository = storageRepository;
        this.itemsAtStorageService = itemsAtStorageService;
    }

    public void addStorage(StorageDto storageDto) {
        storageRepository.save(StorageMapper.toStorage(storageDto));
    }

    public List<Storage> getAllStorage() {
        return storageRepository.findAll();
    }

    public Storage getStorage(Long id) {
        return storageRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Storage.class, id));
    }

    public List<Storage> getAllStorages() {
        return storageRepository.findAll();
    }

    public void updateStorage(StorageDto storageDto, Long id) {
        Storage storage = storageRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Storage.class, id));
        List<Storage> storagesByAddress = storageRepository.findByAddress(storageDto.getAddress());
        List<Storage> storagesByCity = storageRepository.findByCity(storageDto.getCity());
        if (storagesByAddress.size() > 0 && storagesByCity.size() > 0) {
            log.error("Storage with address={}, city={} already exists",
                    storageDto.getAddress(), storageDto.getCity());
            throw new ResourceExistsException(Storage.class,  storageDto.getCity() + " : " + storageDto.getAddress());
        }
        storageRepository.save(StorageMapper.updateStorage(storage, storageDto));
        log.info("Update Storage by id={}", id);
    }

    public void deleteStorage(Long id) {
        storageRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Storage.class, id));
        itemsAtStorageService.getItemsAtStorageByStorageId(id)
                .forEach(s -> itemsAtStorageService.deleteItemsAtStorage(s.getId()));
        storageRepository.deleteById(id);
        log.info("Delete Storage by id={}", id);
    }
}
