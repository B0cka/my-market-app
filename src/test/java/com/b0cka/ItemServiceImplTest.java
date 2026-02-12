package com.b0cka;

import com.b0cka.dto.ItemsPageDto;
import com.b0cka.models.Item;
import com.b0cka.repository.ItemRepository;
import com.b0cka.service.CartService;
import com.b0cka.service.impl.ItemServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CartService cartService;
    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void getItems_ShouldReturnGridWithDummies() {
        Item i1 = new Item(); i1.setId(1L);
        Item i2 = new Item(); i2.setId(2L);

        when(itemRepository.searchNoSort(any(), anyInt(), anyLong()))
                .thenReturn(Flux.just(i1, i2));
        when(cartService.getCount(anyLong())).thenReturn(0);

        itemService.getItems("", "NO", 1, 3)
                .as(StepVerifier::create)
                .assertNext(dto -> {
                    assertEquals(1, dto.getGrid().size());
                    List<Item> firstRow = dto.getGrid().get(0);
                    assertEquals(3, firstRow.size());
                    assertEquals(-1L, firstRow.get(2).getId());
                })
                .verifyComplete();
    }
}