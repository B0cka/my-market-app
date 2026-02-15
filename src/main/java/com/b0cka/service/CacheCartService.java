package com.b0cka.service;

import com.b0cka.dto.HDto;
import reactor.core.publisher.Mono;

public interface CacheCartService {

    Mono<Void> add(Long id);

    Mono<Void> remove(Long id);

    Mono<Integer> getCount(Long id);

    Mono<HDto> getItems();

    Mono<Void> clean();

    Mono<Void> removeAll(Long id);
}
