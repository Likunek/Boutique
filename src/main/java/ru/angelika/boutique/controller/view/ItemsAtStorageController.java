package ru.angelika.boutique.controller.view;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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
@RequestMapping
@RequiredArgsConstructor
public class ItemsAtStorageController {
    private final ItemService itemService;
    private final SellerService sellerService;
    private final StorageService storageService;
    private final ItemsAtStorageService itemsAtStorageService;

    @PostMapping("/seller/send-to-storage")
    public String addItemsAtStorage(@Valid ItemsAtStorageDto itemsAtStorageDto, Model model) {
        try {
            Seller seller = getAuthSeller();
            List<Item> items = itemService.getBySellerId(seller.getId());
            List<Storage> storages = storageService.getAllStorages();

            model.addAttribute("items", items);
            model.addAttribute("storages", storages);
            model.addAttribute("id", seller.getId());
            itemsAtStorageService.addItemsAtStorage(itemsAtStorageDto);
            model.addAttribute("successMessage", "Item successfully sent to storage!");
        } catch (Exception e) {
            log.error("Error add itemAtStorage: itemId={}, storageId={}: {}",
                    itemsAtStorageDto.getItemId(), itemsAtStorageDto.getStorageId(), e.getMessage(), e);
            model.addAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "send-to-storage";
    }

    @GetMapping("/seller/send-to-storage")
    public String getFormItemsAtStorage(Model model) {
        Seller seller = getAuthSeller();
        List<Item> items = itemService.getBySellerId(seller.getId());
        List<Storage> storages = storageService.getAllStorages();

        model.addAttribute("items", items);
        model.addAttribute("storages", storages);
        model.addAttribute("id", seller.getId());
        return "send-to-storage";
    }
    @GetMapping("admin/item-at-storage")
    public String getAllItemsAtStorage(Model model) {
        model.addAttribute("itemsAtStorages", itemsAtStorageService.getAllItemsAtStorage());
        return "admin-items-at-storage";
    }

    @PutMapping("/seller/send-to-storage/{id}")
    public String updateFormItemsAtStorage(@PathVariable Long id, @RequestParam Long itemId,
                                           @Min(0) Long count, RedirectAttributes redirectAttributes) {
        try {
            Item item = itemService.getById(itemId);
            Long sellerId = item.getSeller().getId();
            if (security(sellerId)) {
                log.warn("Seller with id={} tried to update ItemsAtStorage with id={} from someone else's path", sellerId, id);
                return "redirect:/welcome";
            }
            itemsAtStorageService.updateItemsAtStorage(id, count);
            redirectAttributes.addFlashAttribute("successMessage", "Item successfully sent to storage!");
        } catch (Exception e) {
            log.error("Error update itemsAtStorage id={}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/seller/items/" + itemId;
    }

    private Seller getAuthSeller() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return sellerService.getByNumber(auth.getName());
    }

    private boolean security(Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String roleName = auth.getAuthorities().iterator().next().getAuthority();
        if (roleName.equals("ROLE_SELLER")) {
            return !sellerService.getByNumber(auth.getName()).getId().equals(id);
        }
        return false;
    }
}
