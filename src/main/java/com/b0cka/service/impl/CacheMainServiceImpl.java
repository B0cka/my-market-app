package com.b0cka.service.impl;

import com.b0cka.ex.RedisTrouble;
import com.b0cka.models.Item;
import com.b0cka.service.CacheMainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CacheMainServiceImpl implements CacheMainService {

    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    Duration TIME_TO_LIVE_CHISELKI_IN_MIN = Duration.ofMinutes(5);

    @Override
    public Mono<Boolean> saveInCache(Item item) {
        log.info("Полная индексация товара в Redis: id={}", item.getId());

        String itemKey = "item:" + item.getId();
        String priceIdx = "idx:items:price";
        String alphaIdx = "idx:items:alpha";

        Mono<Boolean> saveBody = reactiveRedisTemplate.opsForValue()
                .set(itemKey, item, TIME_TO_LIVE_CHISELKI_IN_MIN);

        Mono<Boolean> indexPrice = reactiveRedisTemplate.opsForZSet()
                .add(priceIdx, item.getId(), item.getPrice().doubleValue());

        Mono<Boolean> indexAlpha = reactiveRedisTemplate.opsForZSet()
                .add(alphaIdx, item.getTitle() + ":" + item.getId(), 0);

        String allText = (item.getTitle() + " " + item.getDescription()).toLowerCase();
        String[] words = allText.replaceAll("\\p{Punct}", "").split("\\s+");

        Flux<Boolean> indexWords = Flux.fromArray(words)
                .filter(w -> w.length() > 2)
                .flatMap(word -> reactiveRedisTemplate.opsForSet().add("idx:word:" + word, item.getId().toString()))
                .map(res -> true);

        return Mono.zip(saveBody, indexPrice, indexAlpha)
                .thenMany(indexWords)
                .then(Mono.just(true))
                .onErrorMap(e -> new RedisTrouble("Ошибка индексации товара " + item.getId()));
    }

    @Override
    public Mono<Boolean> saveImg(String imgName) {
        String key = "item:img:" + imgName;
        String path = "/images/" + imgName;

        return Mono.fromCallable(() -> {
                    try (var in = getClass().getResourceAsStream(path)) {
                        if (in == null) return null;
                        return in.readAllBytes();
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(bytes -> {
                    if (bytes == null) return Mono.just(false);
                    return reactiveRedisTemplate.opsForValue()
                            .set(key, bytes, TIME_TO_LIVE_CHISELKI_IN_MIN);
                })
                .onErrorMap(throwable -> new RedisTrouble("Не удалось сохранить картинку " + imgName));
    }

    @Override
    public Mono<Boolean> sortByPrice(List<Item> items) {
        log.info("Индексация цен для {} товаров", items.size());

        return Flux.fromIterable(items)
                .flatMap(item ->
                        reactiveRedisTemplate.opsForZSet()
                                .add("idx:items:price", item.getId(), item.getPrice().doubleValue())
                                .onErrorMap(throwable -> new RedisTrouble("Ошибка индекса ID " + item.getId()))
                )
                .collectList()
                .map(results -> true);
    }

    @Override
    public Mono<Boolean> sortByAlpha(List<Item> items) {
        log.info("Индексация названий для {} товаров", items.size());

        return Flux.fromIterable(items)
                .flatMap(item ->
                        reactiveRedisTemplate.opsForZSet()
                                .add("idx:items:alpha", item.getTitle() + ":" + item.getId().toString(), 0)
                                .onErrorMap(throwable -> new RedisTrouble("Ошибка индекса ID " + item.getId()))
                )
                .collectList()
                .map(results -> true);
    }

    @Override
    public Mono<Boolean> setForSearch(List<Item> items) {
        return Flux.fromIterable(items)
                .flatMap(item -> {
                    String allText = (item.getTitle() + " " + item.getDescription()).toLowerCase();

                    String[] words = allText.replaceAll("\\p{Punct}", "").split("\\s+");

                    return Flux.fromArray(words)
                            .filter(word -> word.length() > 2)
                            .flatMap(word ->
                                    reactiveRedisTemplate.opsForSet()

                                            .add("idx:word:" + word, item.getId().toString())
                            );
                })
                .then(Mono.just(true))
                .onErrorMap(throwable -> new RedisTrouble("Ошибка при создании поискового индекса"));
    }

    @Override
    public Flux<Item> getItemsForMainPage(String search, String sort, int page, int size) {
        int start = (page - 1) * size;
        int end = start + size;

        if (search == null || search.isBlank()) {
            String indexKey = "idx:items:" + (sort.equals("PRICE") ? "price" : "alpha");

            return reactiveRedisTemplate.opsForZSet()
                    .range(indexKey, Range.of(
                            Range.Bound.inclusive((long)start),
                            Range.Bound.inclusive((long)end)
                    ))
                    .flatMap(member -> {
                        String id = member.toString().contains(":") ? member.toString().split(":")[1] : member.toString();
                        return reactiveRedisTemplate.opsForValue().get("item:" + id);
                    })
                    .cast(Item.class);
        }

        String[] searchWords = search.toLowerCase().replaceAll("\\p{Punct}", "").split("\\s+");
        List<String> keys = Arrays.stream(searchWords)
                .filter(w -> w.length() > 2)
                .map(w -> "idx:word:" + w)
                .toList();

        if (keys.isEmpty()) return Flux.empty();

        return reactiveRedisTemplate.opsForSet()
                .intersect(keys)
                .flatMap(id -> reactiveRedisTemplate.opsForValue().get("item:" + id))
                .cast(Item.class)
                .sort((a, b) -> sort.equals("PRICE") ?
                        a.getPrice().compareTo(b.getPrice()) :
                        a.getTitle().compareTo(b.getTitle()))
                .skip(start)
                .take(size);
    }

    @Override
    public Mono<byte[]> getImg(String imgName) {
        log.info("Поиск в кеше img: {}", imgName);
        String key = "item:img:" + imgName;

        return reactiveRedisTemplate.opsForValue()
                .get(key)
                .map(obj -> (byte[]) obj);
    }

    @Override
    public Mono<Item> getItem(Long id) {
        log.info("Поиск в кеше Item по id: {}", id);

        String key = "item:" + id;

        return reactiveRedisTemplate.opsForValue()
                .get(key)
                .cast(Item.class);
    }
}
