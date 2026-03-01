package com.b0cka.service;

import com.b0cka.dto.ItemsPageDto;
import com.b0cka.models.Item;
import reactor.core.publisher.Mono;


public interface ItemService {

    Mono<Void> refreshAllCaches();

    Mono<ItemsPageDto> getItems(String search, String sort, int page, int size);

    Mono<byte[]> getImg(String path);

    Mono<String> mainPageAction(Long id, String action, String search, String sort, int pageNumber, int pageSize);

    Mono<Item> getItem(Long id);

    Mono<String> itemPageAction(Long id, String action);
}
