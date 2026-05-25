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
import ru.angelika.boutique.dto.UpdateEntityDto;
import ru.angelika.boutique.model.Seller;
import ru.angelika.boutique.service.SellerService;

import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    @GetMapping("/seller/profile/{id}")
    public String sellerPage(@PathVariable Long id, Model model) {
        if (security(id)) {
            log.warn("Seller tried to view /seller/profile/ with id={} from someone else's path", id);
            return "redirect:/welcome";
        }
        Seller seller = sellerService.getById(id);
        model.addAttribute("seller", seller);
        return "seller";
    }

    @GetMapping("/public-seller/{id}")
    public String get(@PathVariable Long id, @RequestParam String role, Model model) {
        Seller seller = sellerService.getById(id);
        model.addAttribute("role", role);
        model.addAttribute("seller", seller);
        return "public-seller";
    }
    @GetMapping("/admin/sellers")
    public String getAll(Model model) {
        model.addAttribute("sellers", sellerService.getAll());
        return "admin-sellers";
    }

    @PutMapping("/seller/profile/{id}")
    public String update(@PathVariable Long id, @Valid UpdateEntityDto updateEntityDto,
                               BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the errors: " +
                    result.getAllErrors().stream()
                            .map(DefaultMessageSourceResolvable::getDefaultMessage)
                            .collect(Collectors.joining(", ")));
            return "redirect:/seller/profile/" + id;
        }
        try {
            if (security(id)) {
                log.warn("Seller tried to update /seller/profile/ with id={} from someone else's path", id);
                return "redirect:/welcome";
            }
            sellerService.update(updateEntityDto, id);
        } catch (Exception e) {
            log.error("Error update seller profile id={}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
            return "redirect:/seller/profile/" + id;
        }
        return "redirect:/logout";
    }

    @DeleteMapping("/seller/profile/{id}")
    public String delete(@PathVariable Long id) {
        if (security(id)) {
            log.warn("Seller tried to delete /seller/profile/ with id={} from someone else's path", id);
            return "redirect:/welcome";
        }
        sellerService.delete(id);
        return "redirect:/registration";
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
