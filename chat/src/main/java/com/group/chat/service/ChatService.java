package com.group.chat.service;

import com.group.chat.models.ChatMessageEntity;
import com.group.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public ChatMessageEntity saveMessage(ChatMessageEntity message) {
        return chatMessageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageEntity> getConversationHistory(String user1Id, String user2Id) {
        return chatMessageRepository.findConversationMessages(user1Id, user2Id);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageEntity> getRecentMessagesForUser(String userId) {
        return chatMessageRepository.findTop50BySenderIdOrReceiverIdOrderByTimestampDesc(userId, userId);
    }
}