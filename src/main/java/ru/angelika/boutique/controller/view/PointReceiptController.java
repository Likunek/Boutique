package ru.angelika.boutique.controller.view;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.angelika.boutique.dto.PointReceiptDto;
import ru.angelika.boutique.service.PointReceiptService;

import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/admin/points")
public class PointReceiptController {
    private final PointReceiptService pointReceiptService;

    @Autowired
    public PointReceiptController(PointReceiptService pointReceiptService) {
        this.pointReceiptService = pointReceiptService;
    }

    @PostMapping("/add")
    public String addStorage(@Valid PointReceiptDto pointDto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            log.error("Error add PointReceipt '{}, {}'", pointDto.getCity(), pointDto.getAddress());
            model.addAttribute("errorMessage", "Please correct the errors: " +
                    result.getAllErrors().stream()
                            .map(DefaultMessageSourceResolvable::getDefaultMessage)
                            .collect(Collectors.joining(", ")));
            return "admin-add-point";
        }
        try {
            pointReceiptService.addPointReceipt(pointDto);
            model.addAttribute("successMessage", "PointReceipt added successfully!");
        } catch (Exception e) {
            log.error("Error add PointReceipt '{}, {}': {}", pointDto.getCity(), pointDto.getAddress(), e.getMessage(), e);
            model.addAttribute("errorMessage", "Error saving PointReceipt: " + e.getMessage());
        }
        return "admin-add-point";
    }
    @GetMapping("/add")
    public String getFormNewItem() {
        return "admin-add-point";
    }
}
