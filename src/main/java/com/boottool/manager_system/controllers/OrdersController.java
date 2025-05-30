package com.boottool.manager_system.controllers;

import com.boottool.manager_system.models.CreateOrderRequest;
import com.boottool.manager_system.models.ApiException;
import com.boottool.manager_system.services.OrderService;
import com.boottool.manager_system.services.OrderView;
import com.boottool.manager_system.services.ProductsRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@Controller
@RequestMapping("/orders")
public class OrdersController {

    private final OrderService orderService;
    private final ProductsRepository productsRepo;

    @Autowired
    public OrdersController(OrderService orderService,
                            ProductsRepository productsRepo) {
        this.orderService = orderService;
        this.productsRepo = productsRepo;
    }

    @GetMapping
    public String listOrders(Model model, Pageable pageable) {
        model.addAttribute("orders", orderService.list(pageable).getContent());
        return "orders/index";
    }

    @GetMapping("/create")
    public String createOrderForm(Model model) {
        // Khởi tạo CreateOrderRequest với 1 dòng item mặc định
        CreateOrderRequest req = new CreateOrderRequest();
        req.setItems(new ArrayList<>());
        req.getItems().add(new CreateOrderRequest.Item());

        model.addAttribute("orderRequest", req);
        model.addAttribute("products", productsRepo.findAll());
        return "orders/create";
    }

    @PostMapping("/create")
    public String submitNewOrder(
            @Valid @ModelAttribute("orderRequest") CreateOrderRequest req,
            BindingResult br,
            Model model
    ) {
        // Luôn cung cấp lại danh sách products cho form nếu phải render lại
        model.addAttribute("products", productsRepo.findAll());

        // Nếu validation Bean (NotEmpty, Min…) fail, quay lại form
        if (br.hasErrors()) {
            return "orders/create";
        }

        try {
            OrderView created = orderService.create(req);
            return "redirect:/orders/" + created.getId();
        } catch (ApiException ex) {
            // Bắt lỗi nghiệp vụ (ví dụ thiếu stock, product không tồn tại)
            model.addAttribute("errorMessage", ex.getMessage());
            return "orders/create";
        }
    }

    @GetMapping("/{id:\\d+}")
    public String viewOrder(@PathVariable("id") Long id, Model model) {
        try {
            OrderView order = orderService.getById(id);
            model.addAttribute("order", order);
            return "orders/detail";
        } catch (ApiException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "orders/index";
        }
    }
}
