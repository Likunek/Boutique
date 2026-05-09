package ru.angelika.boutique.mapper;

import ru.angelika.boutique.dto.PickupPointDto;
import ru.angelika.boutique.model.PickupPoint;

public class PickupPointMapper {
    public static PickupPoint toPointReceipt(PickupPointDto pickupPointDto) {
        PickupPoint pointReceipt = new PickupPoint();
        pointReceipt.setAddress(pickupPointDto.getAddress());
        pointReceipt.setCity(pickupPointDto.getCity());
        return pointReceipt;
    }

    public static PickupPoint updatePointReceipt(PickupPointDto pickupPointDto, PickupPoint point) {
        point.setAddress(pickupPointDto.getAddress());
        point.setCity(pickupPointDto.getCity());
        return point;
    }
}
