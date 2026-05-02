package ru.angelika.boutique.controller.view;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.angelika.boutique.dto.ItemDto;
import ru.angelika.boutique.model.Seller;
import ru.angelika.boutique.service.ItemService;
import ru.angelika.boutique.service.SellerService;

import java.util.stream.Collectors;

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
        addIdAtForm(model);
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
            addIdAtForm(model);
            itemService.addItem(itemDto);
            model.addAttribute("successMessage", "Item '" + itemDto.getName() + "' added successfully!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error saving item: " + e.getMessage());
        }
        return "add-item";
    }

    private void addIdAtForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Seller seller = sellerService.getByNumber(auth.getName());
        model.addAttribute("id", seller.getId());
    }
}
