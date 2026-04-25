package ru.angelika.boutique.service;

import org.springframework.stereotype.Service;
import ru.angelika.boutique.dto.ItemCardDto;
import ru.angelika.boutique.exception.ResourceNotFoundException;
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
        return itemCardRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(ItemCard.class, id));
    }

    public void updateItemCard(ItemCardDto itemCardDto, Long id) {
        itemCardRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(ItemCard.class, id));
        itemCardRepository.save(ItemCardMapper.toItemCard(itemCardDto));
    }
    public void deleteItemCard(Long id) {
        itemCardRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(ItemCard.class, id));
        itemCardRepository.deleteById(id);
    }
}
