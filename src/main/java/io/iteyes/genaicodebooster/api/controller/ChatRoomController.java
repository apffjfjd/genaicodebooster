package io.iteyes.genaicodebooster.api.controller;

import io.iteyes.genaicodebooster.api.model.ChatRoomCreateReqDto;
import io.iteyes.genaicodebooster.api.model.ChatRoomListResDto;
import io.iteyes.genaicodebooster.api.service.ChatRoomService;
import io.iteyes.genaicodebooster.domain.chat.entity.ChatRoom;
import io.iteyes.genaicodebooster.domain.chat.repository.ChatRepository;
import io.iteyes.genaicodebooster.domain.chat.repository.ChatRoomRepository;
import io.iteyes.genaicodebooster.domain.member.entity.Member;
import io.iteyes.genaicodebooster.domain.member.repository.MemberRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chatrooms")
@RequiredArgsConstructor
public class ChatRoomController {
    private static final Logger logger = LoggerFactory.getLogger(ChatRoomController.class);

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final ChatRepository chatRepository;
    private final ChatRoomService chatRoomService;

    // 채팅창 생성
    @Operation(
            summary = "채팅창 생성"
    )
    @PostMapping
    public ResponseEntity<ChatRoom> create(@Valid @RequestBody ChatRoomCreateReqDto request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 멤버입니다."));
        ChatRoom chatRoom = ChatRoom.builder()
                .title(request.getTitle())
                .member(member)
                .build();
        ChatRoom saved = chatRoomRepository.save(chatRoom);
        logger.info("신규 채팅창 등록 완료: {}", saved.getId());
        return ResponseEntity.ok(saved);
    }

    // 채팅창 삭제
    @Operation(summary = "채팅창 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.debug("채팅창 삭제 요청 - ID: {}", id);
        chatRoomService.deleteChatRoom(id);
        return ResponseEntity.ok().build();
    }

    // 채팅창 목록 조회
    @Operation(summary = "채팅창 목록 조회")
    @GetMapping("/list/{memberId}")
    public ResponseEntity<List<ChatRoomListResDto>> list(@PathVariable String memberId) {
        logger.debug("채팅창 목록 조회 요청");
        List<ChatRoom> chatRooms = chatRoomRepository.findAllByMemberOrderByLastChatTimeDesc(memberId);
        List<ChatRoomListResDto> results = chatRooms.stream()
                .map(room -> ChatRoomListResDto.builder()
                        .id(room.getId())
                        .title(room.getTitle())
                        .build())
                .toList();
        logger.info("총 {}명의 채팅창 조회됨", results.size());
        return ResponseEntity.ok(results);
    }

}
