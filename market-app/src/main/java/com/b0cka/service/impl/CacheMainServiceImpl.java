package com.b0cka.service.impl;

import com.b0cka.ex.RedisException;
import com.b0cka.models.Item;
import com.b0cka.service.CacheMainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CacheMainServiceImpl implements CacheMainService {

    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    @Value("${items.cart.time.cache.main}")
    private Duration TIME_TO_LIVE_CHISELKI_IN_MIN;

    @Value("${app.redis.key.item}")
    private String KEY_ITEM;

    @Value("${app.redis.key.img}")
    private String KEY_IMG;

    @Value("${app.redis.key.idx.price}")
    private String KEY_IDX_PRICE;

    @Value("${app.redis.key.idx.alpha}")
    private String KEY_IDX_ALPHA;

    @Value("${app.redis.key.idx.word}")
    private String KEY_IDX_WORD;

    @Override
    public Mono<Boolean> saveInCache(Item item) {
        log.info("Полная индексация товара в Redis: id={}", item.getId());

        String itemKey = KEY_ITEM + item.getId();

        Mono<Boolean> saveBody = reactiveRedisTemplate.opsForValue()
                .set(itemKey, item, TIME_TO_LIVE_CHISELKI_IN_MIN);

        Mono<Boolean> indexPrice = reactiveRedisTemplate.opsForZSet()
                .add(KEY_IDX_PRICE, item.getId(), item.getPrice().doubleValue());

        Mono<Boolean> indexAlpha = reactiveRedisTemplate.opsForZSet()
                .add(KEY_IDX_ALPHA, item.getTitle() + "::" + item.getId(), 0);

        String allText = (item.getTitle() + " " + item.getDescription()).toLowerCase();
        String[] words = allText.replaceAll("\\p{Punct}", "").split("\\s+");

        Flux<Boolean> indexWords = Flux.fromArray(words)
                .filter(w -> w.length() > 2)
                .flatMap(word -> reactiveRedisTemplate.opsForSet().add(KEY_IDX_WORD + word, item.getId().toString()))
                .map(res -> true);

        return Mono.zip(saveBody, indexPrice, indexAlpha)
                .thenMany(indexWords)
                .then(Mono.just(true))
                .onErrorMap(e -> new RedisException("Ошибка индексации товара " + item.getId()));
    }

    @Override
    public Mono<Boolean> saveImg(String imgName) {
        String key = KEY_IMG + imgName;
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
                .onErrorMap(throwable -> new RedisException("Не удалось сохранить картинку " + imgName));
    }

    @Override
    public Mono<Boolean> sortByPrice(List<Item> items) {
        log.info("Индексация цен для {} товаров", items.size());

        return Flux.fromIterable(items)
                .flatMap(item ->
                        reactiveRedisTemplate.opsForZSet()
                                .add(KEY_IDX_PRICE, item.getId(), item.getPrice().doubleValue())
                                .onErrorMap(throwable -> new RedisException("Ошибка индекса ID " + item.getId()))
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
                                .add(KEY_IDX_ALPHA, item.getTitle() + "::" + item.getId().toString(), 0)
                                .onErrorMap(throwable -> new RedisException("Ошибка индекса ID " + item.getId()))
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
                                            .add(KEY_IDX_WORD + word, item.getId().toString())
                            );
                })
                .then(Mono.just(true))
                .onErrorMap(throwable -> new RedisException("Ошибка при создании поискового индекса"));
    }

    @Override
    public Flux<Item> getItemsForMainPage(String search, String sort, int page, int size) {
        int start = (page - 1) * size;
        int end = start + size;

        if (search == null || search.isBlank()) {
            String indexKey = sort.equals("PRICE") ? KEY_IDX_PRICE : KEY_IDX_ALPHA;

            return reactiveRedisTemplate.opsForZSet()
                    .range(indexKey, Range.of(
                            Range.Bound.inclusive((long)start),
                            Range.Bound.inclusive((long)end)
                    ))
                    .flatMap(member -> {
                        String raw = member.toString();
                        String id;
                        if (raw.contains("::")) {
                            id = raw.substring(raw.lastIndexOf("::") + 2);
                        } else if (raw.contains(":")) {
                            id = raw.substring(raw.lastIndexOf(":") + 1);
                        } else {
                            id = raw;
                        }
                        return reactiveRedisTemplate.opsForValue().get(KEY_ITEM + id);
                    })
                    .cast(Item.class);
        }

        String[] searchWords = search.toLowerCase().replaceAll("\\p{Punct}", "").split("\\s+");
        List<String> keys = Arrays.stream(searchWords)
                .filter(w -> w.length() > 2)
                .map(w -> KEY_IDX_WORD + w)
                .toList();

        if (keys.isEmpty()) return Flux.empty();

        return reactiveRedisTemplate.opsForSet()
                .intersect(keys)
                .flatMap(id -> reactiveRedisTemplate.opsForValue().get(KEY_ITEM + id))
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
        String key = KEY_IMG + imgName;

        return reactiveRedisTemplate.opsForValue()
                .get(key)
                .map(obj -> (byte[]) obj);
    }

    @Override
    public Mono<Item> getItem(Long id) {
        log.info("Поиск в кеше Item по id: {}", id);
        String key = KEY_ITEM + id;

        return reactiveRedisTemplate.opsForValue()
                .get(key)
                .cast(Item.class);
    }
}