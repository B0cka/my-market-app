package com.b0cka.service.impl;

import com.b0cka.dto.OrdersDto;
import com.b0cka.models.Item;
import com.b0cka.repository.ItemRepository;
import com.b0cka.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final ItemRepository itemRepository;

    private final Map<Long, Integer> items = new HashMap<>();

    public void add(Long id) {
        items.merge(id, 1, Integer::sum);
    }

    public void remove(Long id) {
        if (items.containsKey(id)) {
            int current = items.get(id);
            if (current > 1) {
                items.put(id, current - 1);
            } else {
                items.remove(id);
            }
        }
    }

    public int getCount(Long id) {
        return items.getOrDefault(id, 0);
    }


    public Map<Long, Integer> getItems() {
        return items;
    }

    @Override
    public Mono<OrdersDto> showCart() {
        Map<Long, Integer> cartMap = getItems();
        return itemRepository.findAllById(cartMap.keySet())
                .map(item -> {
                    int quantity = cartMap.get(item.getId());
                    item.setCount(quantity);
                    return item;
                })
                .collectList()
                .map(items -> {
                    long totalSum = items.stream()
                            .mapToLong(i -> i.getPrice() * i.getCount())
                            .sum();
                    return OrdersDto.builder()
                            .totalSum(totalSum)
                            .items(items)
                            .build();
                });
    }

    @Override
    public Mono<String> updateCart(Long id, String action) {
        if ("PLUS".equals(action)) {
            add(id);
        } else if ("MINUS".equals(action)) {
            remove(id);
        } else if ("DELETE".equals(action)) {

            while(getCount(id) > 0) remove(id);
        }
        return Mono.just("redirect:/cart/items");
    }

    @Override
    public void clear(){
        items.clear();
    }
}