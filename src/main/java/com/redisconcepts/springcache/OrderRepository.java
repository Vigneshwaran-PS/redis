package com.redisconcepts.springcache;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Component
public class OrderRepository {


    public List<Order> orders = new ArrayList<>();

    public OrderRepository(){

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


}
