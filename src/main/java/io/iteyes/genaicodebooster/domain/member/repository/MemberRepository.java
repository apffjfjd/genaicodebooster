package io.iteyes.genaicodebooster.domain.member.repository;

import io.iteyes.genaicodebooster.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {
}
