package com.redisconcepts.pubsub.subscriber;

import com.redisconcepts.springcache.Order;
import com.redisconcepts.utils.JsonUtils;
import org.springframework.stereotype.Component;

@Component
public class NotificationSubscriber {

    public void print(String message){
        System.out.println("Notification Server " + (Order) JsonUtils.fromJaon(message,Order.class));
    }
}
