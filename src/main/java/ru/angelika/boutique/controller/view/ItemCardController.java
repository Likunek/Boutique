package ru.angelika.boutique.controller.view;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
public class ItemCardController {
    private final ItemService itemService;
    private final UserService userService;
    private final SellerService sellerService;
    private final ItemCardService itemCardService;

    @Autowired
    public ItemCardController(ItemService itemService, UserService userService,
                              SellerService sellerService, ItemCardService itemCardService) {
        this.itemService = itemService;
        this.userService = userService;
        this.sellerService = sellerService;
        this.itemCardService = itemCardService;
    }

    @GetMapping("/seller/add-card")
    public String getFormNewCard(Model model) {
        Seller seller = getAuthSeller();
        List<Item> items = itemService.getItemBySellerId(seller.getId());
        model.addAttribute("items", items);
        model.addAttribute("id", seller.getId());
        return "add-card";
    }

    @PostMapping("/seller/add-card")
    public String addItemCard(@Valid ItemCardDto itemCardDto, Model model) {
        try {
            Seller seller = getAuthSeller();
            itemCardService.addItemCard(itemCardDto, seller.getName());
            List<Item> items = itemService.getItemBySellerId(seller.getId());
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
            if (!security(sellerName)) {
                return "redirect:/welcome";
            }
            itemCardService.updateItemCard(itemCardUpdateDto, id, sellerName);
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
                                 @RequestParam(defaultValue = "") String search,
                                 @RequestParam(defaultValue = "0") Long sellerId, Model model) {
        if (role.equals("ROLE_USER")) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            model.addAttribute("cardsId", userService.getCardsId(auth.getName()));
        }
        model.addAttribute("seller", seller);
        if (seller) {
            String sellerName = sellerService.getById(sellerId).getName();
            model.addAttribute("itemCardsPage", itemCardService.getAllItemCardBySeller(page, size, sellerName));
            model.addAttribute("seller", true);
            model.addAttribute("role", role);
            model.addAttribute("name", "by " + sellerName);
            return "all-cards";
        }
        if (!search.isBlank()) {
            model.addAttribute("itemCardsPage", itemCardService.getAllItemCardBySearch(page, size, search));
            model.addAttribute("role", role);
            return "all-cards";
        }
        model.addAttribute("itemCardsPage", itemCardService.getAllItemCard(page, size));
        model.addAttribute("role", role);
        return "all-cards";
    }

    @GetMapping("/item-card/{id}")
    public String getItemCardById(@PathVariable Long id, @RequestParam String role, Model model) {
        boolean isOwner = false;
        ItemCard itemCard = itemCardService.getItemCard(id);
        switch (role) {
            case "ROLE_SELLER" -> isOwner = getAuthSeller().getName().equals(itemCard.getSeller());
            case "ROLE_ADMIN" -> isOwner = true;
            case "ROLE_USER" -> {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                model.addAttribute("cardsId", userService.getCardsId(auth.getName()));
            }
        }
        Long sellerId = sellerService.getBySellerName(itemCard.getSeller()).getId();
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("role", role);
        model.addAttribute("itemCard", itemCard);
        model.addAttribute("sellerId", sellerId);
        return "item-card";
    }

    @DeleteMapping("/item-card/{id}")
    public String deleteItemCardById(@PathVariable Long id, @RequestParam String role, Model model) {
        ItemCard itemCard = itemCardService.getItemCard(id);
        if (role.equals("ROLE_ADMIN") || security(itemCard.getSeller())) {
            itemCardService.deleteItemCard(id);
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
