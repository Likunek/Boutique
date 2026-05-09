package ru.angelika.boutique.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.angelika.boutique.model.Supply;
import ru.angelika.boutique.repository.SupplyRepository;

import java.util.List;

@Slf4j
@Service
public class SupplyService {
    private final SupplyRepository supplyRepository;
    @Autowired
    public SupplyService(SupplyRepository supplyRepository) {
        this.supplyRepository = supplyRepository;
    }

    public List<Supply> getAllSupplies() {
        return supplyRepository.findAll();
    }
}
