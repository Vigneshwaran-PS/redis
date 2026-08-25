package com.redisconcepts.redislist;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/redis-list")
@Slf4j
public class RedisListController {

    public final RedisTemplate<String,String> redisTemplate;
    private final String ACCEPTED_ORDERS = "ACCEPTED_ORDERS";
    private final String IN_PRE_ORDERS = "IN_PRE_ORDERS";

    public RedisListController(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/update")
    public String updateOrder(@RequestParam String orderId, @RequestParam String status){
        try {


            if (StringUtils.isEmpty(orderId) || StringUtils.isEmpty(status)){
                return "invalid request";
            }

            List<String> acceptedOrders = redisTemplate.opsForList().range(ACCEPTED_ORDERS,0,-1);
            List<String> inpreOrders = redisTemplate.opsForList().range(IN_PRE_ORDERS,0,-1);
            log.info("ACCEPTED_ORDERS - {}", acceptedOrders);
            log.info("IN_PRE_ORDERS - {}", inpreOrders);

            if(status.equalsIgnoreCase("ACCEPT")){
                    redisTemplate.opsForList().rightPush(ACCEPTED_ORDERS,orderId);
                    return "Success";
            }

            if(status.equalsIgnoreCase("INPREP")){
                redisTemplate.opsForList().rightPush(IN_PRE_ORDERS,orderId);
                redisTemplate.opsForList().remove(ACCEPTED_ORDERS,100, orderId);
                return "Success";
            }

            if(status.equalsIgnoreCase("COMPLETED")){
                redisTemplate.opsForList().remove(IN_PRE_ORDERS,100, orderId);
                redisTemplate.opsForList().remove(ACCEPTED_ORDERS,100, orderId);
                return "Success";
            }



        }catch (Exception e){
            log.info("Exception occured while getting otp {}", e.getMessage(),e);
        }
        return "Something went wrong. please try again later";
    }

}
