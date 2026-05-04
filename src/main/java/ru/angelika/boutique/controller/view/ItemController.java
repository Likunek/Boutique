package ru.angelika.boutique.controller.view;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.angelika.boutique.dto.ItemDto;
import ru.angelika.boutique.model.Item;
import ru.angelika.boutique.model.Seller;
import ru.angelika.boutique.service.ItemService;
import ru.angelika.boutique.service.SellerService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping()
public class ItemController {
    private final ItemService itemService;
    private final SellerService sellerService;

    @Autowired
    public ItemController(ItemService itemService, SellerService sellerService) {
        this.itemService = itemService;
        this.sellerService = sellerService;
    }

    @GetMapping("/seller/add-item")
    public String getFormNewItem(Model model) {
        addData(model);
        return "add-item";
    }

    @PostMapping("/seller/add-item")
    public String addItem(@Valid ItemDto itemDto, BindingResult result, Model model) {

        if (result.hasErrors()) {
            model.addAttribute("errorMessage", "Please correct the errors: " +
                    result.getAllErrors().stream()
                            .map(DefaultMessageSourceResolvable::getDefaultMessage)
                            .collect(Collectors.joining(", ")));
            return "add-item";
        }
        try {
            Seller seller = addData(model);
            itemService.addItem(itemDto, seller);
            model.addAttribute("successMessage", "Item '" + itemDto.getName() + "' added successfully!");
        } catch (Exception e) {
            log.error("Error add item '{}': {}", itemDto.getName(), e.getMessage(), e);
            model.addAttribute("errorMessage", "Error saving item: " + e.getMessage());
        }
        return "add-item";
    }

    @GetMapping("/seller/items")
    public String getAllItemsSeller(Model model) {
        Seller seller = addData(model);
        List<Item> items = itemService.getAllItemBySeller(seller);
        model.addAttribute("items", items);
        return "seller-items";
    }
    @GetMapping("/seller/items/{id}")
    public String getItemById(@PathVariable Long id, Model model) {
        addData(model);
        Item item = itemService.getItemById(id);
        model.addAttribute("item", item);
        return "item";
    }
    @PutMapping("/seller/items/{id}")
    public String updateItem(@PathVariable Long id, @Valid ItemDto itemDto, RedirectAttributes redirectAttributes) {
        try {
            itemService.updateItem(itemDto, id);
            redirectAttributes.addFlashAttribute("successMessage", "Item successfully update!");
        } catch (Exception e) {
            log.error("Error update item id={}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/seller/items/" + id;
    }
    @DeleteMapping("/seller/items/{id}")
    public String deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return "redirect:/seller/items";
    }

    private Seller addData(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Seller seller = sellerService.getByNumber(auth.getName());
        model.addAttribute("id", seller.getId());
        return seller;
    }
}
