package ru.angelika.boutique.controller.view;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.angelika.boutique.dto.ItemCardDto;
import ru.angelika.boutique.model.Item;
import ru.angelika.boutique.model.Seller;
import ru.angelika.boutique.service.ItemCardService;
import ru.angelika.boutique.service.ItemService;
import ru.angelika.boutique.service.SellerService;

import java.util.List;

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
        addData(model);
        return "add-card";
    }

    @PostMapping("/seller/add-card")
    public String addItemCard(@Valid ItemCardDto itemCardDto, Model model) {
        try {
            itemCardService.addItemCard(itemCardDto, addData(model));
            model.addAttribute("successMessage", "Item successfully sent to storage!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "add-card";
    }

    private String addData(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Seller seller = sellerService.getByNumber(auth.getName());
        List<Item> items = itemService.getItemBySellerId(seller.getId());
        model.addAttribute("items", items);
        model.addAttribute("id", seller.getId());
        return seller.getName();
    }

}
