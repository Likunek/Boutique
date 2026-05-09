package ru.angelika.boutique.controller.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.angelika.boutique.model.Seller;
import ru.angelika.boutique.service.SellerService;

@Controller
@RequestMapping
public class SellerController {

    private final SellerService sellerService;

    @Autowired
    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @GetMapping("/seller/profile/{id}")
    public String sellerPage(@PathVariable Long id, Model model) {
        if (security(id)) {
            return "redirect:/welcome";
        }
        Seller seller = sellerService.getById(id);
        model.addAttribute("seller", seller);
        return "seller";
    }

    @GetMapping("/public-seller/{id}")
    public String getSeller(@PathVariable Long id, Model model) {
        Seller seller = sellerService.getById(id);
        model.addAttribute("seller", seller);
        return "public-seller";
    }
    @GetMapping("/admin/sellers")
    public String getAllSellers(Model model) {
        model.addAttribute("sellers", sellerService.getAllSeller());
        return "admin-sellers";
    }

    @DeleteMapping("/seller/profile/{id}")
    public String deleteSeller(@PathVariable Long id) {
        if (security(id)) {
            return "redirect:/welcome";
        }
        sellerService.deleteSeller(id);
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
