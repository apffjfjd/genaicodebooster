package io.iteyes.genaicodebooster.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    private static final Logger logger = LoggerFactory.getLogger(RestTemplateConfig.class);

    @Bean
    public RestTemplate restTemplate() {
        // SimpleClientHttpRequestFactory를 사용하여 타임아웃 설정
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(20000);  // 연결 타임아웃 (5초)
        factory.setReadTimeout(20000);     // 읽기 타임아웃 (5초)

        // 로그 추가
        logger.debug("RestTemplate configured with connect timeout: 5000ms and read timeout: 5000ms");

        // RestTemplate 생성 시 factory 사용
        return new RestTemplate(factory);
    }
}
