package com.test.order.controller.dto.request;

import com.test.order.controller.dto.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TakeOrderRequest {

    @NotNull
    private OrderStatus status;
}
