package com.b0cka.service.impl;

import com.b0cka.ex.BalanceError;
import com.b0cka.models.Item;
import com.b0cka.models.Order;
import com.b0cka.models.OrderItem;
import com.b0cka.repository.ItemRepository;
import com.b0cka.repository.OrderItemRepository;
import com.b0cka.repository.OrderRepository;
import com.b0cka.service.CartService;
import com.b0cka.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderItemRepository orderItemRepository;

   private final PaymentIntegrationService paymentIntegrationService;

    @Override
    @Transactional
    public Mono<Long> createOrder() {
        return cartService.getItems()
                .flatMap(cartMap -> {
                    if (cartMap.isEmpty()) return Mono.empty();

                    return itemRepository.findAllById(cartMap.keySet())
                            .collectList()
                            .flatMap(items -> {

                                double totalSum = items.stream()
                                        .mapToDouble(item -> item.getPrice() * cartMap.get(item.getId()))
                                        .sum();

                                return paymentIntegrationService.pay(totalSum)
                                        .flatMap(paymentSuccess -> {

                                            if (!paymentSuccess) {
                                                return Mono.error(new BalanceError("Оплата не прошла: недостаточно средств"));
                                            }

                                            Order order = new Order();
                                            order.setTotalSum((long) totalSum);

                                            return ReactiveSecurityContextHolder.getContext()
                                                    .map(ctx -> ctx.getAuthentication().getName())
                                                    .flatMap(username -> {
                                                        order.setUserId(username);
                                                        return orderRepository.save(order);
                                                    })
                                                    .flatMap(savedOrder -> {

                                                        List<OrderItem> orderItems = items.stream()
                                                                .map(item -> {
                                                                    OrderItem oi = new OrderItem();
                                                                    oi.setOrderId(savedOrder.getId());
                                                                    oi.setItemId(item.getId());
                                                                    oi.setQuantity(cartMap.get(item.getId()));
                                                                    oi.setPricePerItem(item.getPrice());
                                                                    return oi;
                                                                }).toList();

                                                        return orderItemRepository.saveAll(orderItems)
                                                                .then(Mono.just(savedOrder.getId()));
                                                    });
                                        });
                            });
                })
                .flatMap(orderId -> cartService.clear().thenReturn(orderId));
    }

    @Override
    public Mono<List<Order>> getAllOrders() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getName())
                .flatMapMany(username ->
                        orderRepository.findAllByUserId(username)
                )
                .flatMap(this::populateOrder)
                .collectList();
    }

    @Override
    public Mono<Order> getOrder(Long id) {
        return orderRepository.findById(id)
                .flatMap(order -> populateOrder(order));
    }


    private Mono<Order> populateOrder(Order order) {

        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getName())
                .flatMap(username -> {

                    if (!order.getUserId().equals(username)) {
                        return Mono.empty();
                    }

                    return orderItemRepository.findAllByOrderId(order.getId())
                            .collectList()
                            .flatMap(orderItems -> {

                                if (orderItems.isEmpty()) {
                                    return Mono.just(order);
                                }

                                List<Long> itemIds = orderItems.stream()
                                        .map(OrderItem::getItemId)
                                        .toList();

                                return itemRepository.findAllById(itemIds)
                                        .collectList()
                                        .map(realItems -> {

                                            Map<Long, Item> itemMap = realItems.stream()
                                                    .collect(Collectors.toMap(Item::getId, i -> i));

                                            for (OrderItem oi : orderItems) {
                                                oi.setItem(itemMap.get(oi.getItemId()));
                                            }

                                            order.setItems(orderItems);
                                            return order;
                                        });
                            });
                });
    }
}