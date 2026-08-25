package com.redisconcepts.pubsub.registration;

import com.redisconcepts.pubsub.subscriber.NotificationSubscriber;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;

@Component
public class NotificationRegistration implements Registration {

    private final String NOTIFICATION_CHANNEL = "NOTIFICATION_CHANNEL";
    private final String SMS_CHANNEL = "SMS_CHANNEL";
    private final String KOT_CHANNEL = "KOT_CHANNEL";

    private final MessageListenerAdapter messageListenerAdapter;

    NotificationRegistration(NotificationSubscriber notificationSubscriber){
        messageListenerAdapter = new MessageListenerAdapter(
                notificationSubscriber,
                "print"
        );
        messageListenerAdapter.afterPropertiesSet();
    }

    @Override
    public MessageListenerAdapter messageListenerAdapter() {
        return messageListenerAdapter;
    }

    @Override
    public ChannelTopic channelTopic() {
        return new ChannelTopic("NOTIFICATION_CHANNEL");
    }
}
