package com.library.service;

import com.library.entity.*;
import com.library.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BorrowingTransactionRepository borrowingTransactionRepository;

    /**
     * Helper method để lấy transaction với đầy đủ thông tin
     * Tránh lazy loading exception
     */
    private BorrowingTransaction getTransactionWithDetails(Integer transactionId) {
        return borrowingTransactionRepository.findByIdWithDetails(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
    }

    /**
     * Helper method để lấy book title an toàn
     */
    private String getBookTitleSafely(BorrowingTransaction transaction) {
        try {
            return transaction.getBookItem().getBookTitle().getTitle();
        } catch (Exception e) {
            // Nếu lazy loading fails, fetch lại transaction với details
            BorrowingTransaction fullTransaction = getTransactionWithDetails(transaction.getId());
            return fullTransaction.getBookItem().getBookTitle().getTitle();
        }
    }

    /**
     * Helper method để lấy item code an toàn
     */
    private String getItemCodeSafely(BorrowingTransaction transaction) {
        try {
            return transaction.getBookItem().getItemCode();
        } catch (Exception e) {
            // Nếu lazy loading fails, fetch lại transaction với details
            BorrowingTransaction fullTransaction = getTransactionWithDetails(transaction.getId());
            return fullTransaction.getBookItem().getItemCode();
        }
    }

    // Tạo thông báo quá hạn
    public void createOverdueNotification(BorrowingTransaction transaction) {
        // Kiểm tra đã có thông báo quá hạn cho transaction này chưa
        List<Notification> existingNotifications = notificationRepository
                .findByTransactionIdAndType(transaction.getId(), Notification.NotificationType.OVERDUE_NOTICE);

        if (!existingNotifications.isEmpty()) {
            return; // Đã có thông báo rồi, không tạo duplicate
        }

        Account borrower = accountRepository.findById(transaction.getBorrower().getId())
                .orElse(null);

        if (borrower == null) return;

        String bookTitle = getBookTitleSafely(transaction);
        String itemCode = getItemCodeSafely(transaction);
        String dueDate = transaction.getDueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        String title = "Sách quá hạn trả";
        String message = String.format(
                "Xin chào %s,\n\n" +
                        "Cuốn sách \"%s\" (Mã: %s) của bạn đã quá hạn trả từ ngày %s.\n" +
                        "Vui lòng trả sách sớm nhất có thể để tránh phí phạt.\n\n" +
                        "Thông tin chi tiết:\n" +
                        "- Tên sách: %s\n" +
                        "- Ngày đến hạn: %s\n" +
                        "- Thư viện: %s\n\n" +
                        "Cảm ơn bạn!",
                borrower.getFullName(),
                bookTitle,
                itemCode,
                dueDate,
                bookTitle,
                dueDate,
                "Thư viện" // Có thể lấy tên thư viện từ library_id
        );

        Notification notification = new Notification();
        notification.setRecipientId(transaction.getBorrower().getId());
        notification.setType(Notification.NotificationType.OVERDUE_NOTICE);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setTransactionId(transaction.getId());

        notificationRepository.save(notification);

        // Update transaction status thành OVERDUE
        transaction.setStatus(BorrowingTransaction.TransactionStatus.OVERDUE);
    }

    // Tạo thông báo nhắc nhở trước hạn (3 ngày trước)
    public void createDueReminderNotification(BorrowingTransaction transaction) {
        // Kiểm tra đã có reminder chưa
        List<Notification> existingReminders = notificationRepository
                .findByTransactionIdAndType(transaction.getId(), Notification.NotificationType.DUE_REMINDER);

        if (!existingReminders.isEmpty()) {
            return; // Đã có reminder
        }

        Account borrower = accountRepository.findById(transaction.getBorrower().getId())
                .orElse(null);

        if (borrower == null) return;

        String bookTitle = getBookTitleSafely(transaction);
        String itemCode = getItemCodeSafely(transaction);
        String dueDate = transaction.getDueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        String title = "Nhắc nhở trả sách";
        String message = String.format(
                "Xin chào %s,\n\n" +
                        "Cuốn sách \"%s\" (Mã: %s) sẽ đến hạn trả vào ngày %s.\n" +
                        "Vui lòng chuẩn bị trả sách đúng hạn.\n\n" +
                        "Nếu bạn muốn gia hạn, vui lòng liên hệ thư viện trước ngày đến hạn.\n\n" +
                        "Cảm ơn bạn!",
                borrower.getFullName(),
                bookTitle,
                itemCode,
                dueDate
        );

        Notification notification = new Notification();
        notification.setRecipientId(transaction.getBorrower().getId());
        notification.setType(Notification.NotificationType.DUE_REMINDER);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setTransactionId(transaction.getId());

        notificationRepository.save(notification);
    }

    // Tạo thông báo khi request được approve/reject
    public void createRequestNotification(BorrowRequest borrowRequest) {
        Account requester = accountRepository.findById(borrowRequest.getRequester().getId())
                .orElse(null);

        if (requester == null) return;

        String bookTitle;
        try {
            bookTitle = borrowRequest.getBookItem().getBookTitle().getTitle();
        } catch (Exception e) {
            // Fallback nếu lazy loading fails
            bookTitle = "Sách không xác định";
        }

        Notification notification = new Notification();
        notification.setRecipientId(borrowRequest.getRequester().getId());
        notification.setRequestId(borrowRequest.getId());

        if (borrowRequest.getStatus() == BorrowRequest.BorrowRequestStatus.APPROVED) {
            notification.setType(Notification.NotificationType.REQUEST_APPROVED);
            notification.setTitle("Yêu cầu mượn sách được chấp nhận");
            notification.setMessage(String.format(
                    "Xin chào %s,\n\n" +
                            "Yêu cầu mượn sách \"%s\" của bạn đã được chấp nhận.\n" +
                            "Bạn có thể đến thư viện để nhận sách.\n\n" +
                            "Ghi chú từ thủ thư: %s\n\n" +
                            "Cảm ơn bạn!",
                    requester.getFullName(),
                    bookTitle,
                    borrowRequest.getReviewNotes() != null ? borrowRequest.getReviewNotes() : "Không có"
            ));
        } else if (borrowRequest.getStatus() == BorrowRequest.BorrowRequestStatus.REJECTED) {
            notification.setType(Notification.NotificationType.REQUEST_REJECTED);
            notification.setTitle("Yêu cầu mượn sách bị từ chối");
            notification.setMessage(String.format(
                    "Xin chào %s,\n\n" +
                            "Rất tiếc, yêu cầu mượn sách \"%s\" của bạn đã bị từ chối.\n\n" +
                            "Lý do: %s\n" +
                            "Ghi chú: %s\n\n" +
                            "Bạn có thể tạo yêu cầu mới hoặc liên hệ thư viện để biết thêm chi tiết.",
                    requester.getFullName(),
                    bookTitle,
                    borrowRequest.getRejectionReason() != null ? borrowRequest.getRejectionReason() : "Không rõ",
                    borrowRequest.getReviewNotes() != null ? borrowRequest.getReviewNotes() : "Không có"
            ));
        }

        notificationRepository.save(notification);
    }

    // Lấy danh sách thông báo của user
    public List<Notification> getUserNotifications(Integer userId) {
        return notificationRepository.findByRecipientIdOrderByReadAndSentAt(userId);
    }

    // Đánh dấu đã đọc
    public void markAsRead(Integer notificationId, Integer userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        // Chỉ cho phép user đọc thông báo của mình
        if (!notification.getRecipientId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    // Đánh dấu tất cả đã đọc
    public void markAllAsRead(Integer userId) {
        List<Notification> notifications = notificationRepository
                .findByRecipientIdOrderByReadAndSentAt(userId);

        notifications.stream()
                .filter(n -> !n.getIsRead())
                .forEach(n -> {
                    n.setIsRead(true);
                    n.setReadAt(LocalDateTime.now());
                });

        notificationRepository.saveAll(notifications);
    }

    // Đếm số thông báo chưa đọc
    public Long getUnreadCount(Integer userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    // Xóa thông báo cũ (cleanup job)
    public void deleteOldNotifications(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        notificationRepository.deleteOldNotifications(cutoffDate);
    }
}
