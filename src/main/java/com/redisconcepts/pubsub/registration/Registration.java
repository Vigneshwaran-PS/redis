package com.redisconcepts.pubsub.registration;


import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

public interface Registration {

    MessageListenerAdapter messageListenerAdapter();

    ChannelTopic channelTopic();
}
