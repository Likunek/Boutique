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
import ru.angelika.boutique.dto.ItemCardDto;
import ru.angelika.boutique.dto.ItemCardUpdateDto;
import ru.angelika.boutique.model.Item;
import ru.angelika.boutique.model.ItemCard;
import ru.angelika.boutique.model.Seller;
import ru.angelika.boutique.service.ItemCardService;
import ru.angelika.boutique.service.ItemService;
import ru.angelika.boutique.service.SellerService;

import java.util.List;
@Slf4j
@Controller
@RequestMapping
public class ItemCardController {
    private final ItemService itemService;
    private final SellerService sellerService;
    private final ItemCardService itemCardService;

    @Autowired
    public ItemCardController(ItemService itemService, SellerService sellerService, ItemCardService itemCardService) {
        this.itemService = itemService;
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
            itemCardService.updateItemCard(itemCardUpdateDto, id, sellerName);
            redirectAttributes.addFlashAttribute("successMessage", "Card successfully update!");
        } catch (Exception e) {
            log.error("Error update itemCard id {} : {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/seller/items/" + itemId;
    }

    @GetMapping("/item-card")
    public String getAllItemCard(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "12") int size,
                                 @RequestParam String role, Model model) {
        Page<ItemCard> itemCards = itemCardService.getAllItemCard(page, size);
        model.addAttribute("itemCardsPage", itemCards);
        model.addAttribute("role", role);
        return "cards";
    }

    private Seller getAuthSeller() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return sellerService.getByNumber(auth.getName());
    }
}
