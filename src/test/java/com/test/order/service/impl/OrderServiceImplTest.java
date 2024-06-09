package com.test.order.service.impl;

import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.Distance;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.DistanceMatrixElementStatus;
import com.google.maps.model.DistanceMatrixRow;
import com.google.maps.model.LatLng;
import com.test.order.controller.dto.OrderStatus;
import com.test.order.controller.dto.request.OrderRequest;
import com.test.order.controller.dto.response.OrderDto;
import com.test.order.controller.dto.response.TakeOrderDto;
import com.test.order.exception.BusinessException;
import com.test.order.model.OrderEntity;
import com.test.order.repository.OrderRepository;
import com.test.order.service.OrderService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;


import java.util.Optional;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
    private static final String SUCCESS = "SUCCESS";

    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private GeoApiContext geoApiContext;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(orderRepository, geoApiContext, redissonClient);
    }


    @Test
    @DisplayName("Place order success")
    void placeOrderSuccess() throws Exception {
        //given
        OrderRequest request = OrderRequest.builder()
                .origin(new String[]{"22", "22"})
                .destination(new String[]{"90", "90"})
                .build();

        OrderEntity entity = new OrderEntity();
        entity.setId(1L);
        entity.setStatus("UNASSIGNED");
        entity.setDistance(1000);

        DistanceMatrixRow[] rows = buildDistanceMatrixRow(1000L, DistanceMatrixElementStatus.OK);
        DistanceMatrix distanceMatrix = new DistanceMatrix(request.getOrigin(), request.getDestination(), rows);
        DistanceMatrixApiRequest distanceMatrixApiRequest = mock(DistanceMatrixApiRequest.class);

        OrderDto orderDto;
        try(MockedStatic<DistanceMatrixApi> mocked = mockStatic(DistanceMatrixApi.class)) {
            // when
            mocked.when(() -> DistanceMatrixApi.newRequest(geoApiContext))
                            .thenReturn(distanceMatrixApiRequest);
            when(distanceMatrixApiRequest
                    .origins(any(LatLng.class)))
                    .thenReturn(distanceMatrixApiRequest);
            when(distanceMatrixApiRequest
                    .destinations(any(LatLng.class)))
                    .thenReturn(distanceMatrixApiRequest);
            when(distanceMatrixApiRequest.awaitIgnoreError()).thenReturn(distanceMatrix);
            when(orderRepository.save(any()))
                    .thenReturn(entity);
            orderDto = orderService.placeOrder(request);
        }

        // then
        Assertions.assertNotNull(orderDto);
        assertEquals(1000, orderDto.getDistance());
        assertEquals("UNASSIGNED", orderDto.getStatus());
    }


    @Test
    @DisplayName("Place order failed when distance matrix api failed")
    void placeOrderFailedWhenDistanceMatrixApiFailed() throws Exception {
        //given
        OrderRequest request = OrderRequest.builder()
                .origin(new String[]{"22", "22"})
                .destination(new String[]{"90", "90"})
                .build();
        DistanceMatrixApiRequest distanceMatrixApiRequest = mock(DistanceMatrixApiRequest.class);

        try(MockedStatic<DistanceMatrixApi> mocked = mockStatic(DistanceMatrixApi.class)) {
            // when
            mocked.when(() -> DistanceMatrixApi.newRequest(geoApiContext))
                    .thenReturn(distanceMatrixApiRequest);
            when(distanceMatrixApiRequest
                    .origins(any(LatLng.class)))
                    .thenReturn(distanceMatrixApiRequest);
            when(distanceMatrixApiRequest
                    .destinations(any(LatLng.class)))
                    .thenReturn(distanceMatrixApiRequest);
            when(distanceMatrixApiRequest.awaitIgnoreError()).thenReturn(null);
            // then
            assertThrows(BusinessException.class, () -> orderService.placeOrder(request));
        }
    }

    @Test
    @DisplayName("Place order failed when distance big than Integer.MAX_VALUE")
    void placeOrderFailedWhenDistanceTooBig() throws Exception {
        //given
        OrderRequest request = OrderRequest.builder()
                .origin(new String[]{"22", "22"})
                .destination(new String[]{"90", "90"})
                .build();
        DistanceMatrixApiRequest distanceMatrixApiRequest = mock(DistanceMatrixApiRequest.class);
        DistanceMatrixRow[] rows = buildDistanceMatrixRow(12345678901L, DistanceMatrixElementStatus.OK);
        DistanceMatrix distanceMatrix = new DistanceMatrix(request.getOrigin(), request.getDestination(), rows);
        try(MockedStatic<DistanceMatrixApi> mocked = mockStatic(DistanceMatrixApi.class)) {
            // when
            mocked.when(() -> DistanceMatrixApi.newRequest(geoApiContext))
                    .thenReturn(distanceMatrixApiRequest);
            when(distanceMatrixApiRequest
                    .origins(any(LatLng.class)))
                    .thenReturn(distanceMatrixApiRequest);
            when(distanceMatrixApiRequest
                    .destinations(any(LatLng.class)))
                    .thenReturn(distanceMatrixApiRequest);
            when(distanceMatrixApiRequest.awaitIgnoreError()).thenReturn(distanceMatrix);
            // then
            assertThrows(BusinessException.class, () -> orderService.placeOrder(request));
        }
    }


    @Test
    @DisplayName("Place order failed when element status not OK")
    void placeOrderFailedWhenElementStatusNotOK() throws Exception {
        //given
        OrderRequest request = OrderRequest.builder()
                .origin(new String[]{"22", "22"})
                .destination(new String[]{"90", "90"})
                .build();
        DistanceMatrixApiRequest distanceMatrixApiRequest = mock(DistanceMatrixApiRequest.class);
        DistanceMatrixRow[] rows = buildDistanceMatrixRow(123456L, DistanceMatrixElementStatus.NOT_FOUND);
        DistanceMatrix distanceMatrix = new DistanceMatrix(request.getOrigin(), request.getDestination(), rows);
        try(MockedStatic<DistanceMatrixApi> mocked = mockStatic(DistanceMatrixApi.class)) {
            // when
            mocked.when(() -> DistanceMatrixApi.newRequest(geoApiContext))
                    .thenReturn(distanceMatrixApiRequest);
            when(distanceMatrixApiRequest
                    .origins(any(LatLng.class)))
                    .thenReturn(distanceMatrixApiRequest);
            when(distanceMatrixApiRequest
                    .destinations(any(LatLng.class)))
                    .thenReturn(distanceMatrixApiRequest);
            when(distanceMatrixApiRequest.awaitIgnoreError()).thenReturn(distanceMatrix);
            // then
            assertThrows(BusinessException.class, () -> orderService.placeOrder(request));
        }
    }


    private DistanceMatrixRow @NotNull [] buildDistanceMatrixRow(Long distance, DistanceMatrixElementStatus status) {
        DistanceMatrixRow[] rows = new DistanceMatrixRow[1];
        DistanceMatrixRow row = new DistanceMatrixRow();

        DistanceMatrixElement[] elements = new DistanceMatrixElement[1];
        DistanceMatrixElement element = new DistanceMatrixElement();
        element.status = status;
        element.distance = new Distance();
        element.distance.inMeters = distance;
        elements[0] = element;
        row.elements = elements;
        rows[0] = row ;
        return rows;
    }



    /*************************************************************************/


    @Test
    void takeOrderSuccess() {
        //given
        Long id = 123L;
        OrderStatus status = OrderStatus.TAKEN;
        RLock lock = mock(RLock.class);
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(123L);
        orderEntity.setStatus(OrderStatus.UNASSIGNED.name());
        orderEntity.setDistance(1000);

        //when
        when(redissonClient.getLock(anyString()))
                .thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        when(orderRepository.findById(id)).thenReturn(Optional.of(orderEntity));
//        doNothing().when(orderRepository).save(any());

        TakeOrderDto result = orderService.takeOrder(id, status);

        //then
        Assertions.assertEquals(SUCCESS, result.getStatus());
    }

    @Test
    @DisplayName("Take order failed when order not found")
    void takeOrderFailedWhenOrderNotFound() {
        //given
        Long id = 123L;
        OrderStatus status = OrderStatus.TAKEN;
        RLock lock = mock(RLock.class);

        //when
        when(redissonClient.getLock(anyString()))
                .thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        //then
        assertThrows(BusinessException.class, () -> orderService.takeOrder(id, status));
    }


    @Test
    @DisplayName("Take order failed when order has been taken")
    void takeOrderFailedWhenOrderHasBeenTaken() {
        //given
        Long id = 123L;
        OrderStatus status = OrderStatus.TAKEN;
        RLock lock = mock(RLock.class);

        OrderEntity orderEntity = mock(OrderEntity.class);

        //when
        when(redissonClient.getLock(anyString()))
                .thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        when(orderRepository.findById(id)).thenReturn(Optional.ofNullable(orderEntity));
        when(orderEntity.getStatus()).thenReturn(OrderStatus.TAKEN.name());

        //then
        assertThrows(BusinessException.class, () -> orderService.takeOrder(id, status));
    }

    @Test
    @DisplayName("Take order failed when status is not taken")
    void takeOrderFailedWhenStatusIsNotTaken() {
        //given
        Long id = 123L;
        OrderStatus status = OrderStatus.UNASSIGNED;

        // when & then
        assertThrows(BusinessException.class, () -> orderService.takeOrder(id, status));
    }

    @Test
    @DisplayName("Take order failed when update status failed")
    void takeOrderFailedWhenUpdateStatusFailed() {
        //given
        Long id = 123L;
        OrderStatus status = OrderStatus.TAKEN;
        RLock lock = mock(RLock.class);

        OrderEntity orderEntity = mock(OrderEntity.class);

        //when
        when(redissonClient.getLock(anyString()))
                .thenReturn(lock);
        when(lock.tryLock()).thenReturn(true);
        when(orderRepository.findById(id)).thenReturn(Optional.ofNullable(orderEntity));
        when(orderEntity.getStatus()).thenReturn(OrderStatus.UNASSIGNED.name());

        when(orderRepository.save(any())).thenThrow(new RuntimeException("Update status failed"));

        //then
        assertThrows(BusinessException.class, () -> orderService.takeOrder(id, status));
    }



}