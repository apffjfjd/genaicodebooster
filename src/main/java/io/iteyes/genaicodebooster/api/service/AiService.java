package io.iteyes.genaicodebooster.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iteyes.genaicodebooster.api.model.AiRequest;
import io.iteyes.genaicodebooster.api.model.AiResponse;
import io.iteyes.genaicodebooster.domain.chat.repository.ChatRepository;
import io.iteyes.genaicodebooster.domain.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AiService {
    private final ChatRepository chatRepository; // 질문/응답 저장용
    private final ChatRoomRepository chatRoomRepository;
    private final ObjectMapper objectMapper;
//    private final WebClient webClient = WebClient.builder()
//            .baseUrl("http://192.168.0.145:8000")
//            .build();
    private static final Logger logger = LoggerFactory.getLogger(AiService.class);

//    public String sendMessage(String question, String history) {
    public String sendMessage(String question) {
        AiRequest request = AiRequest.builder()
                .prompt(question)
//                .history(history)
                .build();

        String url = "http://192.168.0.145:8000/ask";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AiRequest> requestEntity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<AiResponse> response = restTemplate.postForEntity(url, requestEntity, AiResponse.class);
            return response.getBody().getAnswer(); // ← 응답 구조에 맞게
        } catch (Exception e) {
            logger.error("AI 서버 호출 실패", e);
            return "AI 응답 실패";
        }
    }


//    public Flux<String> streamAndSave(AiRequest aiRequest, Long chatRoomId) {
//        StringBuilder fullAnswer = new StringBuilder();
//
//        return webClient.post()
//                .uri("/askstream")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(aiRequest)
//                .accept(MediaType.valueOf("application/x-ndjson"))
//                .header("Transfer-Encoding", "chunked")  // 여기에 헤더 추가
//                .retrieve()
//                .bodyToFlux(String.class)
//                .map(line -> {
//                    try {
//                        logger.info("수신한 줄: {}", line);
//                        JsonNode json = objectMapper.readTree(line);
//                        String token = json.get("response").asText();
//                        fullAnswer.append(token);
//                        return objectMapper.writeValueAsString(AiResponse.builder().answer(token).build()) + "\n";
//                    } catch (Exception e) {
//                        return "\n"; // 파싱 실패 시 빈 응답
//                    }
//                })
//                .doOnComplete(() -> {
//                    try {
//                        ChatRoom room = chatRoomRepository.findById(chatRoomId)
//                                .orElseThrow(() -> new IllegalArgumentException("채팅방 없음"));
//
//                        Chat aiChat = Chat.builder()
//                                .chatRoom(room)
//                                .senderType(Chat.SenderType.ASSISTANT)
//                                .message(fullAnswer.toString())
//                                .createdAt(LocalDateTime.now())
//                                .build();
//
//                        chatRepository.save(aiChat);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                });
        // Flux.create를 사용하여 비동기 스트리밍 처리
//        return Flux.create(sink -> {
//            webClient.post()
//                    .uri("/askstream") // FastAPI 서버
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .bodyValue(aiRequest)
//                    .retrieve()
//                    .bodyToFlux(String.class)
//                    .doOnNext(line -> {
//                        // 각 줄을 읽어서 sink에 전달
//                        try {
//                            // JSON 파싱 (응답 데이터 구조에 맞게)
//                            JsonNode json = objectMapper.readTree(line);
//                            String responseText = json.get("response").asText();
//                            // 실시간으로 클라이언트로 전송
//                            sink.next(responseText);
//                        } catch (Exception e) {
//                            sink.error(new RuntimeException("Error parsing AI response"));
//                        }
//                    })
//                    .doOnTerminate(sink::complete)  // 스트림 종료 시 complete 호출
//                    .subscribe();
//        });
//    }

//    public Flux<ServerSentEvent<String>> streamAndSave(AiRequest aiRequest, Long chatRoomId) {
//        StringBuilder fullAnswer = new StringBuilder();
//
//        return webClient.post()
//                .uri("/askstream")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(aiRequest)
//                .accept(MediaType.valueOf("application/x-ndjson"))
//                .retrieve()
//                .bodyToFlux(String.class)
//                .map(line -> {
//                    try {
//                        logger.info("수신한 줄: {}", line);
//                        JsonNode json = objectMapper.readTree(line);
//                        String token = json.get("response").asText();
//                        fullAnswer.append(token);
//
//                        // ServerSentEvent 객체로 반환
//                        return ServerSentEvent.<String>builder()
//                                .data(token)
//                                .build();
//                    } catch (Exception e) {
//                        logger.error("JSON 파싱 에러: ", e);
//                        return ServerSentEvent.<String>builder()
//                                .data("")
//                                .build();
//                    }
//                })
//                .doOnComplete(() -> {
//                    try {
//                        ChatRoom room = chatRoomRepository.findById(chatRoomId)
//                                .orElseThrow(() -> new IllegalArgumentException("채팅방 없음"));
//
//                        Chat aiChat = Chat.builder()
//                                .chatRoom(room)
//                                .senderType(Chat.SenderType.ASSISTANT)
//                                .message(fullAnswer.toString())
//                                .createdAt(LocalDateTime.now())
//                                .build();
//
//                        chatRepository.save(aiChat);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                });
//    }


//    public String sendMessage(String question) {
//        AiRequest request = AiRequest.builder()
//                .prompt(question)
//                .build();
//
//        try {
//            JsonNode response = webClient.post()
//                    .uri("/ask")
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .bodyValue(request)
//                    .retrieve()
//                    .bodyToMono(JsonNode.class)
//                    .block(); // ✅ 여기서 값을 꺼내야 함
//
//
//            return response.get("answer").asText();
//        } catch (Exception e) {
//            logger.error("AI 서버 호출 실패", e);
//            return "AI 응답 실패";
//        }
//    }

}
