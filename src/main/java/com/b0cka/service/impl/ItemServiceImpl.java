package com.b0cka.service.impl;

import com.b0cka.repository.ItemRepository;
import com.b0cka.dto.ItemsPageDto;
import com.b0cka.models.Item;
import com.b0cka.models.Paging;
import com.b0cka.service.CartService;
import com.b0cka.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final CartService cartServiceImpl;

    @Override
    public Mono<ItemsPageDto> getItems(String search, String sort, int page, int size) {

        int pageNumber = Math.max(page, 1);
        int pageSize = Math.max(size, 1);

        long offset = (long) (pageNumber - 1) * pageSize;
        int limit = pageSize + 1;

        boolean hasSearch = (search != null && !search.isBlank());
        String q = hasSearch ? search.trim() : "";

        Flux<Item> src;

        if ("ALPHA".equals(sort)) {
            src = itemRepository.searchAlpha(q, limit, offset);
        } else if ("PRICE".equals(sort)) {
            src = itemRepository.searchPrice(q, limit, offset);
        } else {
            src = itemRepository.searchNoSort(q, limit, offset);
        }

        return src.collectList().map(items -> {

            boolean hasNext = items.size() > pageSize;

            List<Item> pageItems = hasNext ? items.subList(0, pageSize) : items;

            pageItems.forEach(item -> item.setCount(cartServiceImpl.getCount(item.getId())));
            List<List<Item>> grid = new ArrayList<>();

            for (int i = 0; i < pageItems.size(); i += 3) {

                List<Item> row = new ArrayList<>(pageItems.subList(i, Math.min(i + 3, pageItems.size())));


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

        if ("PLUS".equals(action)) {

            cartServiceImpl.add(id);
        } else if ("MINUS".equals(action)) {
            cartServiceImpl.remove(id);
        }

        String redirectUrl = "/items" +
                "?search=" + (search != null ? search : "") +
                "&sort=" + sort +
                "&pageNumber=" + pageNumber +
                "&pageSize=" + pageSize;
        log.info("redirect:" + redirectUrl);
        return Mono.just("redirect:" + redirectUrl);
    }

    @Override
    public Mono<String> itemPageAction(Long id, String action) {

        if ("PLUS".equals(action)) {

            cartServiceImpl.add(id);
        } else if ("MINUS".equals(action)) {
            cartServiceImpl.remove(id);
        }

        String redirectUrl =
                "/item/" + id;
        return Mono.just("redirect:" + redirectUrl);

    }

    @Override
    public Mono<Item> getItem(Long id) {
        return itemRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Товар не найден")))
                .map(item -> {
                    item.setCount(cartServiceImpl.getCount(id));
                    return item;
                });
    }
}
