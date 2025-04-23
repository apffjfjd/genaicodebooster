package io.iteyes.genaicodebooster.api.service;

import io.iteyes.genaicodebooster.api.controller.ChatRoomController;
import io.iteyes.genaicodebooster.domain.chat.repository.ChatRepository;
import io.iteyes.genaicodebooster.domain.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private static final Logger logger = LoggerFactory.getLogger(ChatRoomController.class);
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRepository chatRepository;

    @Transactional
    public void deleteChatRoom(Long id) {
        if (chatRoomRepository.existsById(id)) {
            chatRepository.deleteAllByChatRoom_Id(id); // 채팅 먼저 삭제
            chatRoomRepository.deleteById(id);         // 채팅방 삭제
            logger.warn("채팅창 삭제 완료: {}", id);
        }

        logger.warn("채팅창 삭제 실패 - ID 존재하지 않음: {}", id);
    }
}
