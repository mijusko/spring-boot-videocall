package com.chat.videocall.user;

import lombok.Data;

@Data
public class WebSocketMessage {
    private String type;
    private String room;
    private String peerId;
    private String target;
    private Object offer;
    private Object answer;
    private Object candidate;
    private String username;
    private String message;
}