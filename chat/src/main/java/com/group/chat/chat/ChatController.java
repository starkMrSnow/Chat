package com.group.chat.chat;

import com.group.chat.controller.UserOnlineStatusController;
import com.group.chat.models.ChatMessageEntity;
import com.group.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.security.Principal; // Keep Principal import to ensure it resolves if used for other contexts

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    /**
     * Handles sending of chat messages. Messages are saved to the database and then
     * routed to the specific receiver's private queue.
     * The senderId is now expected to be provided by the client in the ChatMessage or
     * set in the WebSocket session directly by the client or a simple interceptor.
     * @param chatMessage The message payload.
     * @param headerAccessor Accessor for STOMP headers, used to get session attributes.
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(
            @Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor // Use headerAccessor for session attributes
    ){
        if (chatMessage.getTimestamp() == null) {
            chatMessage.setTimestamp(LocalDateTime.now());
        }

        // Retrieve senderId from session attributes.
        // The client must ensure 'userId' is set in the session during /chat.addUser or connect.
        String senderId = (String) headerAccessor.getSessionAttributes().get("userId");
        if (senderId == null) {
            System.err.println("Error: Sender ID not found in session for message. Message not saved/sent: " + chatMessage.getContent());
            return; // Exit if senderId is not available
        }
        chatMessage.setSenderId(senderId); // Ensure senderId in DTO matches session

        ChatMessageEntity messageEntity = ChatMessageEntity.builder()
                .senderId(chatMessage.getSenderId())
                .receiverId(chatMessage.getReceiverId())
                .content(chatMessage.getContent())
                .timestamp(chatMessage.getTimestamp())
                .type(ChatMessageEntity.MessageType.CHAT)
                .build();

        ChatMessageEntity savedMessage = chatService.saveMessage(messageEntity);
        chatMessage.setId(savedMessage.getId().toString());

        // Send to receiver
        messagingTemplate.convertAndSendToUser(
                chatMessage.getReceiverId(), "/queue/messages", chatMessage
        );
        // Also send back to sender's own queue for UI update
        messagingTemplate.convertAndSendToUser(
                chatMessage.getSenderId(), "/queue/messages", chatMessage
        );
    }

    /**
     * Handles adding a user to the chat session. This is typically when a customer connects.
     * The `userId` (customer ID) is stored in the WebSocket session.
     * A JOIN message is sent to the admin's private queue to notify them of a new active user.
     * @param chatMessage The message payload (expected to contain userId in senderId field).
     * @param headerAccessor Accessor for STOMP headers.
     */
    @MessageMapping("/chat.addUser")
    public void addUser(
            @Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor
    ){
        if (chatMessage.getTimestamp() == null) {
            chatMessage.setTimestamp(LocalDateTime.now());
        }

        // Store the user's ID in the WebSocket session attributes.
        // This is crucial for subsequent message routing and identification.
        headerAccessor.getSessionAttributes().put("userId", chatMessage.getSenderId());

        // Log and save JOIN event
        ChatMessageEntity joinMessageEntity = ChatMessageEntity.builder()
                .senderId(chatMessage.getSenderId()) // The user joining
                .receiverId("admin_system") // A special ID for admin system notifications
                .content(chatMessage.getSenderId() + " has joined the chat.")
                .timestamp(chatMessage.getTimestamp())
                .type(ChatMessageEntity.MessageType.JOIN)
                .build();

        chatService.saveMessage(joinMessageEntity);

        // Notify UserOnlineStatusController (for /api/users/online endpoint)
        UserOnlineStatusController.userConnected(chatMessage.getSenderId());

        // Notify the fixed admin ("ADMIN_001") about the new user joining
        ChatMessage notificationMessage = ChatMessage.builder()
                .id(joinMessageEntity.getId().toString())
                .senderId(joinMessageEntity.getSenderId())
                .receiverId("ADMIN_001") // Example fixed admin ID
                .content(chatMessage.getSenderId() + " has started a chat.")
                .type(ChatMessage.MessageType.JOIN)
                .timestamp(joinMessageEntity.getTimestamp())
                .build();

        messagingTemplate.convertAndSendToUser("ADMIN_001", "/queue/notifications", notificationMessage);
        System.out.println("User " + chatMessage.getSenderId() + " joined and ADMIN_001 was notified.");
    }
}