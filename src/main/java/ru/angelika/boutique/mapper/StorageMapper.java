package ru.angelika.boutique.mapper;

import ru.angelika.boutique.dto.StorageDto;
import ru.angelika.boutique.model.Storage;

public class StorageMapper {
    public static Storage toStorage(StorageDto storageDto) {
        Storage storage = new Storage();
        storage.setAddress(storageDto.getAddress());
        storage.setCity(storageDto.getCity());
        storage.setMaxCapacity(storageDto.getMaxCapacity());
        return storage;
    }

    public static Storage updateStorage(Storage storage, StorageDto storageDto) {
        storage.setCity(storage.getCity());
        storage.setAddress(storageDto.getAddress());
        storage.setMaxCapacity(storageDto.getMaxCapacity());
        return storage;
    }
}
