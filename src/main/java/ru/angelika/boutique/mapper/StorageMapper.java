package ru.angelika.boutique.mapper;

import ru.angelika.boutique.dto.StorageDto;
import ru.angelika.boutique.model.Storage;

/**
 * Маппер для создания и обновления сущности {@link Storage}.
 */
public class StorageMapper {

    /**
     * Создаёт новый склад на основе DTO.
     * <p>Также устанавливает кол-во свободного места на данный момент (currentCapacity) равной максимальной вместимости.</p>
     *
     * @param storageDto DTO с адресом, городом и максимальной вместимостью
     * @return заполненный объект {@link Storage}
     */
    public static Storage toStorage(StorageDto storageDto) {
        Storage storage = new Storage();
        storage.setAddress(storageDto.getAddress());
        storage.setCity(storageDto.getCity());
        storage.setMaxCapacity(storageDto.getMaxCapacity());
        storage.setCurrentCapacity(storageDto.getMaxCapacity().doubleValue());
        return storage;
    }

    /**
     * Обновляет существующий склад (адрес, вместимость, город).
     *
     * @param storage    целевая сущность склада
     * @param storageDto DTO с новыми значениями
     * @return обновлённый объект {@link Storage}
     */
    public static Storage updateStorage(Storage storage, StorageDto storageDto) {
        storage.setCity(storageDto.getCity());
        storage.setAddress(storageDto.getAddress());
        storage.setMaxCapacity(storageDto.getMaxCapacity());
        return storage;
    }
}