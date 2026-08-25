package com.redisconcepts.pubsub.registration;


import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Subscriber {


    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            List<Registration> registrations,
            RedisConnectionFactory redisConnectionFactory
    ){
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
        registrations.forEach(listener ->
                {
                    System.err.println("Class" + listener.getClass());
                    System.err.println("Adaptor" + listener.messageListenerAdapter());
                    redisMessageListenerContainer.addMessageListener(
                            listener.messageListenerAdapter(),
                            listener.channelTopic()
                    );
                }
        );

        return redisMessageListenerContainer;
    }
}
