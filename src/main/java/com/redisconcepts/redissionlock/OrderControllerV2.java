package com.redisconcepts.redissionlock;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RPermitExpirableSemaphore;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/redis-rlock")
@Slf4j
public class OrderControllerV2 {

    private final RedissonClient redissonClient;

    public OrderControllerV2(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    // Rlock - only one server/pod can update the order status
    // Rlock won't maintain the thread/request order that is being processing/waiting for acquire lock,
    // it will be handled by fairlock mechanism
    // Example:
    // Request A - order123
    // Request B - order123
    // Request C - order123
    // Assume A acquired the lock, so next Request B or C can acquire the lock,
    // if we want to maintain the order of the request/thread, then we can use fair-lock
    @PostMapping("/update")
    public String updateOrder(@RequestParam String orderId, @RequestParam String status){
        RLock rLock = redissonClient.getLock("lock:order:"+orderId);
        boolean accqired = false;
        try {
            accqired = rLock.tryLock(10,30, TimeUnit.SECONDS); // wait + lease but no watchdog mechanism
            accqired = rLock.tryLock(10, TimeUnit.SECONDS); // wait but watchdog mechanism,
                                                            // so redis will update the lease time often until
                                                            // the method completes it work
            if (accqired){
                log.info("Lock accquired for order Id - {}",orderId);
                // update order status
                return "success";
            }else{
                log.info("Failed to accquired lock for order Id - {}",orderId);
                // simply return
                return "Success";
            }
        }catch (Exception e){
            log.info("Exception occured while updating order status - {} ",e.getMessage(),e);
        }finally {
            if(rLock.isHeldByCurrentThread() && accqired){
                rLock.unlock();
            }
        }
        return "Something went wrong. please try again later";
    }


    // FAir lock - maintain the order/track of each request
    @PostMapping("/update/v2")
    public String updateOrderV2(@RequestParam String orderId, @RequestParam String status){
        RLock rLock = redissonClient.getFairLock("lock:order:"+orderId);
        boolean accqired = false;
        try {
            accqired = rLock.tryLock(10,30, TimeUnit.SECONDS);
            if (accqired){
                log.info("Lock accquired for order Id - {}",orderId);
                // update order status
                return "success";
            }else{
                log.info("Failed to accquired lock for order Id - {}",orderId);
                // simply return
                return "Success";
            }
        }catch (Exception e){
            log.info("Exception occured while updating order status - {} ",e.getMessage(),e);
        }finally {
            if(rLock.isHeldByCurrentThread() && accqired){
                rLock.unlock();
            }
        }
        return "Something went wrong. please try again later";
    }

    // RSemaphore - How many things are allowed to execute this operation simultaneously?
    // one drop bach, once after acquiring the lock, if the server crashes, then the no of available permits became lesser
    // Example: permit - 5
    // one server crashed after acquiring lock, so the lock never been released,
    // so the avaiable permits became lesser forever. to resolve this we require RPermitExpirableSemaphore
    @PostMapping("/update/v3")
    public String updateOrderV3(@RequestParam String orderId, @RequestParam String status){
        RSemaphore rLock = redissonClient.getSemaphore("lock:order:"+orderId);
        boolean accqired = false;
        try {
            rLock.addPermits(5);
            rLock.acquire();
            if (accqired){
                log.info("Lock accquired for order Id - {}",orderId);
                // update order status
                return "success";
            }else{
                log.info("Failed to accquired lock for order Id - {}",orderId);
                // simply return
                return "Success";
            }
        }catch (Exception e){
            log.info("Exception occured while updating order status - {} ",e.getMessage(),e);
        }finally {
            rLock.release();
        }
        return "Something went wrong. please try again later";
    }


    @PostMapping("/update/v4")
    public String updateOrderV4(@RequestParam String orderId, @RequestParam String status){
        RPermitExpirableSemaphore rLock = redissonClient.getPermitExpirableSemaphore("lock:order:"+orderId);
        boolean accqired = false;
        rLock.addPermits(5);
        String permitId = null;
        try {
            permitId = rLock.acquire();
            if (accqired){
                log.info("Lock accquired for order Id - {}",orderId);
                // update order status
                return "success";
            }else{
                log.info("Failed to accquired lock for order Id - {}",orderId);
                // simply return
                return "Success";
            }
        }catch (Exception e){
            log.info("Exception occured while updating order status - {} ",e.getMessage(),e);
        }finally {
            if(permitId != null)
                rLock.release(permitId);
        }
        return "Something went wrong. please try again later";
    }
}
