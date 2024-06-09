package com.test.order.service.impl;

import com.google.common.primitives.Ints;
import com.google.maps.model.DistanceMatrixElementStatus;
import com.google.maps.model.LatLng;
import com.test.order.controller.dto.OrderStatus;
import com.test.order.controller.dto.response.OrderDto;
import com.test.order.controller.dto.request.OrderRequest;
import com.test.order.controller.dto.response.TakeOrderDto;
import com.test.order.exception.BusinessException;
import com.test.order.model.OrderEntity;
import com.test.order.repository.OrderRepository;
import com.test.order.service.OrderService;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final String SUCCESS = "SUCCESS";

    private final OrderRepository orderRepository;

    private final GeoApiContext geoApiContext;

    private final RedissonClient redissonClient;

    @Override
    @Transactional
    public OrderDto placeOrder(OrderRequest orderRequest) {
        try {
            var distanceMatrix = DistanceMatrixApi.newRequest(geoApiContext)
                    .origins(
                            new LatLng(
                                    Double.parseDouble(orderRequest.getOrigin()[0]),
                                    Double.parseDouble(orderRequest.getOrigin()[1])))
                    .destinations(
                            new LatLng(
                                    Double.parseDouble(orderRequest.getDestination()[0]),
                                    Double.parseDouble(orderRequest.getDestination()[1])
                            )
                    )
                    .awaitIgnoreError();

            long inMeters = Optional.ofNullable(distanceMatrix)
                    .filter(d -> d.rows.length > 0)
                    .map(d -> d.rows[0])
                    .filter(r -> r.elements.length > 0)
                    .map(r -> r.elements[0])
                    .filter(e -> DistanceMatrixElementStatus.OK.equals(e.status))
                    .map(e -> e.distance)
                    .map(d -> d.inMeters)
                    .orElseThrow(() -> new BusinessException("No route can be found between the origin and destination"));

            var orderEntity = new OrderEntity();
            orderEntity.setDistance(Ints.checkedCast(inMeters));
            orderEntity.setStatus(OrderStatus.UNASSIGNED.name());
            OrderEntity order = orderRepository.save(orderEntity);

            return OrderDto.builder()
                    .id(order.getId())
                    .distance(order.getDistance())
                    .status(OrderStatus.UNASSIGNED.name())
                    .build();
        } catch (Exception e) {
            log.error("Place order error", e);
            throw new BusinessException("Place order error, reason: ", e.getMessage());
        }
    }

    @Override
    @Transactional
    public TakeOrderDto takeOrder(Long id, OrderStatus status) {
        if(!OrderStatus.TAKEN.equals(status)) {
            throw new BusinessException("Invalid status: "+status);
        }
        // get order, update status
        var lock = redissonClient.getLock("lock:" + id);
        try {
            if (lock.tryLock()){
                var order = orderRepository.findById(id)
                        .orElseThrow(() -> new BusinessException("Order not found"));
                if (OrderStatus.UNASSIGNED.name().equals(order.getStatus())) {
                    order.setStatus(status.name());
                    orderRepository.save(order);
                    return new TakeOrderDto(SUCCESS);
                }
            }
        }catch (Exception e) {
            log.error("Take order error", e);
            throw new BusinessException("Take order error, reason: ", e.getMessage());
        } finally {
           if(lock.isLocked() && lock.isHeldByCurrentThread())lock.unlock();

        }
        throw new BusinessException("Order has been taken!");
    }

    @Override
    public List<OrderDto> getOrderList(Integer page, Integer limit) {
        if(limit == 0) return Collections.emptyList();
        Pageable pageable = PageRequest.of(page-1, limit, Sort.by("createdAt"));
        return orderRepository.findAll(pageable).map(orderEntity ->
                OrderDto.builder()
                        .id(orderEntity.getId())
                        .distance(orderEntity.getDistance())
                        .status(orderEntity.getStatus())
                        .build())
                .getContent();
    }


}
