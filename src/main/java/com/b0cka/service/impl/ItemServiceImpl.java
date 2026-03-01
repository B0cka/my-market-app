package com.b0cka.service.impl;

import com.b0cka.ex.NotFoundImageException;
import com.b0cka.repository.ItemRepository;
import com.b0cka.dto.ItemsPageDto;
import com.b0cka.models.Item;
import com.b0cka.models.Paging;
import com.b0cka.service.CacheMainService;
import com.b0cka.service.CartService;
import com.b0cka.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final CartService cartServiceImpl;
    private final CacheMainService cacheMainService;

    @Override
    public Mono<ItemsPageDto> getItems(String search, String sort, int page, int size) {
        int pageNumber = Math.max(page, 1);
        int pageSize = Math.max(size, 1);

        return cacheMainService.getItemsForMainPage(search, sort, pageNumber, pageSize)
                .collectList()
                .flatMap(items -> {
                    if (!items.isEmpty()) {
                        log.info("Данные витрины взяты из CACHE");
                        return Mono.just(items);
                    }

                    log.info("Кеш пуст! Идем в БД и прогреваем кеш...");
                    return fetchItemsFromDb(search, sort, pageNumber, pageSize);
                })

                .zipWith(cartServiceImpl.getItems())
                .map(tuple -> {
                    List<Item> pageItems = tuple.getT1();
                    Map<Long, Integer> cartMap = tuple.getT2();

                    pageItems.forEach(item -> item.setCount(cartMap.getOrDefault(item.getId(), 0)));

                    boolean hasNext = pageItems.size() > pageSize;
                    List<Item> finalItems = hasNext ? pageItems.subList(0, pageSize) : pageItems;

                    List<List<Item>> grid = new ArrayList<>();
                    for (int i = 0; i < finalItems.size(); i += 3) {
                        List<Item> row = new ArrayList<>(finalItems.subList(i, Math.min(i + 3, finalItems.size())));
                        while (row.size() < 3) {
                            Item dummy = new Item();
                            dummy.setId(-1L);
                            row.add(dummy);
                        }
                        grid.add(row);
                    }

                    Paging paging = Paging.builder()
                            .pageNumber(pageNumber)
                            .pageSize(pageSize)
                            .hasPrevious(pageNumber > 1)
                            .hasNext(hasNext)
                            .build();

                    return new ItemsPageDto(grid, paging);
                });
    }

    @Override
    public Mono<byte[]> getImg(String filename) {
        String path = "/images/" + filename;

        return Mono.fromCallable(() -> {
                    try (var in = getClass().getResourceAsStream(path)) {
                        if (in == null) return null;
                        return in.readAllBytes();
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(bytes -> bytes == null ? Mono.empty() : Mono.just(bytes));
    }

    @Override
    public Mono<String> mainPageAction(Long id, String action, String search, String sort, int pageNumber, int pageSize) {

        Mono<Void> actionMono = "PLUS".equals(action) ?
                cartServiceImpl.add(id) : cartServiceImpl.remove(id);

        String redirectUrl = "/items" +
                "?search=" + (search != null ? search : "") +
                "&sort=" + sort +
                "&pageNumber=" + pageNumber +
                "&pageSize=" + pageSize;
        log.info("redirect:" + redirectUrl);
        return actionMono.thenReturn("redirect:" + redirectUrl);
    }

    @Override
    public Mono<String> itemPageAction(Long id, String action) {

        Mono<Void> op = "PLUS".equals(action) ? cartServiceImpl.add(id) : cartServiceImpl.remove(id);

        return op.thenReturn("redirect:/item/" + id);

    }

    @Override
    public Mono<Item> getItem(Long id) {
        return cacheMainService.getItem(id)
                .switchIfEmpty(
                        itemRepository.findById(id)
                                .flatMap(item -> cacheMainService.saveInCache(item).thenReturn(item))
                )
                .zipWith(cartServiceImpl.getCount(id))
                .map(tuple -> {
                    tuple.getT1().setCount(tuple.getT2());
                    return tuple.getT1();
                });
    }

    @Override
    public Mono<Void> refreshAllCaches() {
        return itemRepository.findAll()
                .buffer(10)
                .flatMap(batch -> Mono.zip(
                        cacheMainService.sortByPrice(batch),
                        cacheMainService.sortByAlpha(batch),
                        cacheMainService.setForSearch(batch),
                        Flux.fromIterable(batch).flatMap(cacheMainService::saveInCache).collectList()
                ))
                .then();
    }

    private Mono<List<Item>> fetchItemsFromDb(String search, String sort, int page, int size) {
        long offset = (long) (page - 1) * size;
        int limit = size + 1;

        boolean hasSearch = (search != null && !search.isBlank());
        String q = hasSearch ? search.trim() : "";

        Flux<Item> src = hasSearch
                ? ("ALPHA".equals(sort) ? itemRepository.searchAlpha(q, limit, offset) :

                "PRICE".equals(sort) ? itemRepository.searchPrice(q, limit, offset) :
                        itemRepository.searchNoSort(q, limit, offset))
                : (
                itemRepository.searchNoSort("", limit, offset));

        return src.collectList();
    }
}