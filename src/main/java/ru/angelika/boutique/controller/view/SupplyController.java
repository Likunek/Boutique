package ru.angelika.boutique.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.angelika.boutique.service.SupplyService;

@Controller
public class SupplyController {
    private final SupplyService supplyService;

    public SupplyController(SupplyService supplyService) {
        this.supplyService = supplyService;
    }

    @GetMapping("/admin/supplies")
    public String getAllSupplies(Model model) {
        model.addAttribute("supplies", supplyService.getAllSupplies());
        return "admin-supplies";
    }

}
