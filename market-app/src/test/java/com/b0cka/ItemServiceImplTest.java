package com.b0cka.service;

import com.b0cka.dto.ItemsPageDto;
import com.b0cka.models.Item;
import com.b0cka.repository.ItemRepository;
import com.b0cka.service.impl.ItemServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock private ItemRepository itemRepository;
    @Mock private CartService cartService;
    @Mock private CacheMainService cacheMainService;

    @InjectMocks private ItemServiceImpl itemService;

    @Test
    @DisplayName("getItems: Корректная нарезка сетки и заглушки")
    void getItems_GridLogic() {
        Item i1 = new Item(); i1.setId(1L);
        Item i2 = new Item(); i2.setId(2L);

        when(cacheMainService.getItemsForMainPage(any(), any(), anyInt(), anyInt()))
                .thenReturn(Flux.just(i1, i2));
        when(cartService.getItems()).thenReturn(Mono.just(Collections.emptyMap()));

        itemService.getItems("", "NO", 1, 3)
                .as(StepVerifier::create)
                .assertNext(dto -> {
                    assertEquals(1, dto.getGrid().size());
                    List<Item> row = dto.getGrid().get(0);
                    assertEquals(3, row.size());
                    assertEquals(-1L, row.get(2).getId());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getItem: Логика Cache-Aside (если в кеше пусто, идем в БД)")
    void getItem_CacheMiss_ShouldGoToDb() {
        Long id = 1L;
        Item item = new Item(); item.setId(id); item.setTitle("DB Item");

        when(cacheMainService.getItem(id)).thenReturn(Mono.empty());
        when(itemRepository.findById(id)).thenReturn(Mono.just(item));
        when(cacheMainService.saveInCache(any())).thenReturn(Mono.just(true));
        when(cartService.getCount(id)).thenReturn(Mono.just(0));

        itemService.getItem(id)
                .as(StepVerifier::create)
                .assertNext(res -> {
                    assertEquals("DB Item", res.getTitle());
                })
                .verifyComplete();

        verify(cacheMainService, times(1)).saveInCache(any());
    }
}