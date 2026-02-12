package com.b0cka;

import com.b0cka.cont.PostgreContainer1;
import com.b0cka.models.Item;
import com.b0cka.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@ImportTestcontainers(PostgreContainer1.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ItemRepositoryDataJpaTest{

    @Autowired
    ItemRepository itemRepository;

    private Item item(String title, String description, long price) {
        Item i = new Item();
        i.setTitle(title);
        i.setDescription(description);
        i.setPrice(price);
        i.setImgPath("/images/x.jpg");
        return i;
    }

    @Test
    void search_byTitleOrDescription_ignoreCase_works() {
        itemRepository.save(item("iPhone 15", "Apple phone", 120_000));
        itemRepository.save(item("Samsung", "ANDROID PHONE", 50_000));
        itemRepository.save(item("Coffee", "drink", 10_000));

        var page = itemRepository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        "phone", "phone", PageRequest.of(0, 10)
                );

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent())
                .extracting(Item::getTitle)
                .containsExactlyInAnyOrder("iPhone 15", "Samsung");
    }

    @Test
    void paging_works() {
        for (int i = 1; i <= 12; i++) {
            itemRepository.save(item("Item " + i, "d", i));
        }

        var page1 = itemRepository.findAll(PageRequest.of(0, 5));
        var page3 = itemRepository.findAll(PageRequest.of(2, 5));

        assertThat(page1.getContent()).hasSize(5);
        assertThat(page1.getTotalElements()).isEqualTo(12);

        assertThat(page3.getContent()).hasSize(2);
    }

    @Test
    void sorting_byPrice_asc_works() {
        itemRepository.save(item("A", "d", 300));
        itemRepository.save(item("B", "d", 100));
        itemRepository.save(item("C", "d", 200));

        var page = itemRepository.findAll(PageRequest.of(0, 10, Sort.by("price").ascending()));

        assertThat(page.getContent()).extracting(Item::getPrice)
                .containsExactly(100L, 200L, 300L);
    }
}