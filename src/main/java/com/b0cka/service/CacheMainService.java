package com.b0cka.service;

import com.b0cka.models.Item;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CacheMainService {

    Mono<Boolean> saveInCache(Item item);

    Mono<Boolean> saveImg(String imgName);

    Mono<Boolean> sortByPrice(List<Item> items);

    Mono<Boolean> sortByAlpha(List<Item> items);

    Mono<Boolean> setForSearch(List<Item> items);

    Flux<Item> getItemsForMainPage(String search, String sort, int page, int size);

    Mono<byte[]> getImg(String imgName);

    Mono<Item> getItem(Long id);
}
