package io.iteyes.genaicodebooster.admin.model;

import io.iteyes.genaicodebooster.domain.member.entity.Member;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberCreateReqDto {
    @NotBlank(message = "ID는 필수입니다.")
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
