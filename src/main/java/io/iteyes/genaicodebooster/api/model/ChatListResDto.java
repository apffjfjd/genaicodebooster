package io.iteyes.genaicodebooster.api.model;

import io.iteyes.genaicodebooster.domain.chat.entity.Chat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatListResDto {
    private Long id;
    private String message;
    private String senderType; // "USER" 또는 "ASSISTANT"
    private LocalDateTime createdAt;

    public ChatListResDto fromEntity(Chat chat) {
        return ChatListResDto.builder()
                .id(chat.getId())
                .message(chat.getMessage())
                .senderType(chat.getSenderType().name())
                .createdAt(chat.getCreatedAt())
                .build();
    }
}
