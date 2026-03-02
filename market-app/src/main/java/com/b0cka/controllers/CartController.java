package com.b0cka.controllers;

import com.b0cka.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    @GetMapping("/items")
    public Mono<String> showCart(Model model) {
        return cartService.showCart()
                .map(dto -> {
                    model.addAttribute("items", dto.getItems());
                    model.addAttribute("total", dto.getTotalSum());
                    return "cart";
                });
    }

    @PostMapping("/items")
    public Mono<String> updateCart(ServerWebExchange exchange) {
        return exchange.getFormData().flatMap(formData -> {
            Long id = Long.parseLong(formData.getFirst("id"));
            String action = formData.getFirst("action");
            return cartService.updateCart(id, action);
        });
    }
}
