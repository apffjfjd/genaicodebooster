package io.iteyes.genaicodebooster.domain.chat.repository;

import io.iteyes.genaicodebooster.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    List<ChatRoom> findAllByMember_Id(String memberId);

}
