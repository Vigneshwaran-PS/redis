package com.redisconcepts.ratelimiting;

import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@RequestMapping("/rate-limit")
@RestController
@Slf4j
public class RateLimitController {


    public final RedisTemplate<String,String> redisTemplate;
    private final DefaultRedisScript<Long> redisScript;

    public RateLimitController(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;

        String script = """
                local now = tonumber(ARGV[1])
                local windowSeconds = tonumber(ARGV[2])
                local limit = tonumber(ARGV[3])
                local requestId = ARGV[4]

                local windowStart =
                    now - (windowSeconds * 1000)

                -- Remove requests outside the window
                redis.call(
                    'ZREMRANGEBYSCORE',
                    KEYS[1],
                    0,
                    windowStart
                )

                -- Count requests inside the window
                local count = redis.call(
                    'ZCARD',
                    KEYS[1]
                )

                -- Limit reached
                if count >= limit then
                    return 0
                end

                -- Add current request
                redis.call(
                    'ZADD',
                    KEYS[1],
                    now,
                    requestId
                )

                -- Remove the key after the window
                redis.call(
                    'EXPIRE',
                    KEYS[1],
                    windowSeconds
                )

                -- Request allowed
                return 1
                """;

        this.redisScript =
                new DefaultRedisScript<>(
                        script,
                        Long.class
                );
    }


    // Fixed window
    @PostMapping("/login")
    public String login(@RequestParam String customerPhone){
        try {

            log.info("Login API Customer Phone - {}",customerPhone);
            if(StringUtil.isEmpty(customerPhone)){
                return "Invalid request";
            }

            Long count = redisTemplate.opsForValue().increment(customerPhone);

            if(count !=null && count == 1){
                redisTemplate.expire(customerPhone, Duration.ofSeconds(30));
            }

            if(count >= 10){
                return "Limit reached, please try again later";
            }
            return "Success";

        }catch (Exception e){
            log.info("Exception occured while login for customer - {} {}",customerPhone,e.getMessage(),e);
            return "Something went wrong, please try again later.";
        }
    }


    // Sliding window
    @PostMapping("/login/v2")
    public String loginv2(@RequestParam String customerPhone){
        try {

            log.info("V2 Login API Customer Phone - {}",customerPhone);
            if(StringUtil.isEmpty(customerPhone)){
                return "Invalid request";
            }

            long now = System.currentTimeMillis();
            long start = now - (30*1000);
            System.err.println(start);
            redisTemplate.opsForZSet().removeRangeByScore(
                    customerPhone,
                    0,
                    start
            );

            Long count = redisTemplate.opsForZSet().zCard(customerPhone);
            if(count != null && count > 10){
                return "Too many attempts";
            }

            redisTemplate.opsForZSet().add(
                    customerPhone,
                    UUID.randomUUID().toString(),
                    now
            );

            redisTemplate.expire(customerPhone,Duration.ofSeconds(30));

            return "Success";
        }catch (Exception e){
            log.info("Exception occured while login for customer - {} {}",customerPhone,e.getMessage(),e);
            return "Something went wrong, please try again later.";
        }
    }


    // Sliding window + lua (prevent race condition)
    @PostMapping("/login/v3")
    public String loginv3(@RequestParam String customerPhone){
        try {

            log.info("V3 Login API Customer Phone - {}",customerPhone);
            if(StringUtil.isEmpty(customerPhone)){
                return "Invalid request";
            }

            Long result = redisTemplate.execute(
                    redisScript,
                    List.of(customerPhone),
                    String.valueOf(System.currentTimeMillis()),// ARG 1
                    "30", // ARG 2
                    "10",// ARG 3
                    UUID.randomUUID().toString()// ARG 4
            );

            if(result != null && result == 1){
                return "success";
            }

            return "Too many attempts";
        }catch (Exception e){
            log.info("Exception occured while login for customer - {} {}",customerPhone,e.getMessage(),e);
            return "Something went wrong, please try again later.";
        }
    }
}
