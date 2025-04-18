package io.iteyes.genaicodebooster.admin.model;

import io.iteyes.genaicodebooster.domain.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberListResDto {
    private String id;
    private String name;
    private String email;

    public static MemberListResDto fromEntity(Member member) {
        return MemberListResDto.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .build();
    }
}
