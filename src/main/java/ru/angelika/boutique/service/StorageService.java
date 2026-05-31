package ru.angelika.boutique.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.angelika.boutique.dto.StorageDto;
import ru.angelika.boutique.exception.ResourceExistsException;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.mapper.StorageMapper;
import ru.angelika.boutique.model.PickupPoint;
import ru.angelika.boutique.model.Storage;
import ru.angelika.boutique.repository.StorageRepository;

import java.util.List;

/**
 * Сервис для управления складами.
 * Создание, обновление, удаление склада, а также автоматическое переназначение ПВЗ при удалении склада.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {
    private final StorageRepository storageRepository;
    private final ItemsAtStorageService itemsAtStorageService;
    private final PickupPointService pickupPointService;

    /**
     * Добавляет новый склад.
     *
     * @param storageDto DTO с адресом, городом и максимальной вместимостью
     * @throws ResourceExistsException если склад с таким же адресом и городом уже существует
     */
    public void add(StorageDto storageDto) {
        checkDuplicate(storageDto.getAddress(), storageDto.getCity(), null);
        storageRepository.save(StorageMapper.toStorage(storageDto));
        log.info("Add new storage: address={}, city={}, MaxCapacity={}",
                storageDto.getAddress(), storageDto.getCity(), storageDto.getMaxCapacity());
    }

    /**
     * Находит склад по ID.
     *
     * @param id ID склада
     * @return найденный склад
     * @throws ResourceNotFoundException если склад не найден
     */
    public Storage get(Long id) {
        return storageRepository.findById(id).orElseThrow(() -> {
            log.error("Storage not found for get, id={}", id);
            return new ResourceNotFoundException(Storage.class, id);
        });
    }

    /**
     * Возвращает все склады.
     *
     * @return список всех складов
     */
    public List<Storage> getAll() {
        return storageRepository.findAll();
    }

    /**
     * Обновляет данные склада.
     *
     * @param storageDto DTO с новыми данными
     * @param id         ID склада
     * @throws ResourceExistsException   если другой склад с таким же адресом+городом уже существует
     * @throws ResourceNotFoundException если склад не найден
     */
    public void update(StorageDto storageDto, Long id) {
        Storage storage = storageRepository.findById(id).orElseThrow(() -> {
            log.error("Storage not found for update, id={}", id);
            return new ResourceNotFoundException(Storage.class, id);
        });
        checkDuplicate(storageDto.getAddress(), storageDto.getCity(), id);
        storageRepository.save(StorageMapper.updateStorage(storage, storageDto));
        log.info("Update storage by id={}", id);
    }

    /**
     * Удаляет склад.
     * Перед удалением все остатки (ItemsAtStorage) на этом складе удаляются,
     * а привязанные ПВЗ либо переназначаются на другой склад в том же городе, либо удаляются.
     *
     * @param id ID склада
     * @throws ResourceNotFoundException если склад не найден
     */
    public void delete(Long id) {
        storageRepository.findById(id).orElseThrow(() -> {
            log.error("Storage not found for delete, id={}", id);
            return new ResourceNotFoundException(Storage.class, id);
        });
        itemsAtStorageService.getByStorageId(id)
                .forEach(s -> itemsAtStorageService.delete(s.getId()));
        replaceStorage(id);
        storageRepository.deleteById(id);
        log.info("Delete storage by id={}", id);
    }

    /**
     * Переназначает или удаляет ПВЗ, которые были привязаны к удаляемому складу.
     *
     * @param id ID удаляемого склада
     */
    private void replaceStorage(Long id) {
        List<PickupPoint> points = pickupPointService.getAllByStorage(id);
        for (PickupPoint point : points) {
            List<Storage> storages = storageRepository.findByCity(point.getCity());
            if (storages.size() == 0) {
                pickupPointService.delete(point.getId());
            } else {
                storages.stream()
                        .filter(s -> !s.getId().equals(id))
                        .findFirst()
                        .ifPresentOrElse(
                                storage -> {
                                    point.setStorage(storage);
                                    pickupPointService.updateStorage(point);
                                },
                                () -> pickupPointService.delete(point.getId())
                        );
            }
        }
    }

    /**
     * Проверяет, существует ли уже склад с таким же адресом и городом.
     *
     * @param address адрес
     * @param city    город
     * @param id      ID склада (при обновлении – исключить себя)
     * @throws ResourceExistsException если дубликат найден
     */
    private void checkDuplicate(String address, String city, Long id) {
        List<Storage> storagesByAddress = storageRepository.findByAddress(address);
        storagesByAddress.removeIf(storage -> storage.getId().equals(id));
        List<Storage> storagesByCity = storageRepository.findByCity(city);
        storagesByCity.removeIf(storage -> storage.getId().equals(id));
        if (storagesByAddress.size() > 0 && storagesByCity.size() > 0) {
            log.error("Storage with address={}, city={} already exists", address, city);
            throw new ResourceExistsException(Storage.class, city + " : " + address);
        }
    }
}