package ru.angelika.boutique.mapper;

import ru.angelika.boutique.dto.PickupPointDto;
import ru.angelika.boutique.model.PickupPoint;
import ru.angelika.boutique.model.Storage;

public class PickupPointMapper {
    public static PickupPoint toPointReceipt(PickupPointDto pickupPointDto, Storage storage) {
        PickupPoint pointReceipt = new PickupPoint();
        pointReceipt.setAddress(pickupPointDto.getAddress());
        pointReceipt.setCity(pickupPointDto.getCity());
        pointReceipt.setStorage(storage);
        return pointReceipt;
    }

    public static PickupPoint updatePointReceipt(PickupPointDto pickupPointDto, PickupPoint point, Storage storage) {
        point.setAddress(pickupPointDto.getAddress());
        point.setCity(pickupPointDto.getCity());
        point.setStorage(storage);
        return point;
    }
}
