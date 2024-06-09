package com.test.order.repository;

import com.test.order.model.OrderEntity;
import com.test.order.common.TestContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class OrderRepositoryTest extends TestContainerConfig {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void saveOrderSuccess(){
        OrderEntity entity = new OrderEntity();
        entity.setDistance(100);
        entity.setStatus("UNASSIGNED");
        OrderEntity savedEntity = orderRepository.save(entity);

        assertNotNull(savedEntity);
        assertEquals(savedEntity.getDistance(), 100);
        assertEquals(savedEntity.getStatus(),  "UNASSIGNED");
    }
}
