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
public class MemberUpdateReqDto {
    private String id;
    private String passwordHash;
    private String email;
    private String name;
    public Member toEntity() {
        return Member.builder()
                .id(id)
                .email(email)
                .passwordHash(passwordHash)
                .name(name)
                .build();
    }
}
