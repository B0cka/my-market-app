package com.b0cka.service;

import com.b0cka.models.Order;

import java.util.List;

public interface OrderService {
    Long createOrder();

    List<Order> getAllOrders();

    Order getOrder(Long id);

}
