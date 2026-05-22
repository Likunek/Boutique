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
    private final SupplyTransactionalService service;
    private final PickupPointService pickupPointService;

    public List<Supply> getAllSupplies() {
        return supplyRepository.findAll();
    }

    @Scheduled(cron = "0 0 23 * * ?")
    public void addSupply() {
        List<Order> orders = orderService.getAllByStatus(Status.NEW);
        Set<Long> orderPointIds = orders.stream()
                .map(order -> order.getPoint().getId())
                .collect(Collectors.toSet());
        List<PickupPoint> points = pickupPointService.getAll()
                .stream()
                .filter(point -> orderPointIds.contains(point.getId()))
                .toList();
        points.forEach(point -> handlerPoint(point, orders));
    }


    private void handlerPoint(PickupPoint point, List<Order> orders) {
        CompletableFuture.runAsync(() -> {
            try {
                Supply supply = new Supply();
                List<Order> ordersForPoint = orders.stream()
                        .filter(order -> order.getPoint().getId().equals(point.getId())).toList();
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
        log.info("Create new supply by storageId={}, pointId={}",
                supply.getStorage().getId(), supply.getPoint().getId());
    }
}
