package org.panda.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Service
@Slf4j
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private static final Set<String> subscribedChannels = new ConcurrentSkipListSet<>();
    private final Map<String, MessageListener> channelListeners = new ConcurrentHashMap<>();

    @Autowired
    public RedisService(RedisTemplate<String, String> redisTemplate,
                        RedisMessageListenerContainer redisMessageListenerContainer) {
        this.redisTemplate = redisTemplate;
        this.redisMessageListenerContainer = redisMessageListenerContainer;
    }

    public void subscribeRedisChannel(String id, MessageListener messageListener) {
        if (!subscribedChannels.contains(id)) {
            redisMessageListenerContainer.addMessageListener(messageListener, new ChannelTopic(id));
            channelListeners.put(id, messageListener);
            subscribedChannels.add(id);
        }
    }

    public void unsubscribeRedisChannel(String id) {
        if (subscribedChannels.contains(id)) {
            MessageListener messageListener = channelListeners.remove(id);
            if (messageListener != null) {
                redisMessageListenerContainer.removeMessageListener(messageListener);
            }
            subscribedChannels.remove(id);
        }
    }

    public void publishMessageToRedis(String id, String msg) {
        redisTemplate.convertAndSend(id, msg);
    }
}
