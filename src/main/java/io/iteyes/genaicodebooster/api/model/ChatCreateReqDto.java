package io.iteyes.genaicodebooster.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatCreateReqDto {
    private String userId;
    private Long chatRoomId;
    private String msgQuestion;

    private String msgList;
}
