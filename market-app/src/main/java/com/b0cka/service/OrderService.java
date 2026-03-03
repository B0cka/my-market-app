package com.b0cka.service;

import com.b0cka.models.Order;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OrderService {
    Mono<Long> createOrder();

    Mono<List<Order>> getAllOrders();

    Mono<Order> getOrder(Long id);

}
