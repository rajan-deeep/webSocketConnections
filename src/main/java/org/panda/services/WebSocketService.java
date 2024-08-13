package org.panda.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class WebSocketService {

    private static final Map<String, List<WebSocketSession>> idToSessions = new ConcurrentHashMap<>();
    private final RedisService redisService;

    @Autowired
    public WebSocketService(RedisService redisService) {
        this.redisService = redisService;
    }

    public void subscribe(WebSocketSession session, String id) {
        idToSessions.computeIfAbsent(id, k -> new CopyOnWriteArrayList<>()).add(session);
        redisService.subscribeRedisChannel(id, (message, pattern) -> {
            String channel = new String(message.getChannel());
            String payload = new String(message.getBody());
            forwardMessageToSessions(channel, payload);
        });
    }

    public void unsubscribe(WebSocketSession session, String id) {
        List<WebSocketSession> sessions = idToSessions.get(id);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                idToSessions.remove(id);
                redisService.unsubscribeRedisChannel(id);
            }
        }
    }

    public void publishMessage(WebSocketSession session, String id, String msg) {
        List<WebSocketSession> sessions = idToSessions.get(id);
        if (sessions != null && sessions.contains(session)) {
            redisService.publishMessageToRedis(id, msg);
        } else {
            try {
                session.sendMessage(new TextMessage("Error: Not subscribed to channel " + id));
            } catch (Exception e) {
                log.error("Error sending message to client", e);
            }
        }
    }

    //this will run if any message received by websocket server from redis on particular channel
    private void forwardMessageToSessions(String id, String message) {
        List<WebSocketSession> sessions = idToSessions.get(id);
        if (sessions != null) {
            for (WebSocketSession session : sessions) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (Exception e) {
                    log.error("Error forwarding message to WebSocket session", e);
                }
            }
        }
    }
}
