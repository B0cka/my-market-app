package com.b0cka.service.impl;

import com.b0cka.dto.CartItemsDto;
import com.b0cka.dto.OrdersDto;
import com.b0cka.repository.ItemRepository;
import com.b0cka.service.CacheCartService;
import com.b0cka.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final ItemRepository itemRepository;
    private final CacheCartService cartService;

    @Override
    public Mono<Void> add(Long id) {
       return cartService.add(id)
               .then();
    }

    @Override
    public Mono<Void> remove(Long id) {
        return cartService.remove(id)
                .then();
    }

    @Override
    public Mono<Integer> getCount(Long id) {
        return cartService.getCount(id);
    }

    @Override
    public Mono<Map<Long, Integer>> getItems() {
        return cartService.getItems()
                .map(CartItemsDto::getItemQuantities);
    }


    @Override
    public Mono<OrdersDto> showCart() {

        return getItems().flatMap(cartMap -> {
            if (cartMap.isEmpty()) {
                return Mono.just(new OrdersDto(new ArrayList<>(), 0L));
            }

            return itemRepository.findAllById(cartMap.keySet())
                    .map(item -> {
                        item.setCount(cartMap.get(item.getId()));
                        return item;
                    })
                    .collectList()
                    .map(itemsList -> {

                        long totalSum = itemsList.stream()
                                .mapToLong(i -> i.getPrice() * i.getCount())
                                .sum();
                        return OrdersDto.builder()
                                .totalSum(totalSum)
                                .items(itemsList)
                                .build();
                    });
        });
    }

    @Override
    public Mono<String> updateCart(Long id, String action) {
        Mono<Void> operation;

        if ("PLUS".equals(action)) {
            operation = add(id);
        } else if ("MINUS".equals(action)) {
            operation = remove(id);
        } else if ("DELETE".equals(action)) {
            operation = cartService.removeAll(id);
        } else {
            operation = Mono.empty();
        }

        return operation.thenReturn("redirect:/cart/items");
    }

    @Override
    public Mono<Void> clear() {
        return cartService.clean();
    }
}