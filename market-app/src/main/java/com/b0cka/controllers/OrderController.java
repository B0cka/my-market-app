package com.b0cka.controllers;

import com.b0cka.ex.BalanceError;
import com.b0cka.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/buy")
    public Mono<String> buy() {
        return orderService.createOrder()
                .map(id -> "redirect:/orders/" + id + "?newOrder=true")
                .switchIfEmpty(Mono.just("redirect:/cart/items"))
                .onErrorResume(BalanceError.class, e -> {
                    return Mono.just("redirect:/cart?error=balance");
                });
    }

    @GetMapping("/orders")
    public Mono<String> showOrders(Model model) {
        return orderService.getAllOrders().doOnNext(orders -> {
            model.addAttribute("orders", orders);
        }).thenReturn("orders");
    }

    @GetMapping("/orders/{id}")
    public Mono<String> showOrder(@PathVariable Long id,
                            @RequestParam(defaultValue = "false") boolean newOrder,
                            Model model) {
        return orderService.getOrder(id).doOnNext(order -> {

            model.addAttribute("order", order);
            model.addAttribute("newOrder", newOrder);

        }).thenReturn("order");
    }
}