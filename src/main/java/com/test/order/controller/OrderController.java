package com.test.order.controller;

import com.test.order.controller.dto.OrderStatus;
import com.test.order.controller.dto.request.TakeOrderRequest;
import com.test.order.controller.dto.response.OrderDto;
import com.test.order.controller.dto.response.TakeOrderDto;
import com.test.order.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.test.order.controller.dto.request.OrderRequest;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderController {


    private final OrderService orderService;

    @PostMapping("/order")
    public ResponseEntity<OrderDto> placeOrder(@Valid @RequestBody OrderRequest orderRequest) {
        return ResponseEntity.ok(orderService.placeOrder(orderRequest));
    }

    @PatchMapping("/order/{id}")
    public ResponseEntity<TakeOrderDto> takeOrder(
            @PathVariable @Positive(message = "Invalid order id, should be positive") Long id,
            @Valid @RequestBody TakeOrderRequest request) {
        return ResponseEntity.ok(orderService.takeOrder(id, request.getStatus()));
    }

    @GetMapping("/order")
    public ResponseEntity<List<OrderDto>> getOrderList(
            @RequestParam("page") @Positive(message = "Invalid page number, should start from 1")
            Integer page,
            @RequestParam("limit") @Min(value = 0, message = "Invalid limit number, should start from 0")
            Integer limit
    ) {
        return ResponseEntity.ok(orderService.getOrderList(page, limit));
    }

}
