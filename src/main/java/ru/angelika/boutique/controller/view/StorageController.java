package ru.angelika.boutique.controller.view;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.angelika.boutique.dto.StorageDto;
import ru.angelika.boutique.service.StorageService;

import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/admin/storages")
@RequiredArgsConstructor
public class StorageController {
    private final StorageService storageService;

    @PostMapping("/add")
    public String addStorage(@Valid StorageDto storageDto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            log.error("Error add Storage '{}, {}'", storageDto.getCity(), storageDto.getAddress());
            model.addAttribute("errorMessage", "Please correct the errors: " +
                    result.getAllErrors().stream()
                            .map(DefaultMessageSourceResolvable::getDefaultMessage)
                            .collect(Collectors.joining(", ")));
            return "admin-add-storage";
        }
        try {
            storageService.add(storageDto);
            model.addAttribute("successMessage", "Storage added successfully!");
        } catch (Exception e) {
            log.error("Error add Storage '{}, {}': {}", storageDto.getCity(), storageDto.getAddress(), e.getMessage(), e);
            model.addAttribute("errorMessage", "Error saving item: " + e.getMessage());
        }
        return "admin-add-storage";
    }
    @GetMapping("/add")
    public String getFormNewStorage() {
        return "admin-add-storage";
    }

    @GetMapping
    public String getAllStorage(Model model) {
        model.addAttribute("storages", storageService.getAll());
        return "admin-storages";
    }
    @PutMapping("{id}")
    public String updateStorage(StorageDto storageDto, @PathVariable Long id) {
        storageService.update(storageDto, id);
        return "redirect:/admin/storages";
    }
    @DeleteMapping("{id}")
    public String deleteStorage(@PathVariable Long id) {
        storageService.delete(id);
        return "redirect:/admin/storages";
    }
}
