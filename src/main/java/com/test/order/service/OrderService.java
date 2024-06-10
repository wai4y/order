package com.test.order.service;

import com.test.order.controller.dto.OrderStatus;
import com.test.order.controller.dto.request.TakeOrderRequest;
import com.test.order.controller.dto.response.OrderDto;
import com.test.order.controller.dto.request.OrderRequest;
import com.test.order.controller.dto.response.TakeOrderDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderService {
    /**
     * Place order with origin and destination coordinates
     * @param orderRequest the request info
     * @return created order info
     */
    OrderDto placeOrder(OrderRequest orderRequest);

    /**
     * Take order with order id, update order status to SUCCESS
     * @param id order id
     * @param status order status to be updated, only accept TAKEN
     * @return the updated order Success status
     */
    TakeOrderDto takeOrder(Long id, OrderStatus status);


    /**
     * Get order list with pagination
     * @param page page number, start from 1
     * @param limit page size
     * @return order list
     */
    List<OrderDto> getOrderList(Integer page, Integer limit);
}
