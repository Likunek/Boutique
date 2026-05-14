package ru.angelika.boutique.controller.view;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.angelika.boutique.service.SupplyService;

@Controller
@RequiredArgsConstructor
public class SupplyController {
    private final SupplyService supplyService;

    @GetMapping("/admin/supplies")
    public String getAllSupplies(Model model) {
        model.addAttribute("supplies", supplyService.getAllSupplies());
        return "admin-supplies";
    }

}
