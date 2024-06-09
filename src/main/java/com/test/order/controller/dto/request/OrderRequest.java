package com.test.order.controller.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.impl.StringArraySerializer;
import com.test.order.common.ValidLatAndLon;
import com.test.order.config.StringArrayDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequest {

    @ValidLatAndLon
    @JsonDeserialize(using = StringArrayDeserializer.class)
    private String[] origin;

    @ValidLatAndLon
    @JsonDeserialize(using = StringArrayDeserializer.class)
    private String[] destination;
}
