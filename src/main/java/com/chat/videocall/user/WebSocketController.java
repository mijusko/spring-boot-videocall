package com.chat.videocall.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import com.chat.videocall.user.WebSocketMessage;


@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/room/{roomId}")
    public void handleRoomMessage(
            @DestinationVariable String roomId,
            WebSocketMessage message
    ) {
        // Broadcast poruka svim korisnicima u sobi
        messagingTemplate.convertAndSend("/topic/room/" + roomId, message);
    }

    @MessageMapping("/room/{roomId}/{targetPeerId}")
    public void handleDirectMessage(
            @DestinationVariable String roomId,
            @DestinationVariable String targetPeerId,
            WebSocketMessage message) {

    }
}