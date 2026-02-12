package com.b0cka;

import com.b0cka.cont.PostgreContainer1;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ImportTestcontainers(PostgreContainer1.class)
class MarketIntegrationTest{

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testItemsPage() {
        webTestClient.get().uri("/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(org.hamcrest.Matchers.containsString("Витрина магазина"));
    }

    @Test
    void testCartPage() {
        webTestClient.get().uri("/cart/items")
                .exchange()
                .expectStatus().isOk();
    }
}