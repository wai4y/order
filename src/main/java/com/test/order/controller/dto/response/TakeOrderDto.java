package com.test.order.controller.dto.response;

import com.test.order.controller.dto.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TakeOrderDto {
    private String status;
    public TakeOrderDto(String status) {
        this.status = status;
    }
}
