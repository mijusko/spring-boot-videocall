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
        // Koristimo username kao jedinstveni identifikator
        String username = (String) payload.get("username");

        if ("joinRoom".equals(type)) {
            rooms.putIfAbsent(room, new CopyOnWriteArrayList<>());
            List<WebSocketSession> sessions = rooms.get(room);

            if (sessions.size() == 0) {
                sessions.add(session);
                // Prvom korisniku šaljemo poruku "created"
                session.sendMessage(new TextMessage(mapper.writeValueAsString(
                        Map.of("type", "created", "room", room, "userId", username)
                )));
            } else if (sessions.size() < 10) { // Maksimalno 10 korisnika u sobi
                sessions.add(session);
                // Novopridošem korisniku šaljemo poruku "joined"
                session.sendMessage(new TextMessage(mapper.writeValueAsString(
                        Map.of("type", "joined", "room", room, "userId", username)
                )));
                // Obavesti sve korisnike da je novi korisnik pristupio
                broadcastToRoom(room, new TextMessage(mapper.writeValueAsString(
                        Map.of("type", "newUser", "userId", username, "username", username)
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
            // Prosledi signaling poruke svima osim sendera
            broadcastToRoom(room, message, session);
        } else if ("chat".equals(type)) {
            broadcastToRoom(room, message, null);
        } else if ("leaveRoom".equals(type)) {
            leaveRoom(session, room);
        }
    }

    private void broadcastToRoom(String room, TextMessage message, WebSocketSession sender) throws Exception {
        List<WebSocketSession> sessions = rooms.get(room);
        if (sessions != null) {
            for (WebSocketSession s : sessions) {
                // Ako je sender null šaljemo poruku svima, inače izostavljamo sendera
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
