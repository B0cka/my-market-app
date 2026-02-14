package com.b0cka;

import com.b0cka.models.Item;
import com.b0cka.repository.ItemRepository;
import com.b0cka.service.impl.CartServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock private ItemRepository itemRepository;
    @InjectMocks private CartServiceImpl cartService;

    @Test
    void testConcurrentAdd() {
        int iterations = 1000;
        Flux.range(1, iterations)
                .parallel()
                .runOn(Schedulers.parallel())
                .doOnNext(i -> cartService.add(1L))
                .sequential()
                .blockLast();

        assertEquals(iterations, cartService.getCount(1L));
    }

    @Test
    void testShowCartCalculation() {
        cartService.add(1L);
        Item item = new Item(); item.setId(1L); item.setPrice(150L);

        when(itemRepository.findAllById(any(Iterable.class))).thenReturn(Flux.just(item));

        cartService.showCart()
                .as(StepVerifier::create)
                .assertNext(dto -> {
                    assertEquals(150L, dto.getTotalSum());
                    assertEquals(1, dto.getItems().get(0).getCount());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("updateCart: Команда DELETE полностью удаляет товар")
    void updateCart_DeleteAction() {
        cartService.add(5L);
        cartService.add(5L);

        cartService.updateCart(5L, "DELETE")
                .as(StepVerifier::create)
                .expectNext("redirect:/cart/items")
                .verifyComplete();

        assertEquals(0, cartService.getCount(5L));
    }

    @Test
    @DisplayName("updateCart: Команда MINUS уменьшает количество")
    void updateCart_MinusAction() {
        cartService.add(1L);
        cartService.add(1L);

        cartService.updateCart(1L, "MINUS")
                .as(StepVerifier::create)
                .expectNext("redirect:/cart/items")
                .verifyComplete();

        assertEquals(1, cartService.getCount(1L));
    }
}