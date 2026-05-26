package ru.angelika.boutique.controller.view;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final SellerService sellerService;
    private boolean isAdmin;


    @GetMapping("/seller/add-item")
    public String getFormNewItem(Model model) {
        addData(model);
        return "add-item";
    }

    @PostMapping("/seller/add-item")
    public String add(@Valid ItemDto itemDto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            log.error("Error add Item {}", itemDto.getName());
            model.addAttribute("errorMessage", "Please correct the errors: " +
                    result.getAllErrors().stream()
                            .map(DefaultMessageSourceResolvable::getDefaultMessage)
                            .collect(Collectors.joining(", ")));
            return "add-item";
        }
        Seller seller = addData(model);
        try {
            itemService.add(itemDto, seller);
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
        List<Item> items = itemService.getAllBySellerWithStorages(seller);
        model.addAttribute("items", items);
        return "seller-items";
    }

    @GetMapping("/items/{id}")
    public String getById(@PathVariable Long id, Model model) {
        Item item = itemService.getById(id);
        Long sellerId = item.getSeller().getId();
        isAdmin = true;
        if (security(sellerId)) {
            log.warn("Seller with id={} tried to view item with id={} from someone else's path", sellerId, id);
            return "redirect:/welcome";
        }
        model.addAttribute("item", item);
        model.addAttribute("isAdmin", isAdmin);
        return "item";
    }
    @GetMapping("/admin/items")
    public String getAll(@RequestParam(defaultValue = "true") boolean unverified, Model model) {
        if (unverified) {
            model.addAttribute("items", itemService.getAll());
        } else {
            model.addAttribute("items", itemService.getAllVerifyFalse());
        }
        return "admin-items";
    }
    @PutMapping("/admin/items/{id}")
    public String updateVerify(@PathVariable Long id, @RequestParam(defaultValue = "true") boolean verify) {
        itemService.updateVerify(id, verify);
        return "redirect:/admin/items";
    }

    @PutMapping("/items/{id}")
    public String update(@PathVariable Long id, @Valid ItemDto itemDto,
                         BindingResult result, RedirectAttributes redirectAttributes) {
        Long sellerId = itemService.getById(id).getSeller().getId();
        if (security(sellerId)) {
            log.warn("Seller with id={} tried to update item with id={} from someone else's path", sellerId, id);
            return "redirect:/welcome";
        }
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the errors: " +
                    result.getAllErrors().stream()
                            .map(DefaultMessageSourceResolvable::getDefaultMessage)
                            .collect(Collectors.joining(", ")));
            return "redirect:/items/" + id;
        }
        try {
            itemService.update(itemDto, id, sellerId);
            redirectAttributes.addFlashAttribute("successMessage", "Item successfully update!");
        } catch (Exception e) {
            log.error("Error update item id={}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/items/" + id;
    }

    @DeleteMapping("/items/{id}")
    public String delete(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String roleName = auth.getAuthorities().iterator().next().getAuthority();
        if (roleName.equals("ROLE_SELLER")) {
            Long sellerId = itemService.getById(id).getSeller().getId();
            if (!sellerService.getByNumber(auth.getName()).getId().equals(sellerId)) {
                log.warn("Seller with id={} tried to delete item with id={} from someone else's path", sellerId, id);
                return "redirect:/welcome";
            }
            itemService.delete(id);
            return "redirect:/seller/items";
        }
        itemService.delete(id);
        return "redirect:/admin/items";
    }

    private Seller addData(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Seller seller = sellerService.getByNumber(auth.getName());
        model.addAttribute("id", seller.getId());
        return seller;
    }
    private boolean security(Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String roleName = auth.getAuthorities().iterator().next().getAuthority();
        if (roleName.equals("ROLE_SELLER")) {
            isAdmin = false;
            return !sellerService.getByNumber(auth.getName()).getId().equals(id);
        }
        return false;
    }
}
