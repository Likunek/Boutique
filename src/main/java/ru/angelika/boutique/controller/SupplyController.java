package ru.angelika.boutique.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.angelika.boutique.service.SupplyService;

/**
 * Контроллер для отображения поставок (доступен администратору).
 */
@Controller
@RequiredArgsConstructor
public class SupplyController {
    private final SupplyService supplyService;

    /**
     * Отображает список всех поставок.
     *
     * @param model модель
     * @return "admin-supplies"
     */
    @GetMapping("/admin/supplies")
    public String getAll(Model model) {
        model.addAttribute("supplies", supplyService.getAll());
        return "admin-supplies";
    }
}