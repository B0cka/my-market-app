package com.b0cka;

import com.b0cka.cont.PostgreContainer1;
import com.b0cka.models.Item;
import com.b0cka.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataR2dbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportTestcontainers(PostgreContainer1.class)
class ItemRepositoryR2dbcTest{

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void testSearchByTitle() {
        Item item = new Item();
        item.setTitle("Super Phone");
        item.setPrice(100L);

        itemRepository.save(item)
                .thenMany(itemRepository.searchAlpha("Super", 10, 0))
                .as(StepVerifier::create)
                .assertNext(found -> {
                    assertEquals("Super Phone", found.getTitle());
                })
                .verifyComplete();
    }
}