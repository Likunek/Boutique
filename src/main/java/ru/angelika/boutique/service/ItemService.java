package ru.angelika.boutique.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.angelika.boutique.dto.ItemDto;
import ru.angelika.boutique.exception.ItemExistsException;
import ru.angelika.boutique.exception.ItemNotFoundException;
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
        if (itemRepository.findByName(itemDto.getName()) != null) {
            throw new ItemExistsException("the item already exists");
        }
        itemRepository.save(ItemMapper.toItem(itemDto));
    }

    public Item getItemByName(String name) {
        Item item = itemRepository.findByName(name);
        if (item == null) {
            throw new ItemNotFoundException("the item not found");
        }
        return item;
    }

    public void updateItem(ItemDto itemDto, Long id) {
        Item item = itemRepository.findById(id).orElseThrow(() -> new ItemNotFoundException("tne item not found"));
        if (item.getName().equals(itemDto.getName())) {
            throw new ItemExistsException("the item with name already exists");
        }
        itemRepository.save(ItemMapper.toItem(itemDto));
    }

    public void deleteItem(Long id) {
        itemRepository.findById(id).orElseThrow(() -> new ItemNotFoundException("tne item not found"));
        itemRepository.deleteById(id);
    }
}
