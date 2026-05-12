package ru.angelika.boutique.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.angelika.boutique.dto.FeedbackDto;
import ru.angelika.boutique.dto.ItemCardDto;
import ru.angelika.boutique.dto.ItemCardUpdateDto;
import ru.angelika.boutique.exception.ResourceExistsException;
import ru.angelika.boutique.exception.ResourceNotFoundException;
import ru.angelika.boutique.mapper.FeedbackMapper;
import ru.angelika.boutique.mapper.ItemCardMapper;
import ru.angelika.boutique.model.Feedback;
import ru.angelika.boutique.model.Item;
import ru.angelika.boutique.model.ItemCard;
import ru.angelika.boutique.repository.ItemCardRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemCardService {
    private final ItemCardRepository itemCardRepository;
    private final ItemService itemService;

    public void addItemCard(ItemCardDto itemCardDto, String seller) {
        checkDuplicate(seller, itemCardDto.getName(), itemCardDto.getDescription(), null);
        Item item = itemService.getItemById(itemCardDto.getItemId());
        if (item.getItemCard() != null) {
            log.error("ItemCard by itemId={} already exists", item.getId());
            throw new ResourceExistsException(ItemCard.class, " itemId : " + item.getId());
        }
        ItemCard itemCard = itemCardRepository.save(ItemCardMapper.toItemCard(itemCardDto, item.getCostPrice(), seller));
        item.setItemCard(itemCard);
        itemService.addCardItem(item);
        log.info("Add new ItemCard: name={}, description={}, itemId={}",
                itemCardDto.getName(), itemCardDto.getDescription(), itemCardDto.getItemId());
    }

    public void addFeedback(FeedbackDto feedbackDto, Long id) {
        ItemCard itemCard = getItemCard(id);
        itemCard.getFeedbacks().add(FeedbackMapper.toFeedback(feedbackDto));
        Double rating = itemCard.getFeedbacks().stream().mapToDouble(Feedback::getRating).average().orElse(0.0);
        itemCard.setRating(rating);
        itemCardRepository.save(itemCard);
        log.info("Added new feedback, update rating={} itemCard by id={}", rating, id);
    }

    public ItemCard getItemCard(Long id) {
        log.debug("Get itemCard by id={}", id);
        return itemCardRepository.findById(id).orElseThrow(() -> {
            log.error("ItemCard not found for get, id={}", id);
            return new ResourceNotFoundException(ItemCard.class, id);
        });
    }

    public Page<ItemCard> getAllItemCard(int page, int size) {
        return itemCardRepository.findAll(PageRequest.of(page, size));
    }

    public Page<ItemCard> getAllItemCardBySearch(int page, int size, String text) {
        return itemCardRepository.findByNameOrDescription(text.toLowerCase(), PageRequest.of(page, size));
    }

    public Page<ItemCard> getAllItemCardBySeller(int page, int size, String seller) {
        return itemCardRepository.findBySeller(seller, PageRequest.of(page, size));
    }

    public void updateItemCard(ItemCardUpdateDto itemCardDto, Long id, String seller) {
        ItemCard itemCard = getItemCard(id);
        checkDuplicate(seller, itemCardDto.getName(), itemCardDto.getDescription(), id);
        ItemCardMapper.toItemCardUpdate(itemCardDto, itemCard);
        itemCardRepository.save(itemCard);
        log.info("Update itemCard by id={}", id);
    }

    public void deleteItemCard(Long id) {
        itemCardRepository.findById(id).orElseThrow(() -> {
            log.error("ItemCard not found for delete, id={}", id);
            return new ResourceNotFoundException(ItemCard.class, id);
        });
        itemService.deleteItemCard(id);
        itemCardRepository.deleteById(id);
        log.info("Delete itemCard by id={}", id);
    }

    private void checkDuplicate(String seller, String name, String description, Long id) {
        List<ItemCard> nameDuplicate = itemCardRepository.findByNameAndSeller(name, seller);
        nameDuplicate.removeIf(itemCard -> itemCard.getId().equals(id));
        List<ItemCard> descriptionDuplicate = itemCardRepository.findByDescriptionAndSeller(description, seller);
        descriptionDuplicate.removeIf(itemCard -> itemCard.getId().equals(id));
        if (nameDuplicate.size() > 0 && descriptionDuplicate.size() > 0) {
            log.error("ItemCard by seller={}, with name={}, description={} already exists", seller, name, description);
            throw new ResourceExistsException(ItemCard.class, name + " : " + description);
        }
    }

}
