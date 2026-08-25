package com.redisconcepts.redisset;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/redis-set")
@Slf4j
public class RedisSetController {

    public final RedisTemplate<String,String> redisTemplate;
    private final String ACCEPTED_ORDERS = "ACCEPTED_ORDERS";
    private final String IN_PRE_ORDERS = "IN_PRE_ORDERS";

    public RedisSetController(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/update")
    public String updateOrder(@RequestParam String orderId, @RequestParam String status){
        try {


            if (StringUtils.isEmpty(orderId) || StringUtils.isEmpty(status)){
                return "invalid request";
            }

            Set<String> acceptedOrders = redisTemplate.opsForSet().members(ACCEPTED_ORDERS);
            Set<String> inpreOrders = redisTemplate.opsForSet().members(IN_PRE_ORDERS);
            log.info("ACCEPTED_ORDERS - {}", acceptedOrders);
            log.info("IN_PRE_ORDERS - {}", inpreOrders);

            if(status.equalsIgnoreCase("ACCEPT")){
                    redisTemplate.opsForSet().add(ACCEPTED_ORDERS,orderId);
                    return "Success";
            }

            if(status.equalsIgnoreCase("INPREP")){
                redisTemplate.opsForSet().add(IN_PRE_ORDERS,orderId);
                redisTemplate.opsForSet().remove(ACCEPTED_ORDERS, orderId);
                return "Success";
            }

            if(status.equalsIgnoreCase("COMPLETED")){
                redisTemplate.opsForSet().remove(IN_PRE_ORDERS,orderId);
                redisTemplate.opsForSet().remove(ACCEPTED_ORDERS, orderId);
                return "Success";
            }



        }catch (Exception e){
            log.info("Exception occured while getting otp {}", e.getMessage(),e);
        }
        return "Something went wrong. please try again later";
    }

}
