package org.panda.websocketlisteners;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.panda.services.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

@Service
@Slf4j
public class MyWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebSocketService webSocketService;

    @Autowired
    public MyWebSocketHandler(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        Map<String, Object> map = objectMapper.readValue(payload, Map.class);
        String action = (String) map.get("action");
        String id = (String) map.get("id");

        switch (action) {
            case "subscribe":
                webSocketService.subscribe(session, id);
                break;

            case "unsubscribe":
                webSocketService.unsubscribe(session, id);
                break;

            case "message":
                String msg = (String) map.get("msg");
                webSocketService.publishMessage(session, id, msg);
                break;

            default:
                session.sendMessage(new TextMessage("Unknown action: " + action));
                break;
        }
    }
}
