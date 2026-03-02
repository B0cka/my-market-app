package com.b0cka;

import com.b0cka.cont.PostgreContainer1;
import com.b0cka.models.Item;
import com.b0cka.service.impl.CacheMainServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import reactor.test.StepVerifier;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ImportTestcontainers(PostgreContainer1.class)
class CacheMainServiceImplTest {

    @Autowired private CacheMainServiceImpl cacheService;

    @Test
    void testFullIndexingAndSearch() {
        Item item = new Item();
        item.setId(1L);
        item.setTitle("Black iPhone");
        item.setPrice(1000L);
        item.setDescription("Very powerful");

        cacheService.saveInCache(item)
                .then(cacheService.sortByPrice(List.of(item)))
                .then(cacheService.setForSearch(List.of(item)))
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();

        cacheService.getItemsForMainPage("iphone", "PRICE", 1, 5)
                .as(StepVerifier::create)
                .assertNext(found -> {
                    assertEquals("Black iPhone", found.getTitle());
                })
                .verifyComplete();
    }
}