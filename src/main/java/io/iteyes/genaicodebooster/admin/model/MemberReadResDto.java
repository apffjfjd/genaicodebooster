package io.iteyes.genaicodebooster.admin.model;

import io.iteyes.genaicodebooster.domain.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberReadResDto {
    private String id;
    private String passwordHash;
    private String email;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MemberReadResDto fromEntity(Member member) {
        return MemberReadResDto.builder()
                .id(member.getId())
                .name(member.getName())
                .passwordHash(member.getPasswordHash())
                .email(member.getEmail())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .build();
    }
}
