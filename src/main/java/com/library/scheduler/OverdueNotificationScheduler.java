package com.library.scheduler;

import com.library.entity.BorrowingTransaction;
import com.library.repository.BorrowingTransactionRepository;
import com.library.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class OverdueNotificationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(OverdueNotificationScheduler.class);

    @Autowired
    private BorrowingTransactionRepository borrowingTransactionRepository;

    @Autowired
    private NotificationService notificationService;

    // Chạy mỗi ngày lúc 9:00 AM để kiểm tra sách quá hạn
    @Scheduled(cron = "0 0 9 * * *")
//    @Scheduled(cron = "*/10 * * * * *")
    public void checkOverdueBooks() {
        logger.info("Starting overdue books check...");

        try {
            LocalDate today = LocalDate.now();

            // Tìm tất cả transactions có due_date < today và status = ACTIVE
            List<BorrowingTransaction> overdueTransactions = borrowingTransactionRepository
                    .findOverdueTransactions(today);

            logger.info("Found {} overdue transactions", overdueTransactions.size());

            for (BorrowingTransaction transaction : overdueTransactions) {
                try {
                    // Tạo thông báo quá hạn
                    notificationService.createOverdueNotification(transaction);
                    logger.info("Created overdue notification for transaction {}", transaction.getId());
                } catch (Exception e) {
                    logger.error("Error creating overdue notification for transaction {}: {}",
                            transaction.getId(), e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.error("Error in overdue books check: {}", e.getMessage(), e);
        }

        logger.info("Completed overdue books check");
    }

    // Chạy mỗi ngày lúc 10:00 AM để gửi reminder (3 ngày trước hạn)
    @Scheduled(cron = "0 0 10 * * *")
    public void sendDueReminders() {
        logger.info("Starting due reminders check...");

        try {
            LocalDate threeDaysLater = LocalDate.now().plusDays(3);

            // Tìm transactions sẽ đến hạn trong 3 ngày
            List<BorrowingTransaction> upcomingDueTransactions = borrowingTransactionRepository
                    .findUpcomingDueTransactions(threeDaysLater);

            logger.info("Found {} upcoming due transactions", upcomingDueTransactions.size());

            for (BorrowingTransaction transaction : upcomingDueTransactions) {
                try {
                    notificationService.createDueReminderNotification(transaction);
                    logger.info("Created due reminder for transaction {}", transaction.getId());
                } catch (Exception e) {
                    logger.error("Error creating due reminder for transaction {}: {}",
                            transaction.getId(), e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.error("Error in due reminders check: {}", e.getMessage(), e);
        }

        logger.info("Completed due reminders check");
    }

    // Chạy mỗi tuần để cleanup thông báo cũ (chủ nhật lúc 2:00 AM)
    @Scheduled(cron = "0 0 2 * * SUN")
    public void cleanupOldNotifications() {
        logger.info("Starting old notifications cleanup...");

        try {
            // Xóa thông báo cũ hơn 90 ngày
            notificationService.deleteOldNotifications(90);
            logger.info("Completed old notifications cleanup");
        } catch (Exception e) {
            logger.error("Error in old notifications cleanup: {}", e.getMessage(), e);
        }
    }
}