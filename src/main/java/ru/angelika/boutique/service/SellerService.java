package ru.angelika.boutique.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.angelika.boutique.dto.AuthenticationDto;
import ru.angelika.boutique.dto.UpdateEntityDto;
import org.springframework.transaction.annotation.Transactional;
import ru.angelika.boutique.exception.ResourceExistsException;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.model.Seller;
import ru.angelika.boutique.repository.SellerRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerService {
    private final ItemService itemService;
    private final SellerRepository sellerRepository;
    private final AuthenticationService authenticationService;


    public void add(Seller seller) {
        if (sellerRepository.findByName(seller.getName()) != null) {
            log.error("Seller with name={} already exists", seller.getName());
            throw new ResourceExistsException(Seller.class, seller.getName());
        }
        if (sellerRepository.findByNumber(seller.getNumber()) != null) {
            log.error("Seller with number={} already exists", seller.getNumber());
            throw new ResourceExistsException(Seller.class, seller.getNumber());
        }
        if (sellerRepository.findByEmail(seller.getEmail()) != null) {
            log.error("Seller with email={} already exists", seller.getEmail());
            throw new ResourceExistsException(Seller.class, seller.getEmail());
        }
        seller.setAuthentication(authenticationService.findByNumber(seller.getNumber()));
        sellerRepository.save(seller);
        log.info("Add new seller: name={}, number={}, email={}",
                seller.getName(), seller.getNumber(), seller.getEmail());
    }

    public Seller getById(Long id) {
        log.debug("Get seller by id={}", id);
        return sellerRepository.findById(id).orElseThrow(() -> {
            log.error("Seller not found for get, id={}", id);
            return new ResourceNotFoundException(Seller.class, id);
        });
    }

    public Seller getByNumber(String number) {
        log.debug("Get seller by number={}", number);
        Seller seller = sellerRepository.findByNumber(number);
        if (seller == null) {
            log.error("Seller not found for get, number={}", number);
            throw new ResourceNotFoundException(Seller.class, number);
        }
        return seller;
    }

    public Seller getByName(String name) {
        log.debug("Get seller by name={}", name);
        Seller seller = sellerRepository.findByName(name);
        if (seller == null) {
            log.error("Seller not found for get, name={}", name);
            throw new ResourceNotFoundException(Seller.class, name);
        }
        return seller;
    }

    public List<Seller> getAll() {
        return sellerRepository.findAll();
    }

    @Transactional
    public void update(UpdateEntityDto sellerDto, Long id) {
        Seller seller = sellerRepository.findById(id).orElseThrow(() -> {
            log.error("Seller not found for update, id={}", id);
            return new ResourceNotFoundException(Seller.class, id);
        });
        if (!seller.getName().equals(sellerDto.getName())) {
            if (sellerRepository.findByName(sellerDto.getName()) != null) {
                log.error("Seller with name={} already exists", sellerDto.getName());
                throw new ResourceExistsException(Seller.class, sellerDto.getName());
            }
            seller.setName(sellerDto.getName());
        }
        if (!seller.getNumber().equals(sellerDto.getNumber())) {
            if (sellerRepository.findByNumber(sellerDto.getNumber()) != null) {
                log.error("Seller with number={} already exists", sellerDto.getNumber());
                throw new ResourceExistsException(Seller.class, sellerDto.getNumber());
            }
            seller.setNumber(sellerDto.getNumber());
        }
        if (!seller.getEmail().equals(sellerDto.getEmail())) {
            if (sellerRepository.findByEmail(sellerDto.getEmail()) != null) {
                log.error("Seller with email={} already exists", sellerDto.getEmail());
                throw new ResourceExistsException(Seller.class, sellerDto.getEmail());
            }
            seller.setEmail(sellerDto.getEmail());
        }
        authenticationService.update(AuthenticationDto.builder()
                .oldPassword(sellerDto.getOldPassword())
                .newPassword(sellerDto.getNewPassword())
                .number(sellerDto.getNumber())
                .build(), seller.getAuthentication());
        sellerRepository.save(seller);
        log.info("Update seller by id={}", id);
    }

    public void delete(Long id) {
        Seller seller = sellerRepository.findById(id).orElseThrow(() -> {
            log.error("Seller not found for delete, id={}", id);
            return new ResourceNotFoundException(Seller.class, id);
        });
        authenticationService.delete(seller.getNumber());
        itemService.deleteBySellerId(seller.getId());
        sellerRepository.deleteById(id);
        log.info("Delete seller by id={}", id);
    }

}