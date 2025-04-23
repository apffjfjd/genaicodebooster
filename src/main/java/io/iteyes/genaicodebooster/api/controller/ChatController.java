package io.iteyes.genaicodebooster.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.iteyes.genaicodebooster.api.model.AiRequest;
import io.iteyes.genaicodebooster.api.model.ChatCreateReqDto;
import io.iteyes.genaicodebooster.api.model.ChatListReqDto;
import io.iteyes.genaicodebooster.api.model.ChatListResDto;
import io.iteyes.genaicodebooster.api.service.AiService;
import io.iteyes.genaicodebooster.api.service.ChatService;
import io.iteyes.genaicodebooster.domain.chat.entity.Chat;
import io.iteyes.genaicodebooster.domain.chat.entity.ChatRoom;
import io.iteyes.genaicodebooster.domain.chat.repository.ChatRepository;
import io.iteyes.genaicodebooster.domain.chat.repository.ChatRoomRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    @Qualifier("mvcTaskExecutor")
    private final TaskExecutor taskExecutor;
    private final ChatService chatService;
    private final AiService aiService;
    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Value("${chat.list.default-limit}")
    private int limit;


    @Operation(summary = "채팅 리스트 조회")
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

    @Operation(summary = "채팅 생성(송/수신)")
    @Transactional
    @PostMapping(produces = "application/x-ndjson;charset=UTF-8")
    public ResponseBodyEmitter createChat(@RequestBody ChatCreateReqDto request) {
        ChatRoom room = chatRoomRepository.findById(request.getChatRoomId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방 없음"));

        Chat userChat = Chat.builder()
                .chatRoom(room)
                .senderType(Chat.SenderType.USER)
                .message(request.getMsgQuestion())
                .createdAt(LocalDateTime.now())
                .build();

        chatRepository.save(userChat);

        ResponseBodyEmitter emitter = new ResponseBodyEmitter(300_000L); // ✅ SseEmitter → ResponseBodyEmitter 변경
        StringBuilder fullAnswer = new StringBuilder();

        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL("http://192.168.0.145:8000/askstream");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/x-ndjson");
                conn.setDoOutput(true);

                String requestBody = new ObjectMapper().writeValueAsString(
                        AiRequest.builder().prompt(request.getMsgQuestion()).build()
                );
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(requestBody.getBytes(StandardCharsets.UTF_8));
                }

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {

                    ObjectMapper objectMapper = new ObjectMapper();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logger.info("수신된 청크: {}", line);
                        JsonNode json = objectMapper.readTree(line);
                        String token = json.get("response").asText();
                        fullAnswer.append(token);

                        // ✅ NDJSON 포맷으로 한 줄 전송
                        String ndjson = objectMapper.writeValueAsString(Map.of("answer", token));
                        emitter.send(ndjson + "\n", MediaType.APPLICATION_JSON);
                    }

                    // 저장
                    Chat aiChat = Chat.builder()
                            .chatRoom(room)
                            .senderType(Chat.SenderType.ASSISTANT)
                            .message(fullAnswer.toString())
                            .createdAt(LocalDateTime.now())
                            .build();
                    chatRepository.save(aiChat);

                    emitter.complete();
                }
            } catch (Exception e) {
                logger.error("스트리밍 오류", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }


//    @PostMapping(value = "/stream")
//    public SseEmitter streamChat(@RequestBody ChatCreateReqDto request) {
//        // 질문 메시지 저장
//        ChatRoom room = chatRoomRepository.findById(request.getChatRoomId())
//                .orElseThrow(() -> new IllegalArgumentException("채팅방 없음"));
//
//        Chat userChat = Chat.builder()
//                .chatRoom(room)
//                .senderType(Chat.SenderType.USER)
//                .message(request.getMsgQuestion())
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        chatRepository.save(userChat);
//
//        SseEmitter emitter = new SseEmitter(-1L); // 타임아웃 없음
//        StringBuilder fullAnswer = new StringBuilder();
//
//        // 비동기로 AI 서비스에 요청
//        CompletableFuture.runAsync(() -> {
//            try {
//                // 외부 API에 HTTP 연결
//                URL url = new URL("http://192.168.0.145:8000/askstream");
//                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                conn.setRequestMethod("POST");
//                conn.setRequestProperty("Content-Type", "application/json");
//                conn.setRequestProperty("Accept", "application/x-ndjson");
//                conn.setDoOutput(true);
//
//                // 요청 본문 작성
//                String requestBody = new ObjectMapper().writeValueAsString(
//                        AiRequest.builder().prompt(request.getMsgQuestion()).build()
//                );
//                try (OutputStream os = conn.getOutputStream()) {
//                    os.write(requestBody.getBytes(StandardCharsets.UTF_8));
//                }
//
//                // 응답 스트림 읽기
//                try (BufferedReader reader = new BufferedReader(
//                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
//
//                    String line;
//                    ObjectMapper objectMapper = new ObjectMapper();
//                    while ((line = reader.readLine()) != null) {
//                        logger.info("수신된 청크: {}", line);
//                        JsonNode json = objectMapper.readTree(line);
//                        String token = json.get("response").asText();
//                        fullAnswer.append(token);
//
//                        // 클라이언트에 전송
//                        emitter.send(token, MediaType.TEXT_PLAIN);
//                    }
//
//                    // 스트림 완료 후 DB에 저장
//                    Chat aiChat = Chat.builder()
//                            .chatRoom(room)
//                            .senderType(Chat.SenderType.ASSISTANT)
//                            .message(fullAnswer.toString())
//                            .createdAt(LocalDateTime.now())
//                            .build();
//                    chatRepository.save(aiChat);
//
//                    emitter.complete();
//                }
//            } catch (Exception e) {
//                emitter.completeWithError(e);
//            }
//        });
//
//        return emitter;
//    }


//    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<ServerSentEvent<String>> streamAndSave(@RequestBody ChatCreateReqDto request) {
//        // 질문 메시지는 바로 저장
//        ChatRoom room = chatRoomRepository.findById(request.getChatRoomId())
//                .orElseThrow(() -> new IllegalArgumentException("채팅방 없음"));
//
//        Chat userChat = Chat.builder()
//                .chatRoom(room)
//                .senderType(Chat.SenderType.USER)
//                .message(request.getMsgQuestion())
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        chatRepository.save(userChat);
//
//        AiRequest aiReq = AiRequest.builder()
//                .prompt(request.getMsgQuestion())
//                .build();
//
//        return aiService.streamAndSave(aiReq, request.getChatRoomId());
//    }
//
//    @PostMapping(value = "/stream-flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    @ResponseBody
//    public ResponseBodyEmitter streamResponse(@RequestBody ChatCreateReqDto request) {
//        // 질문 메시지는 바로 저장
//        ChatRoom room = chatRoomRepository.findById(request.getChatRoomId())
//                .orElseThrow(() -> new IllegalArgumentException("채팅방 없음"));
//
//        Chat userChat = Chat.builder()
//                .chatRoom(room)
//                .senderType(Chat.SenderType.USER)
//                .message(request.getMsgQuestion())
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        chatRepository.save(userChat);
//
//        AiRequest aiReq = AiRequest.builder()
//                .prompt(request.getMsgQuestion())
//                .build();
//
//        // SSE 이미터 설정
//        SseEmitter emitter = new SseEmitter(-1L); // 타임아웃 없음
//        StringBuilder fullAnswer = new StringBuilder();
//
//        // 비동기 처리를 위한 ExecutorService
//        ExecutorService executor = Executors.newSingleThreadExecutor();
//
//        executor.execute(() -> {
//            try {
//                // WebClient를 사용하여 AI 서비스에 요청 보내기
////                WebClient webClient = WebClient.create();
//                webClient.post()
//                        .uri("/askstream") // AI 서비스 URI
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .bodyValue(aiReq)
//                        .accept(MediaType.valueOf("application/x-ndjson"))
//                        .retrieve()
//                        .bodyToFlux(String.class)
//                        .subscribe(
//                                line -> {
//                                    try {
//                                        // JSON 파싱
//                                        logger.info("수신된 청크: {}", line);
//                                        ObjectMapper objectMapper = new ObjectMapper();
//                                        JsonNode json = objectMapper.readTree(line);
//                                        String token = json.get("response").asText();
//                                        fullAnswer.append(token);
//
//                                        // SSE 이벤트로 전송
//                                        emitter.send(token, MediaType.TEXT_PLAIN);
//                                    } catch (Exception e) {
//                                        e.printStackTrace();
//                                    }
//                                },
//                                error -> {
//                                    emitter.completeWithError(error);
//                                },
//                                () -> {
//                                    try {
//                                        // AI 응답 저장
//                                        Chat aiChat = Chat.builder()
//                                                .chatRoom(room)
//                                                .senderType(Chat.SenderType.ASSISTANT)
//                                                .message(fullAnswer.toString())
//                                                .createdAt(LocalDateTime.now())
//                                                .build();
//                                        chatRepository.save(aiChat);
//
//                                        emitter.complete();
//                                    } catch (Exception e) {
//                                        emitter.completeWithError(e);
//                                    }
//                                }
//                        );
//            } catch (Exception e) {
//                emitter.completeWithError(e);
//            }
//        });
//
//        executor.shutdown();
//        return emitter;
//    }

//    @PostMapping(value = "/text", produces = MediaType.TEXT_PLAIN_VALUE)
//    public ResponseBodyEmitter streamText(@RequestBody ChatCreateReqDto request) {
//        ResponseBodyEmitter emitter = new ResponseBodyEmitter();
//        ExecutorService executor = Executors.newSingleThreadExecutor();  // ✨ 쓰레드 명시
//
//        executor.submit(() -> {
//            try {
//                String[] messages = request.getMsgQuestion().split(",");
//
//                for (String msg : messages) {
//                    logger.info("수신 문자: {}", msg);
//                    emitter.send(msg.trim() + "\n");
//                    TimeUnit.SECONDS.sleep(2);
//                }
//                emitter.complete();
//            } catch (IOException | InterruptedException e) {
//                emitter.completeWithError(e);
//            } finally {
//                executor.shutdown();  // ✨ 요청 종료 후 쓰레드 종료
//            }
//        });
//
//        return emitter;
//    }

//    @PostMapping(value = "/text", produces = MediaType.TEXT_PLAIN_VALUE)
//    public ResponseEntity<StreamingResponseBody> stream(@RequestBody ChatCreateReqDto request) {
//
//        StreamingResponseBody stream = outputStream -> {
//            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);
//
//            try {
//                String[] messages = request.getMsgQuestion().split(",");
//                for (String msg : messages) {
//                    writer.write(msg.trim() + "\n");
//                    writer.flush();  // flush가 핵심! → chunk로 나감
//                    Thread.sleep(2000); // 2초 대기
//                }
//            } catch (Exception e) {
//                writer.write("에러 발생\n");
//                writer.flush();
//            }
//        };
//
//        return ResponseEntity
//                .ok()
//                .header(HttpHeaders.TRANSFER_ENCODING, "chunked")
//                .contentType(MediaType.TEXT_PLAIN)
//                .body(stream);

//    @Operation(summary = "채팅 생성(송/수신)")
//    @Transactional
//    @PostMapping
//    public ResponseEntity<ChatCreateResDto> createChat(@Valid @RequestBody ChatCreateReqDto request) {
//
//        logger.debug("채팅 생성 요청 수신: {}", request);
//
//        try {
//            // 1. 사용자 및 채팅방 유효성 검증 (생략 가능)
//            // Optional<User> user = userRepository.findById(request.getUserId());
//            // Optional<ChatRoom> room = chatRoomRepository.findById(request.getRoomId());
//
//            // 2. 채팅룸 검증
//            ChatRoom room = chatRoomRepository.findById(request.getChatRoomId())
//                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));
//
//            // 3. AI 서버로 메시지 전달
////            String aiResponse = aiService.sendMessage(request.getMsgQuestion(), request.getMsgList());
//            String aiResponse = aiService.sendMessage(request.getMsgQuestion());
//            logger.info("AI 응답 수신: {}", aiResponse);
//            if (aiResponse == null || aiResponse.isBlank()) {
//                logger.warn("AI 응답 없음 → 저장 생략");
//                return ResponseEntity.status(204).build(); // No Content or 적절한 처리
//            }
//
//            // 4. 질문 채팅 저장
//            Chat chat = Chat.builder()
//                    .chatRoom(room)
//                    .senderType(Chat.SenderType.USER)
//                    .message(request.getMsgQuestion())
//                    .createdAt(LocalDateTime.now())
//                    .build();
//            chatRepository.save(chat);
//            logger.info("사용자 메시지 저장 완료: {}", chat.getId());
//
//
//            // 5. AI 응답 채팅 저장
//            Chat aiChat = Chat.builder()
//                    .chatRoom(room)
//                    .senderType(Chat.SenderType.ASSISTANT)
//                    .message(aiResponse) // 또는 `answer`
//                    .createdAt(LocalDateTime.now())
//                    .build();
//            chatRepository.save(aiChat);
//            logger.info("AI 메시지 저장 완료: {}", aiChat.getId());
//
//            // 6. 응답 객체 구성
//            ChatCreateResDto response = ChatCreateResDto.builder()
//                    .answer(aiResponse)
//                    .build();
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            logger.error("채팅 생성 중 오류 발생", e);
//            return ResponseEntity.internalServerError().build();
//        }
//    }
    @PostMapping(value = "/text", produces = "application/x-ndjson;charset=UTF-8")
    public ResponseBodyEmitter stream(@RequestBody ChatCreateReqDto request) {
        ResponseBodyEmitter emitter = new ResponseBodyEmitter();

        taskExecutor.execute(() -> {
            try {
                for (String token : request.getMsgQuestion().split(",")) {
                    logger.info("수신한 토큰: {}", token);
                    String json = new ObjectMapper().writeValueAsString(Map.of("answer", token.trim()));
                    emitter.send(
                            json + "\n",
                            new MediaType("application", "x-ndjson", StandardCharsets.UTF_8));
                    Thread.sleep(300);
                }
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

}
