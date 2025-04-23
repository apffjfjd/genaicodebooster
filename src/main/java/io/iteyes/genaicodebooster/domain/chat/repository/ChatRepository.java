package io.iteyes.genaicodebooster.domain.chat.repository;

import io.iteyes.genaicodebooster.domain.chat.entity.Chat;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByChatRoomIdAndCreatedAtBeforeOrderByCreatedAtAsc(
            Long chatRoomId,
            LocalDateTime before,
            Pageable pageable
    );

    void deleteAllByChatRoom_Id(Long chatRoomId);


}
