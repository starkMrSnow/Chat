package com.group.chat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
// Removed org.springframework.security.access.prepost.PreAuthorize

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This controller provides an API to check online users.
 * It uses a ConcurrentHashMap to track user sessions.
 * NOTE: For a highly scalable solution, consider using a distributed session store or
 * a dedicated presence service (e.g., Redis Pub/Sub).
 */
@RestController
@RequestMapping("/api/users")
public class UserOnlineStatusController {

    // Tracks currently online users by their userId
    private static final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();

    /**
     * Endpoint for admin to get a list of currently online users.
     * Now accessible without authentication.
     */
    @GetMapping("/online")
    // Removed @PreAuthorize annotation
    public ResponseEntity<Set<String>> getOnlineUsers() {
        return ResponseEntity.ok(onlineUsers);
    }

    // Helper methods to update online status (called from WebSocketEventListener)
    public static void userConnected(String userId) {
        onlineUsers.add(userId);
    }

    public static void userDisconnected(String userId) {
        onlineUsers.remove(userId);
    }
}
