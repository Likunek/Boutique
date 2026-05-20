package ru.angelika.boutique.controller.view;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.angelika.boutique.dto.FeedbackDto;
import ru.angelika.boutique.dto.ItemCardDto;
import ru.angelika.boutique.dto.ItemCardUpdateDto;
import ru.angelika.boutique.model.Item;
import ru.angelika.boutique.model.ItemCard;
import ru.angelika.boutique.model.Seller;
import ru.angelika.boutique.service.ItemCardService;
import ru.angelika.boutique.service.ItemService;
import ru.angelika.boutique.service.SellerService;
import ru.angelika.boutique.service.UserService;

import java.util.List;

@Slf4j
@Controller
@RequestMapping
@RequiredArgsConstructor
public class ItemCardController {
    private final ItemService itemService;
    private final UserService userService;
    private final SellerService sellerService;
    private final ItemCardService itemCardService;


    @GetMapping("/seller/add-card")
    public String getFormNewCard(Model model) {
        Seller seller = getAuthSeller();
        List<Item> items = itemService.getBySellerId(seller.getId());
        model.addAttribute("items", items);
        model.addAttribute("id", seller.getId());
        return "add-card";
    }

    @PostMapping("/seller/add-card")
    public String addItemCard(@Valid ItemCardDto itemCardDto, Model model) {
        try {
            Seller seller = getAuthSeller();
            itemCardService.add(itemCardDto, seller.getName());
            List<Item> items = itemService.getBySellerId(seller.getId());
            model.addAttribute("items", items);
            model.addAttribute("id", seller.getId());
            model.addAttribute("successMessage", "Card successfully add!");
        } catch (Exception e) {
            log.error("Error add itemCard '{}': {}", itemCardDto.getName(), e.getMessage(), e);
            model.addAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "add-card";
    }

    @PutMapping("/seller/add-card/{id}")
    public String updateCard(@PathVariable Long id, @RequestParam Long itemId,
                             @Valid ItemCardUpdateDto itemCardUpdateDto, RedirectAttributes redirectAttributes) {
        try {
            String sellerName = getAuthSeller().getName();
            if (!itemService.getById(itemId).getSeller().getName().equals(sellerName)) {
                log.warn("Seller with name={} tried to update itemCard with id={} from someone else's path", sellerName, itemId);
                return "redirect:/welcome";
            }
            itemCardService.update(itemCardUpdateDto, id, sellerName);
            redirectAttributes.addFlashAttribute("successMessage", "Card successfully update!");
        } catch (Exception e) {
            log.error("Error update itemCard id {} : {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/seller/items/" + itemId;
    }

    @PostMapping("/user/item-card/feedback/{id}")
    public String addFeedback(@PathVariable Long id, @Valid FeedbackDto feedbackDto, @RequestParam Long userId) {
        itemCardService.addFeedback(feedbackDto, id);
        return "redirect:/user/profile/" + userId;
    }

    @GetMapping("/item-card")
    public String getAllItemCard(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "12") int size, @RequestParam String role,
                                 @RequestParam(defaultValue = "false") boolean seller,
                                 @RequestParam(required = false) Long sellerId,
                                 @RequestParam(defaultValue = "") String search,
                                 @RequestParam(defaultValue = "id") String sortBy,
                                 @RequestParam(defaultValue = "asc") String sort, Model model) {
        Sort.Direction direction  = sort.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        model.addAttribute("seller", seller);
        model.addAttribute("searchQuery", search);
        if (role.equals("ROLE_USER")) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            model.addAttribute("cardsId", userService.getCardsId(auth.getName()));
        }
        if (seller) {
            String sellerName = sellerService.getById(sellerId).getName();
            model.addAttribute("itemCardsPage", itemCardService.getAllBySeller(pageable, sellerName));
            model.addAttribute("role", role);
            model.addAttribute("name", "by " + sellerName);
            return "all-cards";
        }
        if (!search.isBlank()) {
            model.addAttribute("itemCardsPage", itemCardService.getAllBySearch(pageable, search));
            model.addAttribute("role", role);
            return "all-cards";
        }
        model.addAttribute("itemCardsPage", itemCardService.getAll(pageable));
        model.addAttribute("role", role);
        return "all-cards";
    }

    @GetMapping("/item-card/{id}")
    public String getItemCardById(@PathVariable Long id, @RequestParam String role, Model model) {
        boolean isOwner = false;
        ItemCard itemCard = itemCardService.get(id);
        switch (role) {
            case "ROLE_SELLER" -> isOwner = getAuthSeller().getName().equals(itemCard.getSeller());
            case "ROLE_ADMIN" -> isOwner = true;
            case "ROLE_USER" -> {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                model.addAttribute("cardsId", userService.getCardsId(auth.getName()));
            }
        }
        Long sellerId = sellerService.getByName(itemCard.getSeller()).getId();
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("role", role);
        model.addAttribute("itemCard", itemCard);
        model.addAttribute("sellerId", sellerId);
        return "item-card";
    }

    @DeleteMapping("/item-card/{id}")
    public String deleteItemCardById(@PathVariable Long id, @RequestParam String role, Model model) {
        ItemCard itemCard = itemCardService.get(id);
        if (role.equals("ROLE_ADMIN") || security(itemCard.getSeller())) {
            itemCardService.delete(id);
        }
        return "redirect:/welcome";
    }

    private Seller getAuthSeller() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return sellerService.getByNumber(auth.getName());
    }

    private boolean security(String name) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String roleName = auth.getAuthorities().iterator().next().getAuthority();
        if (roleName.equals("ROLE_SELLER")) {
            return sellerService.getByNumber(auth.getName()).getName().equals(name);
        }
        return false;
    }

}
