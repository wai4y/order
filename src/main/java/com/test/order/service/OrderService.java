package com.test.order.service;

import com.test.order.controller.dto.OrderStatus;
import com.test.order.controller.dto.request.TakeOrderRequest;
import com.test.order.controller.dto.response.OrderDto;
import com.test.order.controller.dto.request.OrderRequest;
import com.test.order.controller.dto.response.TakeOrderDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderService {
    OrderDto placeOrder(OrderRequest orderRequest);

    TakeOrderDto takeOrder(Long id, OrderStatus status);

    List<OrderDto> getOrderList(Integer page, Integer limit);
}
