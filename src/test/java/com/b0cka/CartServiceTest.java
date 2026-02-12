package com.b0cka;

import com.b0cka.models.Item;
import com.b0cka.repository.ItemRepository;
import com.b0cka.service.CartService;
import com.b0cka.service.impl.CartServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;


@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    void testAddAndCount() {

        cartService.add(1L);
        cartService.add(1L);


        Assertions.assertEquals(2, cartService.getCount(1L));
        Assertions.assertEquals(1, cartService.getItems().size());
    }

    @Test
    void testRemove() {
        cartService.add(1L);
        cartService.add(1L);

        cartService.remove(1L);
        Assertions.assertEquals(1, cartService.getCount(1L));

        cartService.remove(1L);
        Assertions.assertEquals(0, cartService.getCount(1L));
    }

    @Test
    void testShowCartCalculation() {

        cartService.add(10L);
        cartService.add(10L);

        Item mockItem = new Item();
        mockItem.setId(10L);
        mockItem.setPrice(100L);


        Mockito.when(itemRepository.findAllById(Mockito.any())).thenReturn(List.of(mockItem));


        var dto = cartService.showCart();


        Assertions.assertEquals(200L, dto.getTotalSum());
    }
}