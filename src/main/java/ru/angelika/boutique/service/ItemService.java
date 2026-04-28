package ru.angelika.boutique.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.angelika.boutique.dto.ItemDto;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.mapper.ItemMapper;
import ru.angelika.boutique.model.Item;
import ru.angelika.boutique.repository.ItemRepository;

@Service
public class ItemService {
    private final ItemRepository itemRepository;

    @Autowired
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public void addItem(ItemDto itemDto) {
        itemRepository.save(ItemMapper.toItem(itemDto));
    }

    public Item getItemById(Long id) {
       return itemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Item.class, id));
    }

    public void updateItem(ItemDto itemDto, Long id) {
        itemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Item.class, id));
        Item item = ItemMapper.toItem(itemDto);
        item.setId(id);
        itemRepository.save(item);
    }

    public void deleteItem(Long id) {
        itemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Item.class, id));
        itemRepository.deleteById(id);
    }
}
