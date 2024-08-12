package org.panda.websocketlisteners;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Map of ID to a list of WebSocket sessions
    public static Map<String, List<WebSocketSession>> idToSocketMap = new HashMap<>();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        Map<String, Object> map = objectMapper.readValue(payload, Map.class);
        String action = (String) map.get("action");
        String id = (String) map.get("id");

        switch (action) {
            case "subscribe":
                // Add the session to the list of sessions for the given ID
                idToSocketMap.computeIfAbsent(id, k -> new ArrayList<>()).add(session);
                session.sendMessage(new TextMessage("Subscribed to ID: " + id));
                break;

            case "unsubscribe":
                // Remove the session from the list of sessions for the given ID
                List<WebSocketSession> sessions = idToSocketMap.get(id);
                if (sessions != null) {
                    sessions.remove(session);
                    if (sessions.isEmpty()) {
                        idToSocketMap.remove(id); // Clean up empty lists
                    }
                }
                session.sendMessage(new TextMessage("Unsubscribed from ID: " + id));
                session.close(CloseStatus.NORMAL);
                break;

            case "message":
                // Send a message to all sessions subscribed to the given ID
                List<WebSocketSession> subscribedSessions = idToSocketMap.get(id);
                if (subscribedSessions != null) {
                    for (WebSocketSession webSocketSession : subscribedSessions) {
                        webSocketSession.sendMessage(new TextMessage((String) map.get("msg")));
                    }
                }
                break;

            default:
                session.sendMessage(new TextMessage("Unknown action: " + action));
                break;
        }
    }
}
