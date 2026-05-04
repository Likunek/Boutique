package ru.angelika.boutique.mapper;

import org.springframework.stereotype.Component;
import ru.angelika.boutique.dto.SellerDto;
import ru.angelika.boutique.model.Seller;

@Component
public class SellerMapper {
    public static Seller toSeller(SellerDto sellerDto) {
        Seller seller = new Seller();
        seller.setName(sellerDto.getName());
        seller.setNumber(sellerDto.getNumber());
        seller.setEmail(sellerDto.getEmail());
        return seller;
    }
}