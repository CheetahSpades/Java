package com.example.delivery.config;

import com.example.delivery.model.Courier;
import com.example.delivery.model.Customer;
import com.example.delivery.model.Order;
import com.example.delivery.model.OrderItem;
import com.example.delivery.model.OrderStatus;
import com.example.delivery.repository.CourierRepository;
import com.example.delivery.repository.CustomerRepository;
import com.example.delivery.repository.OrderRepository;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Loads demo data on startup so the app is usable straight away.
 * Disable with app.seed-data=false (tests do this).
 */
@Configuration
@ConditionalOnProperty(name = "app.seed-data", havingValue = "true")
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    CommandLineRunner seedData(CustomerRepository customers, CourierRepository couriers, OrderRepository orders) {
        return args -> {
            if (customers.count() > 0) {
                return; // already seeded (e.g. persistent database)
            }

            Customer alice = customers.save(
                    new Customer("Alice Johnson", "alice@example.com", "555-0101", "12 Maple Street"));
            Customer bob = customers.save(
                    new Customer("Bob Smith", "bob@example.com", "555-0102", "34 Oak Avenue"));

            couriers.save(new Courier("Charlie Rider", "555-0201"));
            couriers.save(new Courier("Dana Wheels", "555-0202"));

            Order pending = new Order(alice, alice.getAddress());
            pending.addItem(new OrderItem("Pizza Margherita", 1, new BigDecimal("9.50")));
            pending.addItem(new OrderItem("Garlic Bread", 2, new BigDecimal("3.25")));
            orders.save(pending);

            Order confirmed = new Order(bob, bob.getAddress());
            confirmed.addItem(new OrderItem("Sushi Box", 1, new BigDecimal("14.00")));
            confirmed.changeStatus(OrderStatus.CONFIRMED);
            orders.save(confirmed);

            log.info("Seeded demo data: 2 customers, 2 couriers, 2 orders");
        };
    }
}
