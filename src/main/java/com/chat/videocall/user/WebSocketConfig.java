package com.chat.videocall.user;
import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebSocketConfig {

    @Value("${server.port}")
    private int serverPort;

   /* @Value("${socket.port}")
    private int port;*/

    @Bean
    public SocketIOServer socketIOServer() throws Exception {
        com.corundumstudio.socketio.Configuration config =
                new com.corundumstudio.socketio.Configuration();
        config.setHostname("0.0.0.0");
        config.setPort(serverPort); // Isti port kao Spring Boot
        config.setTransports(Transport.WEBSOCKET); // Forsiraj WebSocket
        config.setOrigin("*"); // Dozvoli sve CORS zahteve
        return new SocketIOServer(config);
    }
}
