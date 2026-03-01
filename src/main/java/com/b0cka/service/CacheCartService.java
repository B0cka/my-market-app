package com.b0cka.service;

import com.b0cka.dto.CartItemsDto;
import reactor.core.publisher.Mono;

public interface CacheCartService {

    Mono<Void> add(Long id);

    Mono<Void> remove(Long id);

    Mono<Integer> getCount(Long id);

    Mono<CartItemsDto> getItems();

    Mono<Void> clean();

    Mono<Void> removeAll(Long id);
}
