package com.b0cka;

import com.b0cka.models.Item;
import com.b0cka.models.Order;
import com.b0cka.repository.ItemRepository;
import com.b0cka.repository.OrderRepository;
import com.b0cka.service.CartService;
import com.b0cka.service.impl.CartServiceImpl;
import com.b0cka.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private CartService cartService;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("Создание заказа: Успешный сценарий")
    void createOrder_Success() {

        when(cartService.getItems()).thenReturn(Map.of(1L, 2));

        Item mockItem = new Item();
        mockItem.setId(1L);
        mockItem.setPrice(100L);
        mockItem.setTitle("Test Item");

        when(itemRepository.findAllById(any())).thenReturn(List.of(mockItem));

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(777L);
            return order;
        });

        Long orderId = orderService.createOrder();

        assertNotNull(orderId);
        assertEquals(777L, orderId);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertEquals(200L, savedOrder.getTotalSum());
        assertEquals(1, savedOrder.getItems().size());
        assertEquals(2, savedOrder.getItems().get(0).getQuantity());

        verify(cartService, times(1)).clear();
    }

    @Test
    @DisplayName("Создание заказа: Пустая корзина")
    void createOrder_EmptyCart() {
        when(cartService.getItems()).thenReturn(Collections.emptyMap());

        Long result = orderService.createOrder();

        assertNull(result);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Поиск заказа по ID: Найден")
    void getOrder_Found() {
        Order order = new Order();
        order.setId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order result = orderService.getOrder(1L);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("Поиск заказа по ID: Не найден (Ошибка)")
    void getOrder_NotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.getOrder(99L));
    }
}