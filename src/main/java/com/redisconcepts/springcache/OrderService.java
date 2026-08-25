package com.redisconcepts.springcache;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final String ALL_ORDEERS = "'ALL_ORDERS'";

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }


    @Cacheable(value = "orders", key = ALL_ORDEERS)
    public @Nullable Object getOrders() {
        List<Order> orders = new ArrayList<>();
        try {
            Thread.sleep(Duration.ofSeconds(3));
            orders = orderRepository.getOrders();
        }catch (Exception e){
            log.info("Exception occured while fetching the orders {}", e.getMessage(), e);
        }
        return orders;
    }

    @Cacheable(value = "orders", key = "#orderId")
    public @Nullable Order getOrder(String orderId) {
        Order order = null;
        try {
            Thread.sleep(Duration.ofSeconds(3));
            order = orderRepository.getOrders()
                    .stream()
                    .filter(o -> o.getOrderId().equalsIgnoreCase(orderId))
                    .findFirst()
                    .orElse(null);
        }catch (Exception e){
            log.info("Exception occured while fetching the order {} {} ",orderId, e.getMessage(), e);
        }
        return order;
    }


    @Caching(
            evict = {
                    @CacheEvict(value = "orders", key = ALL_ORDEERS),
                    @CacheEvict(value = "orders", key = "#order.orderId"),
            }
    )
    public @Nullable Order saveOrder(Order order) {
        try {

            orderRepository.getOrders().replaceAll(o -> {
                if(o.getOrderId().equalsIgnoreCase(order.getOrderId())){
                    return order;
                }
                return o;
            });

        }catch (Exception e){
            log.info("Exception occured while saving the order {} {} ",order.getOrderId(), e.getMessage(), e);
        }
        return order;
    }
}
