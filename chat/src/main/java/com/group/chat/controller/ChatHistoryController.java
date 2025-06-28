package com.group.chat.controller;

import com.group.chat.models.ChatMessageEntity;
import com.group.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
// Removed org.springframework.security.access.prepost.PreAuthorize

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final ChatService chatService;

    /**
     * Retrieves the chat history between a customer and an admin.
     * This endpoint is now accessible without authentication.
     * @param customerId The ID of the customer.
     * @param adminId The ID of the admin.
     * @return A list of ChatMessageEntity objects representing the conversation history.
     */
    @GetMapping("/history/{customerId}/{adminId}")
    // Removed @PreAuthorize annotation
    public ResponseEntity<List<ChatMessageEntity>> getChatHistory(
            @PathVariable String customerId,
            @PathVariable String adminId
    ) {
        List<ChatMessageEntity> messages = chatService.getConversationHistory(customerId, adminId);
        return ResponseEntity.ok(messages);
    }

    /**
     * Retrieves recent chat activities for a specific user (e.g., for an admin dashboard
     * to list active customer chats).
     * This endpoint is now accessible without authentication.
     * @param userId The ID of the user (admin or customer) to fetch recent activity for.
     * @return A list of recent ChatMessageEntity objects.
     */
    @GetMapping("/recent/{userId}")
    // Removed @PreAuthorize annotation
    public ResponseEntity<List<ChatMessageEntity>> getRecentChatsForUser(@PathVariable String userId) {
        List<ChatMessageEntity> messages = chatService.getRecentMessagesForUser(userId);
        return ResponseEntity.ok(messages);
    }
}