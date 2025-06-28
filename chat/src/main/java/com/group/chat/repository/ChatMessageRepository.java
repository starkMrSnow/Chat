package com.group.chat.repository;

import com.group.chat.models.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    // Find messages between two specific users, ordered by timestamp
    List<ChatMessageEntity> findBySenderIdAndReceiverIdOrderByTimestampAsc(String senderId, String receiverId);

    List<ChatMessageEntity> findByReceiverIdAndSenderIdOrderByTimestampAsc(String receiverId, String senderId);

    // Custom query to find all messages involved in a conversation between two parties
    default List<ChatMessageEntity> findConversationMessages(String user1Id, String user2Id) {
        // This combines messages where user1 sent to user2 OR user2 sent to user1
        List<ChatMessageEntity> messages1 = findBySenderIdAndReceiverIdOrderByTimestampAsc(user1Id, user2Id);
        List<ChatMessageEntity> messages2 = findByReceiverIdAndSenderIdOrderByTimestampAsc(user1Id, user2Id);

        messages1.addAll(messages2);
        // Sort the combined list by timestamp
        messages1.sort((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));
        return messages1;
    }

    // You might also want to find recent messages for a user
    List<ChatMessageEntity> findTop50BySenderIdOrReceiverIdOrderByTimestampDesc(String userId1, String userId2);
}
