package com.b0cka.service.impl;

import com.b0cka.models.Item;
import com.b0cka.models.Order;
import com.b0cka.models.OrderItem;
import com.b0cka.repository.ItemRepository;
import com.b0cka.repository.OrderRepository;
import com.b0cka.service.CartService;
import com.b0cka.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public Long createOrder() {
        Map<Long, Integer> cartMap = cartService.getItems();
        if (cartMap.isEmpty()) return null;

        Order order = new Order();
        order.setItems(new ArrayList<>());

        long totalSum = 0;
        List<Item> items = itemRepository.findAllById(cartMap.keySet());

        for (Item item : items) {
            int quantity = cartMap.get(item.getId());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setItem(item);
            orderItem.setQuantity(quantity);
            orderItem.setPricePerItem(item.getPrice());

            order.getItems().add(orderItem);
            totalSum += item.getPrice() * quantity;
        }
        order.setTotalSum(totalSum);

        Order savedOrder = orderRepository.save(order);

        cartService.clear();

        return savedOrder.getId();
    }


    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));
    }
}