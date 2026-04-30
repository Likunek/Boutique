package ru.angelika.boutique.controller.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.angelika.boutique.model.Seller;
import ru.angelika.boutique.service.SellerService;

@Controller
@RequestMapping("profile/sellers")
public class SellerController {

    private final SellerService sellerService;

    @Autowired
    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @GetMapping("{id}")
    public String sellerPage(@PathVariable Long id, Model model) {
        Seller seller = sellerService.getById(id);
        model.addAttribute("seller", seller);
        return "seller";
    }
}
