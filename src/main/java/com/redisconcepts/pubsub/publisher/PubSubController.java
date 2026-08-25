package com.redisconcepts.pubsub.publisher;


import com.redisconcepts.springcache.Order;
import com.redisconcepts.utils.JsonUtils;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pub-sub")
@Slf4j
public class PubSubController {

    private final RedisTemplate<String,String> redisTemplate;
    private final String NOTIFICATION_CHANNEL = "NOTIFICATION_CHANNEL";
    private final String SMS_CHANNEL = "SMS_CHANNEL";
    private final String KOT_CHANNEL = "KOT_CHANNEL";

    public PubSubController(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/update")
    public String updateOrder(@RequestParam String orderId, @RequestParam String status){
        try {
            if(StringUtils.isEmpty(orderId) || StringUtils.isEmpty(status)){
                return "Invalid request";
            }
            Order order = Order.builder()
                    .orderId(orderId)
                    .status(status)
                    .build();
            if(status.equalsIgnoreCase("ACCEPT")){
                redisTemplate.convertAndSend(NOTIFICATION_CHANNEL, JsonUtils.toJson(order));
                redisTemplate.convertAndSend(SMS_CHANNEL, JsonUtils.toJson(order));
                redisTemplate.convertAndSend(KOT_CHANNEL, JsonUtils.toJson(order));
            }

            if(status.equalsIgnoreCase("CANCEL")){
                redisTemplate.convertAndSend(NOTIFICATION_CHANNEL, JsonUtils.toJson(order));
            }

            return "Success";

        }catch (Exception e){
            log.info("Exception occured while updating the order status - {}",e.getMessage(),e);
        }
        return "Failed";
    }
}
