package com.redisconcepts.rediskeyvalue;

import com.redisconcepts.springcache.Order;
import com.redisconcepts.utils.JsonUtils;
import io.micrometer.common.util.StringUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/redis-string")
@Slf4j
public class RedisStringContoller {


    public List<Order> orders = new ArrayList<>();
    private final RedisTemplate<String,String> redisTemplate;
    private final String ALL_ORDER = "ALL_ORDER";

    public RedisStringContoller(RedisTemplate<String, String> redisTemplate){
        this.redisTemplate = redisTemplate;

        orders.add(
                Order.builder()
                        .orderId("123")
                        .name("viki")
                        .price("100")
                        .build()
        );
        orders.add(
                Order.builder()
                        .orderId("456")
                        .name("Sai")
                        .price("200")
                        .build()
        );
        orders.add(
                Order.builder()
                        .orderId("789")
                        .name("Ram")
                        .price("300")
                        .build()
        );
    }


    @GetMapping("/orders")
    public List<Order> getOrders(){
        try {
            String redisDate = redisTemplate.opsForValue().get(ALL_ORDER);
            if(StringUtils.isNotEmpty(redisDate)){
                return (List<Order>) JsonUtils.fromJaon(redisDate, List.class);
            }

            Thread.sleep(Duration.ofSeconds(3));
            redisTemplate.opsForValue().set(ALL_ORDER,JsonUtils.toJson(orders),Duration.ofSeconds(20));
            return orders;
        }catch (Exception e){
            log.info("Exception occured while fetching all order - {}",e.getMessage(),e);
        }
        return new ArrayList<>();
    }


    @GetMapping("/order")
    public Order getOrder(@RequestParam String orderId){
        try {
            String redisDate = redisTemplate.opsForValue().get(orderId);
            if(StringUtils.isNotEmpty(redisDate)){
                return (Order) JsonUtils.fromJaon(redisDate, Order.class);
            }

            Thread.sleep(Duration.ofSeconds(3));
            Order order = orders.stream().filter(o -> o.getOrderId().equalsIgnoreCase(orderId))
                    .findFirst()
                    .orElse(null);
            redisTemplate.opsForValue().set(orderId,JsonUtils.toJson(order),Duration.ofSeconds(20));
            return order;
        }catch (Exception e){
            log.info("Exception occured while fetching order - {}",e.getMessage(),e);
        }
        return null;
    }


    @PostMapping("/save")
    public APIResponse saveOrder(@RequestBody Order order){
        try {
            String redisDate = redisTemplate.opsForValue().get(order.getOrderId());
            if(StringUtils.isNotEmpty(redisDate)){
                redisTemplate.delete(order.getOrderId());
                redisTemplate.delete(order.getOrderId());
            }

            orders.replaceAll(o -> {
                if(o.getOrderId().equalsIgnoreCase(order.getOrderId())){
                    return order;
                }
                return o;
            });

            return new APIResponse("User saved successfully",200);
        }catch (Exception e){
            log.info("Exception occured while saving/updating order - {}",e.getMessage(),e);
            return new APIResponse("Failed to save user",400);
        }
    }

    public record APIResponse(String message, int status){}
}
