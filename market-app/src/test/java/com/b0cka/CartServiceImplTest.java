package com.b0cka;

import com.b0cka.dto.CartItemsDto;
import com.b0cka.models.Item;
import com.b0cka.repository.ItemRepository;
import com.b0cka.service.CacheCartService;
import com.b0cka.service.impl.CartServiceImpl;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock private ItemRepository itemRepository;
    @Mock private CacheCartService cacheCartService;
    @InjectMocks private CartServiceImpl cartService;

    @Test
    @DisplayName("updateCart: Проверка действий")
    void updateCart_Actions() {

        when(cacheCartService.add(anyLong())).thenReturn(Mono.empty());
        when(cacheCartService.remove(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(cartService.updateCart(1L, "PLUS"))
                .expectNext("redirect:/cart/items")
                .verifyComplete();

        StepVerifier.create(cartService.updateCart(1L, "MINUS"))
                .expectNext("redirect:/cart/items")
                .verifyComplete();

        verify(cacheCartService, times(1)).add(1L);
        verify(cacheCartService, times(1)).remove(1L);
    }

    @Test
    @DisplayName("showCart: Расчет суммы заказа")
    void showCart_Calculation() {
        when(cacheCartService.getItems()).thenReturn(Mono.just(new CartItemsDto(java.util.Map.of(1L, 2))));

        Item item = new Item(); item.setId(1L); item.setPrice(100L);
        when(itemRepository.findAllById(any(Iterable.class))).thenReturn(Flux.just(item));

        cartService.showCart()
                .as(StepVerifier::create)
                .assertNext(dto -> {
                    assertEquals(200L, dto.getTotalSum());
                    assertEquals(2, dto.getItems().get(0).getCount());
                })
                .verifyComplete();
    }
}