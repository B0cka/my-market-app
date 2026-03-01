package com.b0cka.service.impl;

import com.b0cka.dto.CartItemsDto;
import com.b0cka.service.CacheCartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheCartServiceImpl implements CacheCartService {

    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    Duration TIME_TO_LIVE_CHISELKI_IN_MIN = Duration.ofMinutes(5);

    String KEY = "items:cart";

    @Override
    public Mono<Void> add(Long id) {

        return reactiveRedisTemplate.opsForHash()
                .increment(KEY, id.toString(), 1)
                .then();
    }

    @Override
    public Mono<Void> remove(Long id) {

        return reactiveRedisTemplate.opsForHash()
                .increment(KEY, id.toString(), -1)
                .flatMap(newVal -> {
                    if (newVal <= 0) {
                        return reactiveRedisTemplate.opsForHash().remove(KEY, id.toString()).then();
                    }
                    return Mono.empty();
                })
                .then();
    }

    @Override
    public Mono<Void> removeAll(Long id){
        return reactiveRedisTemplate.opsForHash()
                .remove(KEY, id.toString())
                .then();
    }

    @Override
    public Mono<Integer> getCount(Long id) {

        return reactiveRedisTemplate.opsForHash()
                .get(KEY, id.toString())
                .map(o -> (Integer) o)
                .defaultIfEmpty(0);
    }

    @Override
    public Mono<CartItemsDto> getItems() {

        return reactiveRedisTemplate.opsForHash()
                .entries(KEY)
                .collectMap(
                        entry -> Long.valueOf(entry.getKey().toString()),
                        entry -> Integer.valueOf(entry.getValue().toString())
                )
                .map(map -> new CartItemsDto(map));
    }

    @Override
    public Mono<Void> clean() {

        return reactiveRedisTemplate.opsForHash()
                .delete(KEY)
                .then();


    }


}
