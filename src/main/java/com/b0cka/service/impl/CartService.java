package com.b0cka.service.impl;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class CartService {

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
}