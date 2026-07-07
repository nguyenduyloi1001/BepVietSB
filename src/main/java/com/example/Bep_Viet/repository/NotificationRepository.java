package com.example.Bep_Viet.repository;

import com.example.Bep_Viet.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Lấy tất cả thông báo của 1 user, mới nhất trước
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Lấy thông báo chưa đọc của user
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    // Đếm số thông báo chưa đọc (dùng cho badge icon)
    long countByUserIdAndIsRead(Long userId, Boolean isRead);

    // Đánh dấu tất cả thông báo của user là đã đọc
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    void markAllAsRead(@Param("userId") Long userId);
}
