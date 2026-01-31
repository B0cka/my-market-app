package com.b0cka.controllers;

import com.b0cka.dto.OrdersDto;
import com.b0cka.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    @GetMapping("/items")
    public String showCart(Model model) {
        OrdersDto dto = cartService.showCart();

        model.addAttribute("items", dto.getItems());
        model.addAttribute("total", dto.getTotalSum());

        return "cart";
    }

    @PostMapping("/items")
    public String updateCart(@RequestParam Long id, @RequestParam String action) {
        return cartService.updateCart(id, action);
    }
}
