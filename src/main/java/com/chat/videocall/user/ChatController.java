package com.chat.videocall.user;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @MessageMapping("/chat")          // Klijenti šalju poruke na /app/chat
    @SendTo("/topic/messages")         // Server prosljeđuje poruke na /topic/messages
    public ChatMessage sendMessage(ChatMessage message) throws Exception {
        // Opcionalno: simulirajte malo kašnjenje, npr. Thread.sleep(500);
        return message;
    }
}