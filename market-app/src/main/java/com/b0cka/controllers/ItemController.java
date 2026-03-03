package com.b0cka.controllers;
import com.b0cka.ex.NotFoundImageException;
import com.b0cka.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/items")
    public Mono<String> getItems(
            Model model,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "NO") String sort,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize
    ) {
        log.info("Get запрос: search={}, sort={}, page={}", search, sort, pageNumber);

        return itemService.getItems(search, sort, pageNumber, pageSize)
                .doOnNext(itemsPageDto -> {
                    model.addAttribute("search", search);
                    model.addAttribute("sort", sort);
                    model.addAttribute("items", itemsPageDto.getGrid());
                    model.addAttribute("paging", itemsPageDto.getPaging());
                }).thenReturn("items");

    }

    @GetMapping("/images/{filename:.+}")
    public Mono<ResponseEntity<byte[]>> getImg(@PathVariable String filename) {
        return itemService.getImg(filename)
                .map(bytes -> ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(bytes))
                .switchIfEmpty(Mono.error(new NotFoundImageException("Image not found: " + filename)))
                .onErrorMap(IOException.class, e -> new NotFoundImageException("Image not found: " + filename));
    }

    @PostMapping("/items")
    public Mono<String> mainPageAction(
            ServerWebExchange exchange
    ) {
        return exchange.getFormData().flatMap(formData -> {
            String idStr = formData.getFirst("id");
            String action = formData.getFirst("action");
            String search = formData.getFirst("search");
            String sort = formData.getFirst("sort");
            String pageNumberStr = formData.getFirst("pageNumber");
            String pageSizeStr = formData.getFirst("pageSize");

            Long id = Long.parseLong(idStr);
            int pageNumber = pageNumberStr != null ? Integer.parseInt(pageNumberStr) : 1;
            int pageSize = pageSizeStr != null ? Integer.parseInt(pageSizeStr) : 5;
            if (sort == null) sort = "NO";

            log.info("POST /items: id={}, action={}", id, action);

            return itemService.mainPageAction(id, action, search, sort, pageNumber, pageSize);
        });
    }
    @GetMapping("/items{id}")
    public Mono<String> getItem(@PathVariable Long id, Model model) {
        return itemService.getItem(id)
                .doOnNext(item -> model.addAttribute("item", item))
                .thenReturn("item");
    }

    @GetMapping("/item/{id}")
    public Mono<String> getItem1(@PathVariable Long id, Model model) {
        return itemService.getItem(id)
                .doOnNext(item -> model.addAttribute("item", item))
                .thenReturn("item");
    }


    @PostMapping("/items/{id}")
    public Mono<String> itemPageAction(@PathVariable Long id, ServerWebExchange exchange) {
        return exchange.getFormData().flatMap(formData -> {
            String action = formData.getFirst("action");
            return itemService.itemPageAction(id, action);
        });
    }

}