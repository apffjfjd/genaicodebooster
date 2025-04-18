package io.iteyes.genaicodebooster.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iteyes.genaicodebooster.api.model.AiRequest;
import io.iteyes.genaicodebooster.api.model.AiResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiService {
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

}
