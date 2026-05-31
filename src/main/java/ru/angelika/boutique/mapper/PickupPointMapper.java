package ru.angelika.boutique.mapper;

import ru.angelika.boutique.dto.PickupPointDto;
import ru.angelika.boutique.model.PickupPoint;
import ru.angelika.boutique.model.Storage;

/**
 * Маппер для преобразования DTO в сущность {@link PickupPoint} и обновления существующего ПВЗ.
 */
public class PickupPointMapper {

    /**
     * Создаёт новый пункт выдачи заказов.
     *
     * @param pickupPointDto DTO с адресом, городом
     * @param storage        склад, обслуживающий этот ПВЗ
     * @return заполненный объект {@link PickupPoint}
     */
    public static PickupPoint toPointReceipt(PickupPointDto pickupPointDto, Storage storage) {
        PickupPoint pointReceipt = new PickupPoint();
        pointReceipt.setAddress(pickupPointDto.getAddress());
        pointReceipt.setCity(pickupPointDto.getCity());
        pointReceipt.setStorage(storage);
        return pointReceipt;
    }

    /**
     * Обновляет существующий пункт выдачи заказов.
     *
     * @param pickupPointDto DTO с новыми данными
     * @param point          целевая сущность ПВЗ
     * @param storage        новый склад (может быть тем же или другим)
     * @return обновлённый объект {@link PickupPoint}
     */
    public static PickupPoint updatePointReceipt(PickupPointDto pickupPointDto, PickupPoint point, Storage storage) {
        point.setAddress(pickupPointDto.getAddress());
        point.setCity(pickupPointDto.getCity());
        point.setStorage(storage);
        return point;
    }
}