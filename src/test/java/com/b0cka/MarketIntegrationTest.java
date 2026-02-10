package com.b0cka;

import com.b0cka.cont.PostgreContainer1;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ImportTestcontainers(PostgreContainer1.class)
class MarketIntegrationTest{

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testMainPageLoads() throws Exception {

        mockMvc.perform(get("/items"))
                .andExpect(view().name("items"))
                .andExpect(model().attributeExists("items", "paging"));
    }

    @Test
    void testCartPageLoads() throws Exception {
        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));
    }
}