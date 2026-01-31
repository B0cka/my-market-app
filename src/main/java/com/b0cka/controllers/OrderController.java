package com.b0cka.controllers;

import com.b0cka.models.Order;
import com.b0cka.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/buy")
    public String buy() {
        Long id = orderService.createOrder();

        if (id == null) return "redirect:/cart/items";
        return "redirect:/orders/" + id + "?newOrder=true";
    }

    @GetMapping("/orders")
    public String showOrders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String showOrder(@PathVariable Long id,
                            @RequestParam(defaultValue = "false") boolean newOrder,
                            Model model) {
        Order order = orderService.getOrder(id);

        model.addAttribute("order", order);
        model.addAttribute("newOrder", newOrder);
        return "order";
    }
}