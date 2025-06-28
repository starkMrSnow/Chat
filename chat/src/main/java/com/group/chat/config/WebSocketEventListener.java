package com.group.chat.config;

import com.group.chat.chat.ChatMessage;
import com.group.chat.controller.UserOnlineStatusController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;
import java.security.Principal; // Keep import to ensure it resolves if used in other contexts

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messageTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        // In a non-authenticated setup, userId must be put into session by the client's /chat.addUser call
        // or a custom interceptor. For simplicity, we assume client will provide it on first interaction.
        String userId = (String) headerAccessor.getSessionAttributes().get("userId"); // Retrieve userId

        if (userId != null) {
            log.info("User connected: {}", userId);
            UserOnlineStatusController.userConnected(userId); // Mark user as online

            // Notify admins about the new user connecting
            ChatMessage notificationMessage = ChatMessage.builder()
                    .senderId(userId)
                    .receiverId("ADMIN_001") // Send to specific admin
                    .content(userId + " has connected.")
                    .type(ChatMessage.MessageType.JOIN)
                    .timestamp(LocalDateTime.now())
                    .build();
            messageTemplate.convertAndSendToUser("ADMIN_001", "/queue/notifications", notificationMessage);
            System.out.println("User " + userId + " connected and ADMIN_001 was notified.");
        } else {
            // This might happen if a client connects but hasn't sent a /chat.addUser message yet
            log.warn("Anonymous WebSocket session connected. Awaiting userId from /chat.addUser: {}", headerAccessor.getSessionId());
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event){
       StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
       String userId = (String) headerAccessor.getSessionAttributes().get("userId"); // Retrieve userId

       if (userId != null) {
            log.info("User disconnected: {}", userId);
            UserOnlineStatusController.userDisconnected(userId); // Mark user as offline

            var chatMessage = ChatMessage.builder()
                    .type(ChatMessage.MessageType.LEAVE)
                    .senderId(userId)
                    .timestamp(LocalDateTime.now())
                    .content(userId + " has left the chat.")
                    .build();

            // Notify admin about the user leaving
            messageTemplate.convertAndSendToUser("ADMIN_001", "/queue/notifications", chatMessage);
            System.out.println("User " + userId + " disconnected and ADMIN_001 was notified.");
       } else {
           log.info("Anonymous WebSocket session disconnected: {}", headerAccessor.getSessionId());
       }
    }
}