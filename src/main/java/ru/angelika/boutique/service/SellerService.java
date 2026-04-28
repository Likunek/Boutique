package ru.angelika.boutique.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import ru.angelika.boutique.dto.SellerDto;
import ru.angelika.boutique.exception.ResourceExistsException;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.mapper.SellerMapper;
import ru.angelika.boutique.model.Seller;
import ru.angelika.boutique.repository.SellerRepository;

import java.util.Collection;
import java.util.List;

@Service
public class SellerService implements UserDetailsService {
    private final SellerRepository sellerRepository;
    private final SellerMapper sellerMapper;

    @Autowired
    public SellerService(SellerRepository sellerRepository, SellerMapper sellerMapper) {
        this.sellerRepository = sellerRepository;
        this.sellerMapper = sellerMapper;
    }

    public void addSeller(SellerDto seller) {
        if (sellerRepository.findByName(seller.getName()) != null) {
            throw new ResourceExistsException(Seller.class, seller.getName());
        }
        if (sellerRepository.findByNumber(seller.getNumber()) != null) {
            throw new ResourceExistsException(Seller.class, seller.getNumber());
        }
        if (sellerRepository.findByEmail(seller.getEmail()) != null) {
            throw new ResourceExistsException(Seller.class, seller.getEmail());
        }
        sellerRepository.save(sellerMapper.toSeller(seller));
    }

    public Seller getBySellerName(String name) {
        Seller seller = sellerRepository.findByName(name);
        if (seller == null) {
            throw new ResourceNotFoundException(Seller.class, name);
        }
        return seller;
    }

    public void updateSeller(SellerDto sellerDto, Long id) {
        Seller seller = sellerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Seller.class, id));

        if (!seller.getName().equals(sellerDto.getName())) {
            if (sellerRepository.findByName(sellerDto.getName()) != null) {
                throw new ResourceExistsException(Seller.class, sellerDto.getName());
            }
            seller.setName(sellerDto.getName());
        }
        if (!seller.getNumber().equals(sellerDto.getNumber())) {
            if (sellerRepository.findByNumber(sellerDto.getNumber()) != null)
                throw new ResourceExistsException(Seller.class, sellerDto.getNumber());
            seller.setNumber(sellerDto.getNumber());
        }
        if (!seller.getEmail().equals(sellerDto.getEmail())) {
            if (sellerRepository.findByEmail(sellerDto.getEmail()) != null) {
                throw new ResourceExistsException(Seller.class, sellerDto.getEmail());
            }
            seller.setEmail(sellerDto.getEmail());
        }
        seller.setPassword(sellerDto.getPassword());
    }

    public void deleteSeller(Long id) {
        sellerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Seller.class, id));
        sellerRepository.deleteById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Seller seller = getBySellerName(username);
        return new User(seller.getName(), seller.getPassword(), extractRoles(seller));
    }
    private Collection<? extends GrantedAuthority> extractRoles(Seller seller) {
        return List.of(new SimpleGrantedAuthority("ROLE_" + seller.getRole()));
    }
}