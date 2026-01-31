package com.b0cka;

import com.b0cka.dto.ItemsPageDto;
import com.b0cka.models.Item;
import com.b0cka.repository.ItemRepository;
import com.b0cka.service.impl.CartServiceImpl;
import com.b0cka.service.impl.ItemServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private CartServiceImpl cartService;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    @DisplayName("getItems: Проверка разбиения на сетку и заглушек")
    void getItems_GridLogic() {

        List<Item> mockItems = List.of(
                new Item(), new Item(), new Item(), new Item()
        );
        Page<Item> page = new PageImpl<>(mockItems);

        when(itemRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(cartService.getCount(any())).thenReturn(0);

        ItemsPageDto result = itemService.getItems(null, "NO", 1, 5);

        List<List<Item>> grid = result.getGrid();

        assertEquals(2, grid.size());

        assertEquals(3, grid.get(0).size());

        assertEquals(3, grid.get(1).size());
        assertEquals(-1L, grid.get(1).get(1).getId());
        assertEquals(-1L, grid.get(1).get(2).getId());
    }

    @Test
    @DisplayName("itemPageAction: Проверка формирования ссылки редиректа")
    void itemPageAction_Redirect() {
        Long itemId = 10L;
        String action = "PLUS";

        String result = itemService.itemPageAction(itemId, action);

        assertTrue(result.contains("redirect:/"));
        assertTrue(result.contains("/" + itemId));
    }
}