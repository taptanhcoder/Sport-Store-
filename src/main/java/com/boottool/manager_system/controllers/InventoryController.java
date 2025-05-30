package com.boottool.manager_system.controllers;

import com.boottool.manager_system.models.Inventory;
import com.boottool.manager_system.models.InventoryTxnType;
import com.boottool.manager_system.services.InventoryRepository;
import com.boottool.manager_system.services.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryRepository invRepo;
    private final InventoryService invService;

    @Autowired
    public InventoryController(InventoryRepository invRepo, InventoryService invService) {
        this.invRepo = invRepo;
        this.invService = invService;
    }

    @GetMapping({"", "/"})
    public String list(Model model) {
        model.addAttribute("inventories", invRepo.findAll());
        return "inventory/index";
    }

    // Chuyển sang sử dụng path variable để luôn yêu cầu id
    @GetMapping("/{id}/adjust")
    public String formAdjust(@PathVariable Long id, Model model) {
        Inventory inv = invRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid inventory ID: " + id));
        model.addAttribute("inventory", inv);
        return "inventory/AdjustStock";
    }

    @PostMapping("/{id}/adjust")
    public String doAdjust(
            @PathVariable Long id,
            @RequestParam int delta,
            @RequestParam String note,
            RedirectAttributes ra
    ) {
        Inventory inv = invRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid inventory ID: " + id));
        invService.adjustStock(
                inv.getProduct(),
                delta,
                delta > 0 ? InventoryTxnType.IN : InventoryTxnType.OUT,
                note
        );
        ra.addFlashAttribute("success", "Đã điều chỉnh kho thành công.");
        return "redirect:/inventory";
    }
}