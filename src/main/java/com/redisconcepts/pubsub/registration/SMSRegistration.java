package com.redisconcepts.pubsub.registration;

import com.redisconcepts.pubsub.subscriber.KOTSubscriber;
import com.redisconcepts.pubsub.subscriber.SMSSubscriber;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;

@Component
public class SMSRegistration implements Registration {

    private final String NOTIFICATION_CHANNEL = "NOTIFICATION_CHANNEL";
    private final String SMS_CHANNEL = "SMS_CHANNEL";
    private final String KOT_CHANNEL = "KOT_CHANNEL";

    private final MessageListenerAdapter messageListenerAdapter;

    SMSRegistration(SMSSubscriber smsSubscriber){
        messageListenerAdapter = new MessageListenerAdapter(
                smsSubscriber,
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
