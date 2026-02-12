package com.b0cka;

import com.b0cka.cont.PostgreContainer1;
import com.b0cka.models.Item;
import com.b0cka.models.Order;
import com.b0cka.models.OrderItem;
import com.b0cka.repository.ItemRepository;
import com.b0cka.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@ImportTestcontainers(PostgreContainer1.class)
class OrderRepositoryDataJpaTest {

    @Autowired
    OrderRepository orderRepository;
    @Autowired
    ItemRepository itemRepository;
    @Autowired TestEntityManager em;

    private Item item(String title, long price) {
        Item i = new Item();
        i.setTitle(title);
        i.setDescription("d");
        i.setPrice(price);
        i.setImgPath("/images/x.jpg");
        return itemRepository.save(i);
    }

    @Test
    void saveOrder_cascadesOrderItems() {
        Item iphone = item("iPhone", 100);
        Item coffee = item("Coffee", 10);

        Order order = new Order();
        order.setItems(new ArrayList<>());
        order.setTotalSum(120L);

        OrderItem oi1 = new OrderItem();
        oi1.setOrder(order);
        oi1.setItem(iphone);
        oi1.setQuantity(1);
        oi1.setPricePerItem(iphone.getPrice());

        OrderItem oi2 = new OrderItem();
        oi2.setOrder(order);
        oi2.setItem(coffee);
        oi2.setQuantity(2);
        oi2.setPricePerItem(coffee.getPrice());

        order.getItems().add(oi1);
        order.getItems().add(oi2);

        Order saved = orderRepository.save(order);

        em.clear();

        Order found = orderRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getTotalSum()).isEqualTo(120);
        assertThat(found.getItems()).hasSize(2);

        assertThat(found.getItems())
                .extracting(oi -> oi.getItem().getTitle())
                .containsExactlyInAnyOrder("iPhone", "Coffee");

        assertThat(found.getItems())
                .extracting(OrderItem::getQuantity)
                .containsExactlyInAnyOrder(1, 2);
    }
}