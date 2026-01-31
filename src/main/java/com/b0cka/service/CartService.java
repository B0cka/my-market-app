package com.b0cka.service;

import com.b0cka.dto.OrdersDto;


import java.util.Map;

public interface CartService {

    void add(Long id);

    void remove(Long id);

    int getCount(Long id);

    Map<Long, Integer> getItems();

    OrdersDto showCart();

    String updateCart(Long id, String action);

    void clear();
}
