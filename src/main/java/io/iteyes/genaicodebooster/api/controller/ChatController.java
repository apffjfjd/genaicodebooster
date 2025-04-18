package io.iteyes.genaicodebooster.api.controller;

import io.iteyes.genaicodebooster.api.model.ChatCreateReqDto;
import io.iteyes.genaicodebooster.api.model.ChatCreateResDto;
import io.iteyes.genaicodebooster.api.model.ChatListReqDto;
import io.iteyes.genaicodebooster.api.model.ChatListResDto;
import io.iteyes.genaicodebooster.api.service.AiService;
import io.iteyes.genaicodebooster.api.service.ChatService;
import io.iteyes.genaicodebooster.domain.chat.entity.Chat;
import io.iteyes.genaicodebooster.domain.chat.entity.ChatRoom;
import io.iteyes.genaicodebooster.domain.chat.repository.ChatRepository;
import io.iteyes.genaicodebooster.domain.chat.repository.ChatRoomRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final AiService aiService;
    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Value("${chat.list.default-limit}")
    private int limit;

    @Operation(summary = "채팅 생성(송/수신)")
    @Transactional
    @PostMapping
    public ResponseEntity<ChatCreateResDto> createChat(@Valid @RequestBody ChatCreateReqDto request) {

        logger.debug("채팅 생성 요청 수신: {}", request);

        try {
            // 1. 사용자 및 채팅방 유효성 검증 (생략 가능)
            // Optional<User> user = userRepository.findById(request.getUserId());
            // Optional<ChatRoom> room = chatRoomRepository.findById(request.getRoomId());

            // 2. 채팅룸 검증
            ChatRoom room = chatRoomRepository.findById(request.getChatRoomId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

            // 3. AI 서버로 메시지 전달
//            String aiResponse = aiService.sendMessage(request.getMsgQuestion(), request.getMsgList());
            String aiResponse = aiService.sendMessage(request.getMsgQuestion());
            logger.info("AI 응답 수신: {}", aiResponse);
            if (aiResponse == null || aiResponse.isBlank()) {
                logger.warn("AI 응답 없음 → 저장 생략");
                return ResponseEntity.status(204).build(); // No Content or 적절한 처리
            }

            // 4. 질문 채팅 저장
            Chat chat = Chat.builder()
                    .chatRoom(room)
                    .senderType(Chat.SenderType.USER)
                    .message(request.getMsgQuestion())
                    .createdAt(LocalDateTime.now())
                    .build();
            chatRepository.save(chat);
            logger.info("사용자 메시지 저장 완료: {}", chat.getId());


            // 5. AI 응답 채팅 저장
            Chat aiChat = Chat.builder()
                    .chatRoom(room)
                    .senderType(Chat.SenderType.ASSISTANT)
                    .message(aiResponse) // 또는 `answer`
                    .createdAt(LocalDateTime.now())
                    .build();
            chatRepository.save(aiChat);
            logger.info("AI 메시지 저장 완료: {}", aiChat.getId());

            // 6. 응답 객체 구성
            ChatCreateResDto response = ChatCreateResDto.builder()
                    .answer(aiResponse)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("채팅 생성 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "채팅 생성(송/수신)")
    @GetMapping("/list/{chatRoomId}")
    public ResponseEntity<List<ChatListResDto>> chatList(@PathVariable Long chatRoomId, @ModelAttribute ChatListReqDto request) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Chat> chats = chatRepository.findByChatRoomIdAndCreatedAtBeforeOrderByCreatedAtAsc(
                chatRoomId, request.getBefore(), pageable);
        List<ChatListResDto> response = chats.stream()
                .map(chat -> new ChatListResDto().fromEntity(chat))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
