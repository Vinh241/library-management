package com.library.repository;

import com.library.entity.BookMovement;
import com.library.entity.BookItem;
import com.library.entity.BorrowingTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookMovementRepository extends JpaRepository<BookMovement, Integer> {

    /**
     * Tìm tất cả movements của một book item (có phân trang)
     */
    Page<BookMovement> findByBookItemOrderByPerformedAtDesc(BookItem bookItem, Pageable pageable);

    /**
     * Tìm tất cả movements của một book item (không phân trang)
     */
    List<BookMovement> findByBookItemOrderByPerformedAtDesc(BookItem bookItem);

    /**
     * Tìm movements theo book item ID (có phân trang)
     */
    @Query("SELECT bm FROM BookMovement bm WHERE bm.bookItem.id = :bookItemId ORDER BY bm.performedAt DESC")
    Page<BookMovement> findByBookItemIdOrderByPerformedAtDesc(@Param("bookItemId") Integer bookItemId, Pageable pageable);

    /**
     * Tìm movements theo transaction
     */
    List<BookMovement> findByTransactionOrderByPerformedAtDesc(BorrowingTransaction transaction);

    /**
     * Tìm movements theo transaction ID
     */
    @Query("SELECT bm FROM BookMovement bm WHERE bm.transaction.id = :transactionId ORDER BY bm.performedAt DESC")
    List<BookMovement> findByTransactionIdOrderByPerformedAtDesc(@Param("transactionId") Integer transactionId);

    /**
     * Tìm movements theo action
     */
    Page<BookMovement> findByActionOrderByPerformedAtDesc(BookMovement.MovementAction action, Pageable pageable);

    /**
     * Tìm movements theo người thực hiện
     */
    @Query("SELECT bm FROM BookMovement bm WHERE bm.performedBy.id = :performedById ORDER BY bm.performedAt DESC")
    Page<BookMovement> findByPerformedByIdOrderByPerformedAtDesc(@Param("performedById") Integer performedById, Pageable pageable);

    /**
     * Tìm movements trong khoảng thời gian
     */
    @Query("SELECT bm FROM BookMovement bm WHERE bm.performedAt BETWEEN :startDate AND :endDate ORDER BY bm.performedAt DESC")
    Page<BookMovement> findByPerformedAtBetweenOrderByPerformedAtDesc(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate, 
            Pageable pageable);

    /**
     * Tìm movements với nhiều điều kiện (tìm kiếm nâng cao)
     */
    @Query("SELECT bm FROM BookMovement bm " +
           "WHERE (:bookItemId IS NULL OR bm.bookItem.id = :bookItemId) " +
           "AND (:action IS NULL OR bm.action = :action) " +
           "AND (:performedById IS NULL OR bm.performedBy.id = :performedById) " +
           "AND (:startDate IS NULL OR bm.performedAt >= :startDate) " +
           "AND (:endDate IS NULL OR bm.performedAt <= :endDate) " +
           "ORDER BY bm.performedAt DESC")
    Page<BookMovement> findMovementsWithFilters(
            @Param("bookItemId") Integer bookItemId,
            @Param("action") BookMovement.MovementAction action,
            @Param("performedById") Integer performedById,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Đếm số lượng movements của một book item
     */
    @Query("SELECT COUNT(bm) FROM BookMovement bm WHERE bm.bookItem.id = :bookItemId")
    long countByBookItemId(@Param("bookItemId") Integer bookItemId);

    /**
     * Lấy movement gần nhất của một book item
     */
    @Query("SELECT bm FROM BookMovement bm WHERE bm.bookItem.id = :bookItemId ORDER BY bm.performedAt DESC LIMIT 1")
    BookMovement findLatestByBookItemId(@Param("bookItemId") Integer bookItemId);

    /**
     * Tìm movements của một thư viện (thông qua book items)
     */
    @Query("SELECT bm FROM BookMovement bm " +
           "WHERE bm.bookItem.library.id = :libraryId " +
           "ORDER BY bm.performedAt DESC")
    Page<BookMovement> findByLibraryIdOrderByPerformedAtDesc(@Param("libraryId") Integer libraryId, Pageable pageable);

    /**
     * Thống kê movements theo action trong khoảng thời gian
     */
    @Query("SELECT bm.action, COUNT(bm) FROM BookMovement bm " +
           "WHERE bm.performedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY bm.action")
    List<Object[]> countMovementsByActionInPeriod(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
