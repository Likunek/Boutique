package ru.angelika.boutique.mapper;

import org.springframework.stereotype.Component;
import ru.angelika.boutique.dto.UpdateEntityDto;
import ru.angelika.boutique.model.Seller;

@Component
public class SellerMapper {
    public static Seller toSeller(UpdateEntityDto updateEntityDto) {
        Seller seller = new Seller();
        seller.setName(updateEntityDto.getName());
        seller.setNumber(updateEntityDto.getNumber());
        seller.setEmail(updateEntityDto.getEmail());
        return seller;
    }
}