package ru.angelika.boutique.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import ru.angelika.boutique.model.User;
import ru.angelika.boutique.repository.ItemCardRepository;

import java.util.List;

/**
 * Сервис для управления товарными карточками (ItemCard).
 * Обеспечивает создание, поиск, обновление, удаление, добавление отзывов.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemCardService {
    private final ItemCardRepository itemCardRepository;
    private final UserService userService;
    private final ItemService itemService;

    /**
     * Создаёт новую товарную карточку на основе DTO и имени продавца.
     *
     * @param itemCardDto DTO с данными карточки (itemId, name, description)
     * @param seller      имя продавца (будет сохранено в поле seller карточки)
     * @throws ResourceExistsException если карточка с таким названием/описанием уже существует у данного продавца,
     *                                 или если для указанного Item уже есть карточка
     */
    public void add(ItemCardDto itemCardDto, String seller) {
        checkDuplicate(seller, itemCardDto.getName(), itemCardDto.getDescription(), null);
        Item item = itemService.getById(itemCardDto.getItemId());
        if (item.getItemCard() != null) {
            log.error("ItemCard by itemId={} already exists", item.getId());
            throw new ResourceExistsException(ItemCard.class, " itemId : " + item.getId());
        }
        ItemCard itemCard = itemCardRepository.save(ItemCardMapper.toItemCard(itemCardDto, item.getCostPrice(), seller));
        item.setItemCard(itemCard);
        itemService.addItemCard(item);
        log.info("Add new ItemCard: name={}, description={}, itemId={}",
                itemCardDto.getName(), itemCardDto.getDescription(), itemCardDto.getItemId());
    }

    /**
     * Добавляет отзыв к товарной карточке и пересчитывает её рейтинг.
     *
     * @param feedbackDto DTO с данными отзыва (userId, rating, text)
     * @param id          ID товарной карточки
     * @throws ResourceNotFoundException если карточка или пользователь не найдены
     */
    public void addFeedback(FeedbackDto feedbackDto, Long id) {
        ItemCard itemCard = get(id);
        User user = userService.getById(feedbackDto.getUserId());
        itemCard.getFeedbacks().add(FeedbackMapper.toFeedback(feedbackDto, user));
        Double rating = itemCard.getFeedbacks().stream().mapToDouble(Feedback::getRating).average().orElse(0.0);
        itemCard.setRating(rating);
        itemCardRepository.save(itemCard);
        log.info("Added new feedback, update rating={} itemCard by id={}", rating, id);
    }

    /**
     * Находит товарную карточку по ID.
     *
     * @param id ID карточки
     * @return найденная карточка
     * @throws ResourceNotFoundException если карточка не найдена
     */
    public ItemCard get(Long id) {
        log.debug("Get itemCard by id={}", id);
        return itemCardRepository.findById(id).orElseThrow(() -> {
            log.error("ItemCard not found for get, id={}", id);
            return new ResourceNotFoundException(ItemCard.class, id);
        });
    }

    /**
     * Возвращает страницу всех товарных карточек с поддержкой пагинации.
     *
     * @param pageable параметры пагинации
     * @return страница карточек
     */
    public Page<ItemCard> getAll(Pageable pageable) {
        return itemCardRepository.findAll(pageable);
    }

    /**
     * Поиск карточек по подстроке в названии или описании (без учёта регистра).
     *
     * @param pageable параметры пагинации
     * @param text     искомая подстрока
     * @return страница найденных карточек
     */
    public Page<ItemCard> getAllBySearch(Pageable pageable, String text) {
        return itemCardRepository.findByNameOrDescription(text.toLowerCase(), pageable);
    }

    /**
     * Возвращает страницу карточек, принадлежащих указанному продавцу.
     *
     * @param pageable параметры пагинации
     * @param seller   имя продавца
     * @return страница карточек
     */
    public Page<ItemCard> getAllBySeller(Pageable pageable, String seller) {
        return itemCardRepository.findBySeller(seller, pageable);
    }

    /**
     * Обновляет название и описание товарной карточки.
     *
     * @param itemCardDto DTO с новыми данными
     * @param id          ID карточки
     * @param seller      имя продавца (для проверки дубликатов)
     * @throws ResourceExistsException   если новое имя+описание уже заняты этим продавцом
     * @throws ResourceNotFoundException если карточка не найдена
     */
    public void update(ItemCardUpdateDto itemCardDto, Long id, String seller) {
        ItemCard itemCard = get(id);
        checkDuplicate(seller, itemCardDto.getName(), itemCardDto.getDescription(), id);
        ItemCardMapper.toItemCardUpdate(itemCardDto, itemCard);
        itemCardRepository.save(itemCard);
        log.info("Update itemCard by id={}", id);
    }

    /**
     * Удаляет товарную карточку.
     *
     * @param id ID карточки
     * @throws ResourceNotFoundException если карточка не найдена
     */
    public void delete(Long id) {
        get(id);
        itemService.deleteItemCard(id);
        itemCardRepository.deleteById(id);
        log.info("Delete itemCard by id={}", id);
    }

    /**
     * Проверяет, не существует ли уже карточки с такими же названием и описанием у того же продавца.
     *
     * @param seller      имя продавца
     * @param name        название
     * @param description описание
     * @param id          ID карточки (при обновлении – исключить саму себя, может быть null)
     * @throws ResourceExistsException если дубликат найден
     */
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