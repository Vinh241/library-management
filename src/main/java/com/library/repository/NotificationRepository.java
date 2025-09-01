package com.library.repository;

import com.library.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    // Lấy thông báo của user (chưa đọc trước)
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId ORDER BY n.isRead ASC, n.sentAt DESC")
    List<Notification> findByRecipientIdOrderByReadAndSentAt(@Param("recipientId") Integer recipientId);

    // Đếm số thông báo chưa đọc
    Long countByRecipientIdAndIsReadFalse(Integer recipientId);

    // Lấy thông báo theo loại và user
    List<Notification> findByRecipientIdAndType(Integer recipientId, Notification.NotificationType type);

    // Lấy thông báo theo transaction để tránh duplicate
    List<Notification> findByTransactionIdAndType(Integer transactionId, Notification.NotificationType type);

    // Xóa thông báo cũ (có thể dùng cho cleanup)
    @Query("DELETE FROM Notification n WHERE n.sentAt < :beforeDate")
    void deleteOldNotifications(@Param("beforeDate") LocalDateTime beforeDate);
}