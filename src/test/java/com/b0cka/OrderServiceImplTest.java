package com.b0cka;

import com.b0cka.models.Item;
import com.b0cka.models.Order;
import com.b0cka.models.OrderItem;
import com.b0cka.repository.ItemRepository;
import com.b0cka.repository.OrderItemRepository;
import com.b0cka.repository.OrderRepository;
import com.b0cka.service.CartService;
import com.b0cka.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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
    void getOrder_ShouldUseSingleQueryForItems() {
        Order order = new Order(); order.setId(1L);
        OrderItem oi = new OrderItem(); oi.setItemId(10L); oi.setOrderId(1L);
        Item item = new Item(); item.setId(10L); item.setTitle("Test");

        when(orderRepository.findById(1L)).thenReturn(Mono.just(order));
        when(orderItemRepository.findAllByOrderId(1L)).thenReturn(Flux.just(oi));
        when(itemRepository.findAllById(any(Iterable.class))).thenReturn(Flux.just(item));

        orderService.getOrder(1L)
                .as(StepVerifier::create)
                .assertNext(result -> {
                    assertEquals(1, result.getItems().size());
                    verify(itemRepository, times(1)).findAllById(any(Iterable.class)); // Проверка на отсутствие N+1
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("createOrder: Успешное создание заказа из корзины")
    void createOrder_FullFlow() {
        when(cartService.getItems()).thenReturn(java.util.Map.of(1L, 2));

        Item mockItem = new Item();
        mockItem.setId(1L);
        mockItem.setPrice(500L);

        when(itemRepository.findAllById(any(Iterable.class))).thenReturn(Flux.just(mockItem));

        Order savedOrder = new Order();
        savedOrder.setId(777L);
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(savedOrder));

        when(orderItemRepository.saveAll(any(Flux.class))).thenReturn(Flux.empty());

        orderService.createOrder()
                .as(StepVerifier::create)
                .expectNext(777L) // Должен вернуть ID заказа
                .verifyComplete();

        verify(orderRepository).save(argThat(o -> o.getTotalSum() == 1000L));
        verify(cartService).clear();
    }

}