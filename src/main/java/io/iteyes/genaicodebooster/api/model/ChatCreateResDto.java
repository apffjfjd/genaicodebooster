package io.iteyes.genaicodebooster.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatCreateResDto {
    private String answer;
//    private int tokenCount;         // gemma3 8.5b 모델은 최대 8192개 토큰 제한
//    private String modelName;
}
