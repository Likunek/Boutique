package ru.angelika.boutique.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.angelika.boutique.dto.PickupPointDto;
import ru.angelika.boutique.exception.ResourceExistsException;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.mapper.PickupPointMapper;
import ru.angelika.boutique.model.PickupPoint;
import ru.angelika.boutique.model.Storage;
import ru.angelika.boutique.repository.OrderRepository;
import ru.angelika.boutique.repository.PickupPointRepository;
import ru.angelika.boutique.repository.StorageRepository;

import java.util.List;

/**
 * Сервис для управления пунктами выдачи заказов (ПВЗ).
 * Создание, обновление, удаление ПВЗ, поиск по складу, городу, адресу.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PickupPointService {
    private final PickupPointRepository pickupPointRepository;
    private final OrderRepository orderRepository;
    private final StorageRepository storageRepository;

    /**
     * Добавляет новый пункт выдачи.
     *
     * @param pickupPointDto DTO с адресом, городом и ID склада
     * @throws ResourceExistsException   если ПВЗ с таким адресом и городом уже существует
     * @throws ResourceNotFoundException если указанный склад не найден
     */
    public void add(PickupPointDto pickupPointDto) {
        checkDuplicate(pickupPointDto.getAddress(), pickupPointDto.getCity(), null);
        Storage storage = storageRepository.findById(pickupPointDto.getStorageId()).orElseThrow(() -> {
            log.error("Storage not found for delete, id={}", pickupPointDto.getStorageId());
            return new ResourceNotFoundException(Storage.class, pickupPointDto.getStorageId());
        });
        pickupPointRepository.save(PickupPointMapper.toPointReceipt(pickupPointDto, storage));
        log.info("Add new pointReceipt: address={}, city={}",
                pickupPointDto.getAddress(), pickupPointDto.getCity());
    }

    /**
     * Находит ПВЗ по ID.
     *
     * @param id ID ПВЗ
     * @return найденный ПВЗ
     * @throws ResourceNotFoundException если ПВЗ не найден
     */
    public PickupPoint get(Long id) {
        return pickupPointRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("PickupPoint not found for get, id={}", id);
                    return new ResourceNotFoundException(PickupPoint.class, id);
                });
    }

    /**
     * Находит ПВЗ по ID с предварительной загрузкой связанного склада.
     *
     * @param id ID ПВЗ
     * @return ПВЗ с инициализированным полем storage
     * @throws ResourceNotFoundException если ПВЗ не найден
     */
    @Transactional
    public PickupPoint getWithStorage(Long id) {
        PickupPoint point = pickupPointRepository.findByIdWithStorage(id);
        if (point == null) {
            log.error("PickupPoint not found for get with Storage, id={}", id);
            throw new ResourceNotFoundException(PickupPoint.class, id);
        }
        return point;
    }

    /**
     * Возвращает все ПВЗ, привязанные к указанному складу.
     *
     * @param id ID склада
     * @return список ПВЗ
     */
    public List<PickupPoint> getAllByStorage(Long id) {
        return pickupPointRepository.findByStorageId(id);
    }

    /**
     * Возвращает все ПВЗ.
     *
     * @return список всех ПВЗ
     */
    public List<PickupPoint> getAll() {
        return pickupPointRepository.findAll();
    }

    /**
     * Обновляет данные ПВЗ.
     *
     * @param id             ID ПВЗ
     * @param pickupPointDto DTO с новыми данными
     * @throws ResourceExistsException   если другой ПВЗ с таким же адресом+городом уже существует
     * @throws ResourceNotFoundException если ПВЗ или склад не найдены
     */
    public void update(Long id, PickupPointDto pickupPointDto) {
        PickupPoint pointReceipt = pickupPointRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("PointReceipt not found for update, id={}", id);
                    return new ResourceNotFoundException(PickupPoint.class, id);
                });
        Storage storage = storageRepository.findById(pickupPointDto.getStorageId()).orElseThrow(() -> {
            log.error("Storage not found for delete, id={}", pickupPointDto.getStorageId());
            return new ResourceNotFoundException(Storage.class, pickupPointDto.getStorageId());
        });
        checkDuplicate(pickupPointDto.getAddress(), pickupPointDto.getCity(), id);
        pickupPointRepository.save(PickupPointMapper.updatePointReceipt(pickupPointDto, pointReceipt, storage));
        log.info("Update point by id={}", id);
    }

    /**
     * Обновляет только склад, привязанный к ПВЗ (используется при переназначении склада).
     *
     * @param point ПВЗ (уже изменённый в коде)
     */
    public void updateStorage(PickupPoint point) {
        pickupPointRepository.save(point);
        log.info("Update point by id={} with storage={}", point.getId(),
                point.getStorage().getCity() + " " + point.getStorage().getAddress());
    }

    /**
     * Удаляет ПВЗ и все связанные с ним заказы.
     *
     * @param id ID ПВЗ
     * @throws ResourceNotFoundException если ПВЗ не найден
     */
    public void delete(Long id) {
        pickupPointRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("PointReceipt not found for delete, id={}", id);
                    return new ResourceNotFoundException(PickupPoint.class, id);
                });
        orderRepository.deleteAll(orderRepository.findByPointId(id));
        pickupPointRepository.deleteById(id);
        log.info("Delete pointReceipt by id={}", id);
    }

    /**
     * Проверяет, существует ли уже ПВЗ с таким же адресом и городом.
     *
     * @param address адрес
     * @param city    город
     * @param id      ID ПВЗ (при обновлении – исключить себя)
     * @throws ResourceExistsException если дубликат найден
     */
    private void checkDuplicate(String address, String city, Long id) {
        List<PickupPoint> pointsByAddress = pickupPointRepository.findByAddress(address);
        pointsByAddress.removeIf(point -> point.getId().equals(id));
        List<PickupPoint> pointsByCity = pickupPointRepository.findByCity(city);
        pointsByCity.removeIf(point -> point.getId().equals(id));
        if (pointsByAddress.size() > 0 && pointsByCity.size() > 0) {
            log.error("PointReceipt with address={}, city={} already exists", address, city);
            throw new ResourceExistsException(PickupPoint.class, city + " : " + address);
        }
    }
}