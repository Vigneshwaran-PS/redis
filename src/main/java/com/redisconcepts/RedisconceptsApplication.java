package com.redisconcepts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class RedisconceptsApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedisconceptsApplication.class, args);
	}

}
