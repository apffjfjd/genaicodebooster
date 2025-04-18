package io.iteyes.genaicodebooster.api.model;

import io.iteyes.genaicodebooster.domain.chat.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomCreateReqDto {
    private String memberId;
    private String title;
    private String devLang;
}
