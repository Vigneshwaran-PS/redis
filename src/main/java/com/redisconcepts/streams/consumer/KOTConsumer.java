package com.redisconcepts.streams.consumer;

import io.lettuce.core.StreamMessage;
import io.lettuce.core.XAutoClaimArgs;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.models.stream.ClaimedMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
public class KOTConsumer {

    private final String KOT_STREAM = "kot-stream";
    private final RedisTemplate<String,String> redisTemplate;
    private final String GROUP = "KOT_GROUP_1";
    private final RedisCommands<String,String> redisCommand;

    KOTConsumer(RedisTemplate<String, String> redisTemplate, RedisCommands<String,String> redisCommand){
        this.redisTemplate = redisTemplate;
        this.redisCommand = redisCommand;
        ensureGroupExists();
        startConsumer("kot-consumer-1");
        startConsumer("kot-consumer-2");
    }

    // Creates the stream (MKSTREAM) and consumer group if they don't exist yet.
    // Safe to call every startup - if the group already exists Redis returns
    // BUSYGROUP, which we just log and ignore.
    private void ensureGroupExists() {
        try {
            redisTemplate.opsForStream().createGroup(KOT_STREAM, ReadOffset.from("0"), GROUP);
            log.info("Created consumer group {} on stream {}", GROUP, KOT_STREAM);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("BUSYGROUP")) {
                log.info("Consumer group {} already exists on stream {}", GROUP, KOT_STREAM);
            } else {
                log.error("Failed to create consumer group {} on stream {}", GROUP, KOT_STREAM, e);
            }
        }
    }

    private void startConsumer(String consumer) {
        new Thread(() -> {
            ackOwnPendingEvent(consumer);
            while (true){
                ackNewEvents(consumer);
                recoverDeadConsumerMessages(consumer);
            }

        }).start();
    }

    private void recoverDeadConsumerMessages(String consumer) {

        String startId = "0-0";

        try {

            while (true) {

                ClaimedMessages<String, String> claimedMessages =
                        redisCommand.xautoclaim(
                                KOT_STREAM,
                                XAutoClaimArgs.Builder.xautoclaim(
                                        io.lettuce.core.Consumer.from(GROUP, consumer),
                                        Duration.ofSeconds(10),
                                        startId
                                ).count(100)
                        );

                // Messages claimed from dead consumers
                List<StreamMessage<String, String>> messages = claimedMessages.getMessages();

                for (StreamMessage<String, String> message : messages) {

                    log.info("Recovered Message : {}", message.getId());

                    Long ack = redisTemplate.opsForStream().acknowledge(
                            KOT_STREAM,
                            GROUP,
                            RecordId.of(message.getId())
                    );

                    log.info("ACK Result : {}", ack);
                }

                // Finished scanning the Pending Entries List
                if ("0-0".equals(startId)) {
                    break;
                }
            }

        } catch (Exception e) {
            log.error("Error recovering dead consumer messages", e);
        }
    }

    private void ackNewEvents(String consumer) {
        try {
            List<MapRecord<String, Object, Object>> pending =
                    redisTemplate.opsForStream().read(
                            Consumer.from(GROUP, consumer),
                            StreamReadOptions.empty().count(10),
                            StreamOffset.create(KOT_STREAM, ReadOffset.lastConsumed())
                    );

            if(pending == null || pending.isEmpty()){
                return;
            }

            for(MapRecord<String, Object, Object> p: pending){
                redisTemplate.opsForStream().acknowledge(
                        KOT_STREAM,
                        GROUP,
                        p.getId()
                );
            }
        } catch (Exception e) {
            log.error("Error acking new events for consumer {}", consumer, e);
        }
    }

    private void ackOwnPendingEvent(String consumer) {
        try {
            while (true){

                List<MapRecord<String, Object, Object>> pending =
                        redisTemplate.opsForStream().read(
                                Consumer.from(GROUP, consumer),
                                StreamReadOptions.empty().count(10),
                                StreamOffset.create(KOT_STREAM, ReadOffset.from("0"))
                        );

                if(pending == null || pending.isEmpty()){
                    break;
                }

                for(MapRecord<String, Object, Object> p: pending){
                    redisTemplate.opsForStream().acknowledge(
                            KOT_STREAM,
                            GROUP,
                            p.getId()
                    );
                }
            }
        } catch (Exception e) {
            log.error("Error acking own pending events for consumer {}", consumer, e);
        }
    }
}
