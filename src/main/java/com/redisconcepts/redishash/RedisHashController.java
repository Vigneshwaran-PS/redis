package com.redisconcepts.redishash;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

@RestController
@RequestMapping("/redis-hash")
@Slf4j
public class RedisHashController {


    public final RedisTemplate<String,String> redisTemplate;
    private final int MAX_RETRIES = 2;
    private final String PHONE = "PHONE";
    private final String OTP = "OTP";
    private final String RETRIED_ATTEMPTS = "RETRIED_ATTEMPTS";

    public RedisHashController(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/otp")
    public String getOtp(@RequestParam String phone){
        try {

            Map<Object,Object> result = redisTemplate.opsForHash().getOperations().boundHashOps(phone).entries();
            log.info("Redis Data - {}", result);
            String otp = generateOtp();
            if(result == null || result.isEmpty()){
                redisTemplate.opsForHash().putAll(
                        phone,
                        Map.of(
                             PHONE,phone,
                             RETRIED_ATTEMPTS,"1",
                             OTP,otp
                        )
                );
                redisTemplate.opsForHash().expire(phone, Duration.ofSeconds(60), List.of(OTP));
                redisTemplate.expire(phone,Duration.ofMinutes(5));
                return otp;
            }

            String retriedAttempts = (String)result.getOrDefault(RETRIED_ATTEMPTS,"0");

            if(StringUtils.isNotEmpty(retriedAttempts) && Integer.parseInt(retriedAttempts) >= MAX_RETRIES){
                redisTemplate.opsForHash().delete(phone,OTP);
                return "Please try again later.";
            }

            int attempts = Integer.parseInt(retriedAttempts);
            redisTemplate.opsForHash().putAll(
                    phone,
                    Map.of(
                            PHONE,phone,
                            OTP, otp,
                            RETRIED_ATTEMPTS, String.valueOf(attempts+1)
                    )
            );
            redisTemplate.opsForHash().expire(phone, Duration.ofSeconds(60), List.of(OTP));
            return otp;

        }catch (Exception e){
            log.info("Exception occured while getting otp {}", e.getMessage(),e);
        }
        return "Something went wrong please try again later.";
    }

    public String generateOtp(){

        String otp = "";
        for(int i=1; i<=4; i++){
            otp = otp.concat("" + (int)(Math.floor(Math.random() * 10) + 1));
        }

        return otp;
    }


    @GetMapping("/validate")
    public String validateOtp(@RequestParam String phone, @RequestParam String otp){
        try {
            if(StringUtils.isEmpty(phone) || StringUtils.isEmpty(otp)){
                return "Invalid phone or mobile number";
            }

            Map<Object,Object> result = redisTemplate.opsForHash().getOperations().boundHashOps(phone).entries();
            log.info("Redis Data - {}", result);
            if(result == null || result.isEmpty()){
                return "Invalid OTP";
            }

            String cacheOtp = (String)result.getOrDefault(OTP,"");
            if(StringUtils.isEmpty(cacheOtp)){
                return "OTP Expired";
            }

            if(cacheOtp.equalsIgnoreCase(otp)){
                redisTemplate.delete(phone);
                return "success";
            }


        }catch (Exception e){
            log.info("Exception occured while getting otp {}", e.getMessage(),e);
        }
        return "test";
    }
}
