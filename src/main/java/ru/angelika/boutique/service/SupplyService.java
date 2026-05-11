package ru.angelika.boutique.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.*;
import ru.angelika.boutique.repository.SupplyRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SupplyService {

    private final SupplyRepository supplyRepository;
    private final OrderService orderService;
    private final SupplyTransactionalService service;
    private final PickupPointService pickupPointService;

    @Autowired
    public SupplyService(SupplyRepository supplyRepository, OrderService orderService,
                         SupplyTransactionalService service, PickupPointService pickupPointService) {
        this.supplyRepository = supplyRepository;
        this.orderService = orderService;
        this.service = service;
        this.pickupPointService = pickupPointService;
    }

    public List<Supply> getAllSupplies() {
        return supplyRepository.findAll();
    }

    @Transactional
    @Scheduled(cron = "0 0 23 * * ?")
    public void addSupply() {
        List<Order> orders = orderService.getOrdersByStatus(Status.NEW);
        Set<Long> orderPointIds = orders.stream()
                .map(order -> order.getPoint().getId())
                .collect(Collectors.toSet());
        List<PickupPoint> points = pickupPointService.getAllPoints()
                .stream()
                .filter(point -> orderPointIds.contains(point.getId()))
                .toList();
        for (PickupPoint point : points) {
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
                supply.setWeight(supply.getItems().stream().mapToDouble(Item::getWeight).sum());
                supply.setStorage(point.getStorage());
                supply.setPoint(point);
                supplyRepository.save(supply);
                log.info("Create new supply by storageId={}, pointId={}",
                        supply.getStorage().getId(), supply.getPoint().getId());
            }
        }
    }


}
