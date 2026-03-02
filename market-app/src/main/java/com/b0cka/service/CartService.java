package com.b0cka.service;

import com.b0cka.dto.OrdersDto;
import reactor.core.publisher.Mono;


import java.util.Map;

public interface CartService {

    Mono<Void> add(Long id);

    Mono<Void> remove(Long id);

    Mono<Integer> getCount(Long id);

    Mono<Map<Long, Integer>> getItems();

    Mono<OrdersDto> showCart();

    Mono<String> updateCart(Long id, String action);

    Mono<Void> clear();
}
