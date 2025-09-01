package com.library.controller;

import com.library.entity.Account;
import com.library.entity.Notification;
import com.library.repository.AccountRepository;
import com.library.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AccountRepository accountRepository;

    // Lấy danh sách thông báo của user hiện tại
    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications() {
        try {
            Account currentUser = getCurrentUser();
            List<Notification> notifications = notificationService.getUserNotifications(currentUser.getId());
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Đếm số thông báo chưa đọc
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        try {
            Account currentUser = getCurrentUser();
            Long count = notificationService.getUnreadCount(currentUser.getId());
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(0L);
        }
    }

    // Đánh dấu một thông báo đã đọc
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<String> markAsRead(@PathVariable Integer notificationId) {
        try {
            Account currentUser = getCurrentUser();
            notificationService.markAsRead(notificationId, currentUser.getId());
            return ResponseEntity.ok("Notification marked as read");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Đánh dấu tất cả thông báo đã đọc
    @PutMapping("/mark-all-read")
    public ResponseEntity<String> markAllAsRead() {
        try {
            Account currentUser = getCurrentUser();
            notificationService.markAllAsRead(currentUser.getId());
            return ResponseEntity.ok("All notifications marked as read");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Lấy chi tiết một thông báo
    @GetMapping("/{notificationId}")
    public ResponseEntity<Notification> getNotificationDetail(@PathVariable Integer notificationId) {
        try {
            Account currentUser = getCurrentUser();
            List<Notification> userNotifications = notificationService.getUserNotifications(currentUser.getId());

            Notification notification = userNotifications.stream()
                    .filter(n -> n.getId().equals(notificationId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Notification not found"));

            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    private Account getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}