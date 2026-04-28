package ru.angelika.boutique.mapper;

import ru.angelika.boutique.dto.PointReceiptDto;
import ru.angelika.boutique.model.PointReceipt;

public class PointReceiptMapper {
    public static PointReceipt toPointReceipt(PointReceiptDto pointReceiptDto) {
        PointReceipt pointReceipt = new PointReceipt();
        pointReceipt.setAddress(pointReceipt.getAddress());
        pointReceipt.setDescription(pointReceipt.getDescription());
        return pointReceipt;
    }
}
