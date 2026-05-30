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

@Slf4j
@Service
@RequiredArgsConstructor
public class PickupPointService {
    private final PickupPointRepository pickupPointRepository;
    private final OrderRepository orderRepository;
    private final StorageRepository storageRepository;


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

    public PickupPoint get(Long id) {
        return pickupPointRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("PickupPoint not found for get, id={}", id);
                    return new ResourceNotFoundException(PickupPoint.class, id);
                });
    }

    @Transactional
    public PickupPoint getWithStorage(Long id) {
        PickupPoint point = pickupPointRepository.findByIdWithStorage(id);
        if (point == null) {
            log.error("PickupPoint not found for get with Storage, id={}", id);
            throw new ResourceNotFoundException(PickupPoint.class, id);
        }
        return point;
    }

    public List<PickupPoint> getAllByStorage(Long id) {
        return pickupPointRepository.findByStorageId(id);
    }

    public List<PickupPoint> getAll() {
        return pickupPointRepository.findAll();
    }

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

    public void updateStorage(PickupPoint point) {
        pickupPointRepository.save(point);
        log.info("Update point by id={} with storage={}", point.getId(),
                point.getStorage().getCity() + " " + point.getStorage().getAddress());
    }

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

    private void checkDuplicate(String address, String city, Long id) {
        List<PickupPoint> pointsByAddress = pickupPointRepository.findByAddress(address);
        pointsByAddress.removeIf(point -> point.getId().equals(id));
        List<PickupPoint> pointsByCity = pickupPointRepository.findByCity(city);
        pointsByCity.removeIf(point -> point.getId().equals(id));
        if (pointsByAddress.size() > 0 && pointsByCity.size() > 0) {
            log.error("PointReceipt with address={}, city={} already exists", address, city);
            throw new ResourceExistsException(PickupPoint.class,  city + " : " + address);
        }
    }
}
