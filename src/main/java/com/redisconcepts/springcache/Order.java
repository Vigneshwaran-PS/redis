package com.redisconcepts.springcache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Order {

    private String orderId;
    private String name;
    private String price;
    private String status;

}
