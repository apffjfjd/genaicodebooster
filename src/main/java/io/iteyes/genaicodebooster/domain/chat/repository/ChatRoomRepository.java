package io.iteyes.genaicodebooster.domain.chat.repository;

import io.iteyes.genaicodebooster.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    List<ChatRoom> findAllByMember_IdOrderByUpdatedAtDesc(String memberId);

    @Query(value = """
        SELECT cr.*
        FROM chat_rooms cr
        JOIN (
            SELECT chat_room_id, MAX(created_at) AS last_chat_time
            FROM chats
            GROUP BY chat_room_id
        ) latest ON cr.id = latest.chat_room_id
        WHERE cr.member_id = :memberId
        ORDER BY latest.last_chat_time DESC
    """, nativeQuery = true)
    List<ChatRoom> findAllByMemberOrderByLastChatTimeDesc(@Param("memberId") String memberId);


}
