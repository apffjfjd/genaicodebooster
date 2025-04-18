package io.iteyes.genaicodebooster.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * HTTP 요청과 응답을 로깅하기 위한 인터셉터 클래스
 *
 * 이 클래스는 모든 HTTP 요청과 응답에 대한 정보를 로깅합니다.
 * - 요청이 컨트롤러에 도달하기 전에 요청 정보를 로깅합니다.
 * - 요청 처리 후 클라이언트에 반환되는 응답 정보를 로깅합니다.
 *
 * 이를 통해 애플리케이션의 HTTP 요청과 응답을 추적하고 디버깅 및 모니터링할 수 있습니다.
 */

@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LogManager.getLogger(LoggingInterceptor.class);

    /**
     * 요청이 컨트롤러에 도달하기 전에 호출됩니다.
     * 요청 본문(Request Body)을 포함한 요청 정보를 로깅합니다.
     *
     * @param request  HttpServletRequest 객체
     * @param response HttpServletResponse 객체
     * @param handler  현재 요청을 처리할 핸들러
     * @return 요청 처리를 계속 진행하려면 true 반환
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // ContentCachingRequestWrapper로 래핑된 요청만 로깅 처리
        if (request instanceof ContentCachingRequestWrapper) {
            logRequest((ContentCachingRequestWrapper) request);
        }
        return true;
    }

    /**
     * 요청이 완료된 후 호출됩니다.
     * 응답 본문(Response Body)을 포함한 응답 정보를 로깅합니다.
     *
     * @param request  HttpServletRequest 객체
     * @param response HttpServletResponse 객체
     * @param handler  요청을 처리한 핸들러
     * @param ex       요청 처리 중 발생한 예외(있을 경우)
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws IOException {
        if (request instanceof ContentCachingRequestWrapper && response instanceof ContentCachingResponseWrapper) {
            logResponse((ContentCachingRequestWrapper) request, (ContentCachingResponseWrapper) response);
            // 캐싱된 응답 데이터를 클라이언트로 복사
            ((ContentCachingResponseWrapper) response).copyBodyToResponse();
        }
    }

    /**
     * 요청 정보를 로깅합니다.
     * 요청의 스레드 정보, HTTP 메서드, URL, 본문 내용을 기록합니다.
     *
     * @param request ContentCachingRequestWrapper로 래핑된 요청 객체
     */
    private void logRequest(ContentCachingRequestWrapper request) {
        String threadInfo = Thread.currentThread().getName(); // 현재 스레드 이름
        String requestBody = getRequestBody(request); // 요청 본문
        StringBuilder sb = new StringBuilder("\n📥 [REQUEST] ───────────────────────────────────────────\n");
        sb.append("  ▶ Thread   : ").append(threadInfo).append("\n");
        sb.append("  ▶ Method   : ").append(request.getMethod()).append("\n");
        sb.append("  ▶ URI      : ").append(request.getRequestURI()).append("\n");
        sb.append("  ▶ Headers  : ").append(Collections.list(request.getHeaderNames()).stream()
                .map(h -> h + "=" + request.getHeader(h))
                .collect(Collectors.joining(", "))).append("\n");
        sb.append("  ▶ Body     : ").append(formatJson(requestBody)).append("\n");
        sb.append("──────────────────────────────────────────────────────────");
        logger.info(sb.toString());
    }

    /**
     * 응답 정보를 로깅합니다.
     * 응답의 스레드 정보, HTTP 메서드, URL, 상태 코드, 본문 내용을 기록합니다.
     *
     * @param request  ContentCachingRequestWrapper로 래핑된 요청 객체
     * @param response ContentCachingResponseWrapper로 래핑된 응답 객체
     */
    private void logResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        String threadInfo = Thread.currentThread().getName(); // 현재 스레드 이름
        String responseBody = getResponseBody(response); // 응답 본문
        StringBuilder sb = new StringBuilder("\n📤 [RESPONSE] ──────────────────────────────────────────\n");
        sb.append("  ◀ Thread   : ").append(threadInfo).append("\n");
        sb.append("  ◀ Status   : ").append(response.getStatus()).append("\n");
        sb.append("  ◀ URI      : ").append(request.getRequestURI()).append("\n");
        sb.append("  ◀ Body     : ").append(formatJson(responseBody)).append("\n");
        sb.append("──────────────────────────────────────────────────────────");
        logger.info(sb.toString());
    }

    /**
     * 요청 본문(Request Body)을 문자열로 변환합니다.
     *
     * @param raw 요청 or 응답 String 객체
     * @return 요청 본문 문자열
     */
    private String formatJson(String raw) {
        if (raw == null || raw.isBlank()) return "{}";
        try {
            ObjectMapper mapper = new ObjectMapper();
            Object json = mapper.readValue(raw, Object.class);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (Exception e) {
            return "JSON pretty printing 실패 - raw: " + raw; // JSON 파싱 실패 시 원본 그대로 출력
        }
    }


    /**
     * 요청 본문(Request Body)을 문자열로 변환합니다.
     *
     * @param request ContentCachingRequestWrapper로 래핑된 요청 객체
     * @return 요청 본문 문자열
     */
    private String getRequestBody(ContentCachingRequestWrapper request) {
        // 요청 본문을 UTF-8 문자열로 변환
        byte[] content = request.getContentAsByteArray();
        return new String(content, StandardCharsets.UTF_8);
    }

    /**
     * 응답 본문(Response Body)을 문자열로 변환합니다.
     *
     * @param response ContentCachingResponseWrapper로 래핑된 응답 객체
     * @return 응답 본문 문자열
     */
    private String getResponseBody(ContentCachingResponseWrapper response) {
        // 응답 본문을 UTF-8 문자열로 변환
        byte[] content = response.getContentAsByteArray();
        return new String(content, StandardCharsets.UTF_8);
    }
}
