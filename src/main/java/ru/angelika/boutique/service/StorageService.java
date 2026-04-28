package ru.angelika.boutique.service;

import org.springframework.stereotype.Service;
import ru.angelika.boutique.dto.StorageDto;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.mapper.StorageMapper;
import ru.angelika.boutique.model.Storage;
import ru.angelika.boutique.repository.StorageRepository;

@Service
public class StorageService {
    private final StorageRepository storageRepository;

    public StorageService(StorageRepository storageRepository) {
        this.storageRepository = storageRepository;
    }

    public void addStorage(StorageDto storageDto) {
        storageRepository.save(StorageMapper.toStorage(storageDto));
    }

    public Storage getStorage(Long id) {
        return storageRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Storage.class, id));
    }

    public void updateStorage(StorageDto storageDto, Long id) {
        Storage storage = storageRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Storage.class, id));
        storageRepository.save(StorageMapper.updateStorage(storage, storageDto));
    }

    public void deleteStorage(Long id) {
        storageRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Storage.class, id));
        storageRepository.deleteById(id);
    }
}
