package com.b0cka.service;

import com.b0cka.dto.OrdersDto;
import reactor.core.publisher.Mono;


import java.util.Map;

public interface CartService {

    void add(Long id);

    void remove(Long id);

    int getCount(Long id);

    Map<Long, Integer> getItems();

    Mono<OrdersDto> showCart();

    Mono<String> updateCart(Long id, String action);

    void clear();
}
