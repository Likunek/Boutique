package ru.angelika.boutique.controller.view;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.angelika.boutique.dto.PickupPointDto;

import ru.angelika.boutique.service.PickupPointService;

import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/admin/points")
public class PickupPointController {
    private final PickupPointService pickupPointService;

    @Autowired
    public PickupPointController(PickupPointService pickupPointService) {
        this.pickupPointService = pickupPointService;
    }

    @PostMapping("/add")
    public String addPoint(@Valid PickupPointDto pointDto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            log.error("Error add PointReceipt '{}, {}'", pointDto.getCity(), pointDto.getAddress());
            model.addAttribute("errorMessage", "Please correct the errors: " +
                    result.getAllErrors().stream()
                            .map(DefaultMessageSourceResolvable::getDefaultMessage)
                            .collect(Collectors.joining(", ")));
            return "admin-add-point";
        }
        try {
            pickupPointService.addPickupPoint(pointDto);
            model.addAttribute("successMessage", "PointReceipt added successfully!");
        } catch (Exception e) {
            log.error("Error add PointReceipt '{}, {}': {}", pointDto.getCity(), pointDto.getAddress(), e.getMessage(), e);
            model.addAttribute("errorMessage", "Error saving PointReceipt: " + e.getMessage());
        }
        return "admin-add-point";
    }
    @GetMapping("/add")
    public String getFormNewPoint() {
        return "admin-add-point";
    }

    @GetMapping
    public String getAllPoints(Model model) {
        model.addAttribute("points", pickupPointService.getAllPoints());
        return "admin-points";
    }
    @PutMapping("{id}")
    public String updatePoint(PickupPointDto pickupPointDto, @PathVariable Long id) {
        pickupPointService.updatePickupPoint(id, pickupPointDto);
        return "redirect:/admin/points";
    }
    @DeleteMapping("{id}")
    public String deletePoint(@PathVariable Long id) {
        pickupPointService.deletePickupPoint(id);
        return "redirect:/admin/points";
    }
}
