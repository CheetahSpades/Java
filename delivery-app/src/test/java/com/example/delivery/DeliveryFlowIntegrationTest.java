package com.example.delivery;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * End-to-end tests that drive the REST API the way a client would:
 * create customer -> place order -> assign courier -> deliver.
 */
@SpringBootTest(properties = "app.seed-data=false")
@AutoConfigureMockMvc
class DeliveryFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private long createCustomer() throws Exception {
        String body = """
                {"name": "Ada Lovelace", "email": "ada@example.com",
                 "phone": "555-0100", "address": "1 Analytical Way"}
                """;
        String response = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private long placeOrder(long customerId) throws Exception {
        String body = """
                {"customerId": %d, "deliveryAddress": "42 Delivery Lane",
                 "items": [{"productName": "Pizza Margherita", "quantity": 2, "price": 9.50}]}
                """.formatted(customerId);
        String response = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PLACED"))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private long registerCourier(String name) throws Exception {
        String body = """
                {"name": "%s", "phone": "555-0200"}
                """.formatted(name);
        String response = mockMvc.perform(post("/api/couriers")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private void advance(long orderId, String status) throws Exception {
        mockMvc.perform(patch("/api/orders/" + orderId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"" + status + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(status));
    }

    private JsonNode courier(long courierId) throws Exception {
        String response = mockMvc.perform(get("/api/couriers"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        for (JsonNode node : objectMapper.readTree(response)) {
            if (node.get("id").asLong() == courierId) {
                return node;
            }
        }
        throw new AssertionError("Courier " + courierId + " not found in listing");
    }

    @Test
    void fullDeliveryFlowFromPlacedToDelivered() throws Exception {
        long customerId = createCustomer();
        long orderId = placeOrder(customerId);
        long courierId = registerCourier("Grace Hopper");

        advance(orderId, "CONFIRMED");

        mockMvc.perform(post("/api/orders/" + orderId + "/assign?courierId=" + courierId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courierId").value(courierId));
        org.junit.jupiter.api.Assertions.assertEquals("BUSY", courier(courierId).get("status").asText());

        advance(orderId, "PICKED_UP");
        advance(orderId, "OUT_FOR_DELIVERY");
        advance(orderId, "DELIVERED");

        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"))
                .andExpect(jsonPath("$.deliveredAt").isNotEmpty())
                .andExpect(jsonPath("$.total").value(19.00));

        // delivering the order frees the courier
        org.junit.jupiter.api.Assertions.assertEquals("AVAILABLE", courier(courierId).get("status").asText());
    }

    @Test
    void busyCourierCannotBeAssignedToASecondOrder() throws Exception {
        long customerId = createCustomer();
        long firstOrder = placeOrder(customerId);
        long secondOrder = placeOrder(customerId);
        long courierId = registerCourier("Busy Bee");

        mockMvc.perform(post("/api/orders/" + firstOrder + "/assign?courierId=" + courierId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/orders/" + secondOrder + "/assign?courierId=" + courierId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Courier " + courierId + " is not available"));
    }

    @Test
    void orderCannotBeCancelledAfterPickup() throws Exception {
        long customerId = createCustomer();
        long orderId = placeOrder(customerId);
        advance(orderId, "CONFIRMED");
        advance(orderId, "PICKED_UP");

        mockMvc.perform(delete("/api/orders/" + orderId))
                .andExpect(status().isConflict());
    }

    @Test
    void skippingLifecycleStepsIsRejected() throws Exception {
        long customerId = createCustomer();
        long orderId = placeOrder(customerId);

        mockMvc.perform(patch("/api/orders/" + orderId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"DELIVERED\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void placingAnOrderWithoutItemsFailsValidation() throws Exception {
        long customerId = createCustomer();
        String body = """
                {"customerId": %d, "deliveryAddress": "42 Delivery Lane", "items": []}
                """.formatted(customerId);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.items").exists());
    }

    @Test
    void unknownOrderReturns404() throws Exception {
        mockMvc.perform(get("/api/orders/999999"))
                .andExpect(status().isNotFound());
    }
}
