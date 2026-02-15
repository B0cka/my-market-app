package com.b0cka.service;

import com.b0cka.models.Item;
import com.b0cka.models.Order;
import com.b0cka.models.OrderItem;
import com.b0cka.repository.ItemRepository;
import com.b0cka.repository.OrderItemRepository;
import com.b0cka.repository.OrderRepository;
import com.b0cka.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private CartService cartService;

    @InjectMocks private OrderServiceImpl orderService;

    @Test
    void getOrder_Fixing_N_Plus_1() {
        Order order = new Order(); order.setId(1L);
        // ОБЯЗАТЕЛЬНО даем ID, чтобы не было Duplicate Key Null
        Item item1 = new Item(); item1.setId(10L); item1.setTitle("T1");
        Item item2 = new Item(); item2.setId(11L); item2.setTitle("T2");

        OrderItem oi1 = new OrderItem(); oi1.setItemId(10L); oi1.setOrderId(1L);
        OrderItem oi2 = new OrderItem(); oi2.setItemId(11L); oi2.setOrderId(1L);

        when(orderRepository.findById(1L)).thenReturn(Mono.just(order));
        when(orderItemRepository.findAllByOrderId(1L)).thenReturn(Flux.just(oi1, oi2));
        when(itemRepository.findAllById(any(Iterable.class))).thenReturn(Flux.just(item1, item2));

        orderService.getOrder(1L)
                .as(StepVerifier::create)
                .assertNext(res -> {
                    assertEquals(2, res.getItems().size());
                    assertNotNull(res.getItems().get(0).getItem());
                })
                .verifyComplete();
    }

}