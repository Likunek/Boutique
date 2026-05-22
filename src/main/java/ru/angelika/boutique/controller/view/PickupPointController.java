package ru.angelika.boutique.controller.view;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.angelika.boutique.dto.PickupPointDto;

import ru.angelika.boutique.service.PickupPointService;
import ru.angelika.boutique.service.StorageService;

import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/admin/points")
@RequiredArgsConstructor
public class PickupPointController {
    private final PickupPointService pickupPointService;
    private final StorageService storageService;

    @PostMapping("/add")
    public String addPoint(@Valid PickupPointDto pointDto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            log.error("Error add PointReceipt '{}, {}'", pointDto.getCity(), pointDto.getAddress());
            model.addAttribute("errorMessage", "Please correct the errors: " +
                    result.getAllErrors().stream()
                            .map(DefaultMessageSourceResolvable::getDefaultMessage)
                            .collect(Collectors.joining(", ")));
            return "redirect:/admin/points/add";
        }
        try {
            pickupPointService.add(pointDto);
            model.addAttribute("successMessage", "PointReceipt added successfully!");
        } catch (Exception e) {
            log.error("Error add PointReceipt '{}, {}': {}", pointDto.getCity(), pointDto.getAddress(), e.getMessage(), e);
            model.addAttribute("errorMessage", "Error saving PointReceipt: " + e.getMessage());
        }
        return "redirect:/admin/points/add";
    }

    @GetMapping("/add")
    public String getFormNewPoint(Model model) {
        model.addAttribute("storages", storageService.getAllStorages());
        return "admin-add-point";
    }

    @GetMapping
    public String getAllPoints(Model model) {
        model.addAttribute("points", pickupPointService.getAll());
        model.addAttribute("storages", storageService.getAllStorages());
        return "admin-points";
    }

    @PutMapping("{id}")
    public String updatePoint(PickupPointDto pickupPointDto, @PathVariable Long id) {
        pickupPointService.update(id, pickupPointDto);
        return "redirect:/admin/points";
    }

    @DeleteMapping("{id}")
    public String deletePoint(@PathVariable Long id) {
        pickupPointService.delete(id);
        return "redirect:/admin/points";
    }
}
