package ru.angelika.boutique.controller.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.angelika.boutique.dto.StorageDto;
import ru.angelika.boutique.service.StorageService;

@Controller
@RequestMapping("/admin/storages")
public class StorageController {
    private final StorageService storageService;

    @Autowired
    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping
    public String getAllStorage(Model model) {
        model.addAttribute("storages", storageService.getAllStorage());
        return "admin-storages";
    }
    @PutMapping("{id}")
    public String updateStorage(StorageDto storageDto, @PathVariable Long id) {
        storageService.updateStorage(storageDto, id);
        return "redirect:/admin/storages";
    }
    @DeleteMapping("{id}")
    public String deleteStorage(@PathVariable Long id) {
        storageService.deleteStorage(id);
        return "redirect:/admin/storages";
    }
}
