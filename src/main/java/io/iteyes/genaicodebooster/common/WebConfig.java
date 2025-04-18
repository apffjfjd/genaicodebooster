package io.iteyes.genaicodebooster.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 이 클래스는 Spring MVC의 설정을 커스터마이즈하기 위해 사용됩니다.
 * <p>
 * <p>
 * {@code WebMvcConfigurer} 인터페이스를 구현하여 애플리케이션의 MVC 설정을 확장하거나 수정할 수 있습니다.
 * 주로 인터셉터, 리소스 핸들러, 뷰 컨트롤러 등의 설정을 추가하는 데 사용됩니다.
 * </p>
 *
 * <p>
 * Spring Boot에서 기본 제공하는 설정을 유지하면서, 추가적인 설정을 위해 {@code WebMvcConfigurer}를 구현합니다.
 * </p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoggingInterceptor loggingInterceptor;

    public WebConfig(LoggingInterceptor loggingInterceptor) {
        this.loggingInterceptor = loggingInterceptor;
    }

    /**
     * 인터셉터를 등록합니다.
     *
     * @param registry InterceptorRegistry 객체
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("**/healthcheck/**", "**/swagger-ui/**");
    }

    /**
     * api 연동테스트중 CORS 해결을 위해 추가
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedOrigins(
                        "http://192.168.0.24:3000"
//                        "http://192.168.0.24:3000",  // 허용할 첫 번째 origin
//                        "http://218.232.110.71:33000", // 허용할 두 번째 origin
//                        "http://192.168.0.201:33000", // 허용할 두 번째 origin
//                        "http://192.168.0.42:33000" // 허용할 두 번째 origin
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드
                .allowedHeaders("*") // 허용할 헤더
                .allowCredentials(true); // 자격 증명 허용
    }
}
