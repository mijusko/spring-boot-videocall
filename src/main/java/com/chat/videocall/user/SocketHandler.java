package com.chat.videocall.user;


import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.*;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SocketHandler {

    private final SocketIOServer server;
    private static final Map<String, String> users = new HashMap<>();
    private static final Map<String, String> rooms = new HashMap<>();

    public SocketHandler(SocketIOServer server) {
        this.server = server;
        server.addListeners(this);
        //server.start();
    }

    @OnConnect
    public void onConnect(SocketIOClient client) {
        String clientId = client.getSessionId().toString();
        users.put(clientId, null);
        log.info("Client connected: {}", clientId);
    }

    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        String clientId = client.getSessionId().toString();
        String room = users.get(clientId);
        if (room != null) {
            log.info("Client disconnected: {} from room: {}", clientId, room);
            users.remove(clientId);
            client.getNamespace().getRoomOperations(room).sendEvent("userDisconnected", clientId);
        }
    }

    @OnEvent("joinRoom")
    public void onJoinRoom(SocketIOClient client, String room) {
        int connectedClients = server.getRoomOperations(room).getClients().size();
        if (connectedClients == 0) {
            client.joinRoom(room);
            client.sendEvent("created", room);
            users.put(client.getSessionId().toString(), room);
            rooms.put(room, client.getSessionId().toString());
            log.info("Room {} created by {}", room, client.getSessionId());
        } else if (connectedClients == 1) {
            client.joinRoom(room);
            client.sendEvent("joined", room);
            users.put(client.getSessionId().toString(), room);
            // Oznaka da je prvi korisnik pozivaoc
            client.sendEvent("setCaller", rooms.get(room));
            log.info("Client {} joined room {}", client.getSessionId(), room);
        } else {
            client.sendEvent("full", room);
            log.info("Room {} is full", room);
        }
    }

    @OnEvent("ready")
    public void onReady(SocketIOClient client, String room, AckRequest ackRequest) {
        // Emitujemo "ready" događaj svim klijentima u sobi
        client.getNamespace().getRoomOperations(room).sendEvent("ready", room);
        log.info("Ready event in room: {}", room);
    }

    @OnEvent("candidate")
    public void onCandidate(SocketIOClient client, Map<String, Object> payload) {
        String room = (String) payload.get("room");
        client.getNamespace().getRoomOperations(room).sendEvent("candidate", payload);
        log.info("Candidate event in room: {}", room);
    }

    @OnEvent("offer")
    public void onOffer(SocketIOClient client, Map<String, Object> payload) {
        String room = (String) payload.get("room");
        Object sdp = payload.get("sdp");
        client.getNamespace().getRoomOperations(room).sendEvent("offer", sdp);
        log.info("Offer event in room: {}", room);
    }

    @OnEvent("answer")
    public void onAnswer(SocketIOClient client, Map<String, Object> payload) {
        String room = (String) payload.get("room");
        Object sdp = payload.get("sdp");
        client.getNamespace().getRoomOperations(room).sendEvent("answer", sdp);
        log.info("Answer event in room: {}", room);
    }

    @OnEvent("leaveRoom")
    public void onLeaveRoom(SocketIOClient client, String room) {
        client.leaveRoom(room);
        log.info("Client {} left room {}", client.getSessionId(), room);
    }

    @OnEvent("chatMessage")
    public void onChatMessage(SocketIOClient client, Map<String, Object> payload) {
        String room = (String) payload.get("room");
        String message = (String) payload.get("message");
        String sender = (String) payload.get("sender");

        Map<String, Object> chatData = new HashMap<>();
        chatData.put("sender", sender);
        chatData.put("message", message);

        client.getNamespace().getRoomOperations(room).sendEvent("chatMessage", chatData);
        log.info("Chat message in room {}: {}: {}", room, sender, message);
    }
}
