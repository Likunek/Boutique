package ru.angelika.boutique.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.exception.SupplyException;
import ru.angelika.boutique.model.*;
import ru.angelika.boutique.repository.SupplyRepository;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplyService {

    private final SupplyRepository supplyRepository;
    private final OrderService orderService;
    private final PickupPointService pickupPointService;
    private final SupplyTransactionalService service;

    public List<Supply> getAll() {
        return supplyRepository.findAll();
    }

    @Scheduled(cron = "0 0 23 * * ?")
    @Transactional
    public void addSupply() {
        List<Order> orders = orderService.getAllByStatus(Status.NEW);
        Set<PickupPoint> points = orders.stream()
                .map(Order::getPoint)
                .collect(Collectors.toSet());
        List<Long> ordersId = orders.stream().map(Order::getId).toList();
        points.forEach(point -> handlerPoint(point.getId(), ordersId));
    }


    private void handlerPoint(Long pointId, List<Long> ordersId) {
        CompletableFuture.runAsync(() -> {
            try {
                Supply supply = new Supply();
                PickupPoint point = pickupPointService.getWithStorage(pointId);
                List<Order> orders = ordersId.stream().map(orderService::getWithPoint).toList();
                List<Order> ordersForPoint = orders.stream()
                        .filter(order -> order.getPoint().getId().equals(pointId)).toList();
                for (Order order : ordersForPoint) {
                    try {
                        List<Item> items = service.checkCountOnStorage(order, point.getStorage().getId());
                        supply.getItems().addAll(items);
                        orderService.updateStatus(order, Status.WAY);
                    } catch (ResourceNotFoundException e) {
                        log.warn("Order {} skipped for supply: {}", order.getId(), e.getMessage());
                    }
                }
                if (supply.getItems().size() > 0) {
                    saveSupply(supply, point);
                }
            } catch (Exception e) {
                log.error("An error occurred when forming supply, pointId={}", pointId);
                throw new SupplyException("An error occurred when forming supply");
            }
        });
    }

    @Transactional
    private void saveSupply(Supply supply, PickupPoint point) {
        supply.setWeight(supply.getItems().stream().mapToDouble(Item::getWeight).sum());
        supply.setStorage(point.getStorage());
        supply.setPoint(point);
        supplyRepository.save(supply);
        log.info("Create new supply by countItem={}, storageId={}, pointId={}", supply.getItems().size(),
                supply.getStorage().getId(), supply.getPoint().getId());
    }
}
