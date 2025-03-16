package com.chat.videocall.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class SignalingHandler extends TextWebSocketHandler {

    // Map room name to list of sessions
    private final ConcurrentHashMap<String, List<WebSocketSession>> rooms = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("WebSocket connected: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, Object> payload = mapper.readValue(message.getPayload(), Map.class);
        String type = (String) payload.get("type");
        String room = (String) payload.get("room");

        if ("joinRoom".equals(type)) {
            rooms.putIfAbsent(room, new CopyOnWriteArrayList<>());
            List<WebSocketSession> sessions = rooms.get(room);

            if (sessions.size() == 0) {
                sessions.add(session);
                session.sendMessage(new TextMessage(mapper.writeValueAsString(
                        Map.of("type", "created", "room", room)
                )));
            } else if (sessions.size() < 10) { // Maksimalno 10 korisnika u sobi
                sessions.add(session);
                session.sendMessage(new TextMessage(mapper.writeValueAsString(
                        Map.of("type", "joined", "room", room)
                )));
                // Obavesti postojeće korisnike o novom učesniku
                broadcastToRoom(room, new TextMessage(mapper.writeValueAsString(
                        Map.of("type", "newUser", "userId", session.getId())
                )), session);
            } else {
                session.sendMessage(new TextMessage(mapper.writeValueAsString(
                        Map.of("type", "full", "room", room)
                )));
            }
        } else if ("offer".equals(type) ||
                "answer".equals(type) ||
                "candidate".equals(type) ||
                "ready".equals(type)) {
            broadcastToRoom(room, message, session);
        } else if ("chat".equals(type)) {
            // Rukovanje chat porukama: broadcast poruka svim učesnicima u sobi
            broadcastToRoom(room, message, null);
        } else if ("leaveRoom".equals(type)) {
            leaveRoom(session, room);
        }
    }

    private void broadcastToRoom(String room, TextMessage message, WebSocketSession sender) throws Exception {
        List<WebSocketSession> sessions = rooms.get(room);
        if (sessions != null) {
            for (WebSocketSession s : sessions) {
                // Ako je sender null šaljemo poruku svima, u suprotnom izostavljamo sender-a
                if (sender == null || !s.getId().equals(sender.getId())) {
                    if (s.isOpen()) {
                        s.sendMessage(message);
                    }
                }
            }
        }
    }

    private void leaveRoom(WebSocketSession session, String room) {
        List<WebSocketSession> sessions = rooms.get(room);
        if (sessions != null) {
            sessions.remove(session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Ukloni sesiju iz svih soba
        rooms.forEach((room, sessions) -> sessions.remove(session));
        System.out.println("WebSocket disconnected: " + session.getId());
    }
}
