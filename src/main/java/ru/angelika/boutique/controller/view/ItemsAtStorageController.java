package ru.angelika.boutique.controller.view;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.angelika.boutique.dto.ItemsAtStorageDto;
import ru.angelika.boutique.model.Item;
import ru.angelika.boutique.model.Seller;
import ru.angelika.boutique.model.Storage;
import ru.angelika.boutique.service.ItemService;
import ru.angelika.boutique.service.ItemsAtStorageService;
import ru.angelika.boutique.service.SellerService;
import ru.angelika.boutique.service.StorageService;

import java.util.List;
@Slf4j
@Controller
@RequestMapping("/seller/send-to-storage")
public class ItemsAtStorageController {
    private final ItemService itemService;
    private final SellerService sellerService;
    private final StorageService storageService;
    private final ItemsAtStorageService itemsAtStorageService;

    @Autowired
    public ItemsAtStorageController(ItemService itemService, SellerService sellerService,
                                    StorageService storageService, ItemsAtStorageService itemsAtStorageService) {
        this.itemService = itemService;
        this.sellerService = sellerService;
        this.storageService = storageService;
        this.itemsAtStorageService = itemsAtStorageService;
    }

    @GetMapping
    public String getFormItemsAtStorage(Model model) {
        addData(model);
        return "send-to-storage";
    }

    @PostMapping
    public String addItemsAtStorage(@Valid ItemsAtStorageDto itemsAtStorageDto, Model model) {
        addData(model);
        try {
            itemsAtStorageService.addItemsAtStorage(itemsAtStorageDto);
            model.addAttribute("successMessage", "Item successfully sent to storage!");
        } catch (Exception e) {
            log.error("Error add itemAtStorage: itemId={}, storageId={}: {}",
                    itemsAtStorageDto.getItemId(), itemsAtStorageDto.getStorageId(), e.getMessage(), e);
            model.addAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "send-to-storage";
    }

    @PutMapping
    public String updateFormItemsAtStorage(Long id, Long count, RedirectAttributes redirectAttributes) {
        try {
            itemsAtStorageService.updateItemsAtStorage(id, count);
            redirectAttributes.addFlashAttribute("successMessage", "Item successfully sent to storage!");
        } catch (Exception e) {
            log.error("Error update itemsAtStorage id={}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/seller/items/" + id;
    }

    private void addData(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Seller seller = sellerService.getByNumber(auth.getName());

        List<Item> items = itemService.getItemBySellerId(seller.getId());
        List<Storage> storages = storageService.getAllStorages();

        model.addAttribute("items", items);
        model.addAttribute("storages", storages);
        model.addAttribute("id", seller.getId());
    }
}
