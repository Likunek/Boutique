package ru.angelika.boutique.service;

import org.springframework.stereotype.Service;
import ru.angelika.boutique.dto.ItemCardDto;
import ru.angelika.boutique.exception.ItemCardNotFoundException;
import ru.angelika.boutique.mapper.ItemCardMapper;
import ru.angelika.boutique.model.ItemCard;
import ru.angelika.boutique.repository.ItemCardRepository;

@Service
public class ItemCardService {
    private final ItemCardRepository itemCardRepository;

    public ItemCardService(ItemCardRepository itemCardRepository) {
        this.itemCardRepository = itemCardRepository;
    }

    public void addItemCard(ItemCardDto itemCardDto) {
        itemCardRepository.save(ItemCardMapper.toItemCard(itemCardDto));
    }

    public ItemCard getItemCard(Long id) {
        return itemCardRepository.findById(id).orElseThrow(() -> new ItemCardNotFoundException("the itemCard not found"));
    }

    public void updateItemCard(ItemCardDto itemCardDto, Long id) {
        itemCardRepository.findById(id).orElseThrow(() -> new ItemCardNotFoundException("the itemCard not found"));
        itemCardRepository.save(ItemCardMapper.toItemCard(itemCardDto));
    }
    public void deleteItemCard(Long id) {
        itemCardRepository.findById(id).orElseThrow(() -> new ItemCardNotFoundException("the itemCard not found"));
        itemCardRepository.deleteById(id);
    }
}
