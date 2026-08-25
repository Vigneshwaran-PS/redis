package com.redisconcepts.springcache;

import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/spring-cache")
public class OrderController {


    private final OrderService orderService;
    private final CacheManager cacheManager;

    public OrderController(OrderService orderService, CacheManager cacheManager) {
        this.orderService = orderService;
        this.cacheManager = cacheManager;
    }


    @GetMapping("/view-cache")
    public ResponseEntity getCacheData(){
        return ResponseEntity.ok(cacheManager.getCache("orders"));
    }

    @GetMapping("/orders")
    public ResponseEntity getOrders(){
        return ResponseEntity.ok(orderService.getOrders());
    }

    @GetMapping("/get-order")
    public ResponseEntity<Order> getOrder(@RequestParam String orderId){
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }


    @PostMapping("/save-order")
    public ResponseEntity saveOrder(@RequestBody Order order){
        return ResponseEntity.ok(orderService.saveOrder(order));
    }

}
