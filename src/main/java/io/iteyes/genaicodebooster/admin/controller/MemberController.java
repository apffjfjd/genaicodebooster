package io.iteyes.genaicodebooster.admin.controller;

import io.iteyes.genaicodebooster.admin.model.*;
import io.iteyes.genaicodebooster.domain.member.entity.Member;
import io.iteyes.genaicodebooster.domain.member.repository.MemberRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/admin/members")
@RequiredArgsConstructor
public class MemberController {
    private static final Logger logger = LoggerFactory.getLogger(MemberController.class);

    private final MemberRepository memberRepository;

    @Value("${member.list.default-limit}")
    private int limit;


    // 회원 등록
    @Operation(summary = "회원 등록")
    @PostMapping
    public ResponseEntity<Member> create(@Valid @RequestBody MemberCreateReqDto request) {
        logger.info("받은 DTO: {}", request);
        logger.info("받은 id: '{}'", request.getId());
        logger.info("받은 passwordHash: '{}'", request.getPasswordHash());
        logger.info("받은 email: '{}'", request.getEmail());
        logger.info("받은 name: '{}'", request.getName());

        if (request.getId() == null || request.getId().isBlank()) {
            logger.error("🚨🚨🚨 ID가 null 또는 blank 입니다!");
        }

        // 기존 로직
        if (memberRepository.count() >= limit) {
            logger.warn("멤버 수 제한 초과로 등록 거부됨");
            return ResponseEntity.status(403).build();
        }
        Member member = request.toEntity();
        Member response = memberRepository.save(member);
        logger.info("신규 회원 등록 완료: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    // 회원 정보 조회
    @Operation(summary = "회원 정보 조회")
    @GetMapping("/{id}")
    public ResponseEntity<MemberReadResDto> read(@PathVariable String id) {
        logger.info("받은 id: {}", id);

        if (id == null || id.isBlank()) {
            logger.error("🚨🚨🚨 ID가 null 또는 blank 입니다!");
        }

        Member member = memberRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 회원이 존재하지 않습니다: " + id));

        MemberReadResDto responseDto = new MemberReadResDto();
        MemberReadResDto response = responseDto.fromEntity(member);
        return ResponseEntity.ok(response);
    }

    // 회원 수정
    @Operation(summary = "회원 수정")
    @PutMapping("/{id}")
    public ResponseEntity<Member> update(@PathVariable String id, @Valid @RequestBody MemberUpdateReqDto request) {
        logger.debug("회원 수정 요청 - ID: {}, DTO: {}", id, request);
        logger.info("받은 id: '{}'", request.getId());
        logger.info("받은 passwordHash: '{}'", request.getPasswordHash());
        logger.info("받은 email: '{}'", request.getEmail());
        logger.info("받은 name: '{}'", request.getName());
        return memberRepository.findById(id)
                .map(member -> {
                    member.setName(request.getName());
                    member.setEmail(request.getEmail());
                    member.setPasswordHash(request.getPasswordHash());
                    Member updated = memberRepository.save(member);
                    logger.info("회원 수정 완료 - ID: {}", id);
                    return ResponseEntity.ok(updated);
                })
                .orElseGet(() -> {
                    logger.warn("회원 수정 실패 - ID 존재하지 않음: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    // 회원 삭제
    @Operation(summary = "회원 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        logger.debug("회원 삭제 요청 - ID: {}", id);
        if (memberRepository.existsById(id)) {
            memberRepository.deleteById(id);
            logger.info("회원 삭제 완료 - ID: {}", id);
            return ResponseEntity.ok().build();
        }
        logger.warn("회원 삭제 실패 - ID 존재하지 않음: {}", id);
        return ResponseEntity.notFound().build();
    }

    // 회원 목록 조회
    @Operation(summary = "회원 목록 조회")
    @GetMapping("/list")
    public ResponseEntity<List<MemberListResDto>> list() {
        logger.debug("회원 목록 조회 요청");
        List<MemberListResDto> members = memberRepository.findAll().stream()
                .map(MemberListResDto::fromEntity)
                .toList();
        logger.info("총 {}명의 회원 조회됨", members.size());
        return ResponseEntity.ok(members);
    }

    // 로그인
    @Operation(summary = "회원 로그인")
    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody MemberLoginReqDto request) {
        logger.debug("로그인 시도 - ID: {}", request.getId());
        return memberRepository.findById(request.getId())
                .filter(member -> member.getPasswordHash().equals(request.getPasswordHash()))
                .map(member -> {
                    logger.info("로그인 성공 - ID: {}, 이메일: {}", member.getId(), member.getEmail());
                    return ResponseEntity.ok("Login success for: " + member.getName());
                })
                .orElseGet(() -> {
                    logger.warn("로그인 실패 - 잘못된 자격증명: {}", request.getId());
                    return ResponseEntity.status(401).body("Invalid credentials");
                });
    }
}
