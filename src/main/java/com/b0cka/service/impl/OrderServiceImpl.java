package com.b0cka.service.impl;

import com.b0cka.models.Item;
import com.b0cka.models.Order;
import com.b0cka.models.OrderItem;
import com.b0cka.repository.ItemRepository;
import com.b0cka.repository.OrderItemRepository;
import com.b0cka.repository.OrderRepository;
import com.b0cka.service.CartService;
import com.b0cka.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderItemRepository orderItemRepository;
    private record Line(Item item, int qty) {}

    @Override
    public Mono<Long> createOrder() {
        Map<Long, Integer> cartMap = cartService.getItems();
        if (cartMap.isEmpty()) {
            return Mono.empty();
        }

        return itemRepository.findAllById(cartMap.keySet())
                .map(item -> new Line(item, cartMap.get(item.getId())))

                .collectList()

                .flatMap(lines -> {
                    long totalSum = lines.stream()
                            .mapToLong(l -> l.item().getPrice() * l.qty())
                            .sum();

                    Order order = new Order();
                    order.setTotalSum(totalSum);


                    return orderRepository.save(order)
                            .flatMap(savedOrder -> {

                                Flux<OrderItem> orderItemsToSave =
                                        Flux.fromIterable(lines)
                                                .map(l -> {
                                                    OrderItem oi = new OrderItem();
                                                    oi.setOrderId(savedOrder.getId());
                                                    oi.setItemId(l.item().getId());
                                                    oi.setQuantity(l.qty());
                                                    oi.setPricePerItem(l.item().getPrice());
                                                    return oi;
                                                });

                                return orderItemRepository.saveAll(orderItemsToSave)
                                        .then(Mono.just(savedOrder.getId()));
                            });
                })
                .doOnSuccess(id -> cartService.clear());
    }

    @Override
    public Mono<List<Order>> getAllOrders() {
        return orderRepository.findAll()
                .flatMap(order -> populateOrder(order))
                .collectList();
    }

    @Override
    public Mono<Order> getOrder(Long id) {
        return orderRepository.findById(id)
                .flatMap(order -> populateOrder(order));
    }


    private Mono<Order> populateOrder(Order order) {
        return orderItemRepository.findAllByOrderId(order.getId())
                .flatMap(orderItem ->
                        itemRepository.findById(orderItem.getItemId())
                                .map(realItem -> {
                                    orderItem.setItem(realItem);
                                    return orderItem;
                                })
                )
                .collectList()
                .map(items -> {
                    order.setItems(items);
                    return order;
                });
    }
}