package com.b0cka.service.impl;

import com.b0cka.dto.CartItemsDto;
import com.b0cka.service.CacheCartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheCartServiceImpl implements CacheCartService {

    @Qualifier("reactiveStringRedisTemplate")
    private final ReactiveStringRedisTemplate redisTemplate;

    @Value("${items.cart.cache}")
    private String KEY_PREFIX;

    @Value("${items.cart.time.cache}")
    private Duration TTL;

    @Override
    public Mono<Void> add(Long id) {
        return currentUser()
                .flatMap(username ->
                        redisTemplate.opsForHash()
                                .increment(userKey(username), id.toString(), 1)
                                .doOnNext(val -> log.info("NEW VALUE = {}", val))
                                .then()
                );
    }

    @Override
    public Mono<Void> remove(Long id) {
        return currentUser()
                .flatMap(username -> {
                    String key = userKey(username);

                    return redisTemplate.opsForHash()
                            .increment(key, id.toString(), -1)
                            .flatMap(newVal -> {
                                if (newVal <= 0) {
                                    return redisTemplate.opsForHash()
                                            .remove(key, id.toString())
                                            .then();
                                }
                                return Mono.empty();
                            });
                });
    }


    @Override
    public Mono<Void> removeAll(Long id) {
        return currentUser()
                .flatMap(username ->
                        redisTemplate.opsForHash()
                                .remove(userKey(username), id.toString())
                                .then()
                );
    }

    @Override
    public Mono<Integer> getCount(Long id) {
        return currentUser()
                .flatMap(username ->
                        redisTemplate.opsForHash()
                                .get(userKey(username), id.toString())
                )
                .map(value -> Integer.parseInt(value.toString()))
                .defaultIfEmpty(0);
    }


    @Override
    public Mono<CartItemsDto> getItems() {
        return currentUser()
                .flatMap(username ->
                        redisTemplate.opsForHash()
                                .entries(userKey(username))
                                .collectMap(
                                        entry -> Long.parseLong(entry.getKey().toString()),
                                        entry -> Integer.parseInt(entry.getValue().toString())
                                )
                )
                .map(CartItemsDto::new);
    }


    @Override
    public Mono<Void> clean() {
        return currentUser()
                .flatMap(username ->
                        redisTemplate.delete(userKey(username)).then()
                );
    }

    private String userKey(String username) {
        return KEY_PREFIX + ":" + username;
    }

    private Mono<String> currentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getName()).doOnNext(name -> log.info("CURRENT USER = {}", name));
    }
}