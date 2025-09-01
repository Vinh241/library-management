package com.library.repository;

import com.library.entity.BorrowingTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowingTransactionRepository extends JpaRepository<BorrowingTransaction, Integer> {

    /**
     * Tìm transaction theo borrow request ID
     */
    Optional<BorrowingTransaction> findByBorrowRequestId(Integer borrowRequestId);

    /**
     * Tìm transaction với eager loading cho notifications
     */
    @Query("SELECT bt FROM BorrowingTransaction bt " +
           "JOIN FETCH bt.bookItem bi " +
           "JOIN FETCH bi.bookTitle " +
           "JOIN FETCH bt.borrower " +
           "WHERE bt.id = :transactionId")
    Optional<BorrowingTransaction> findByIdWithDetails(@Param("transactionId") Integer transactionId);

    /**
     * Tìm tất cả transaction theo borrower
     */
    Page<BorrowingTransaction> findByBorrowerId(Integer borrowerId, Pageable pageable);

    /**
     * Tìm tất cả transaction theo trạng thái
     */
    Page<BorrowingTransaction> findByStatus(BorrowingTransaction.TransactionStatus status, Pageable pageable);

    /**
     * Tìm tất cả transaction theo borrower và trạng thái
     */
    Page<BorrowingTransaction> findByBorrowerIdAndStatus(Integer borrowerId, 
                                                        BorrowingTransaction.TransactionStatus status, 
                                                        Pageable pageable);

    /**
     * Tìm tất cả transaction theo thư viện
     */
    @Query("SELECT bt FROM BorrowingTransaction bt " +
           "JOIN bt.bookItem bi " +
           "WHERE bi.library.id = :libraryId")
    Page<BorrowingTransaction> findByLibraryId(@Param("libraryId") Integer libraryId, Pageable pageable);

    /**
     * Tìm tất cả transaction theo thư viện và trạng thái
     */
    @Query("SELECT bt FROM BorrowingTransaction bt " +
           "JOIN bt.bookItem bi " +
           "WHERE bi.library.id = :libraryId AND bt.status = :status")
    Page<BorrowingTransaction> findByLibraryIdAndStatus(@Param("libraryId") Integer libraryId, 
                                                       @Param("status") BorrowingTransaction.TransactionStatus status, 
                                                       Pageable pageable);



    /**
     * Tìm tất cả transaction sắp đến hạn (trong N ngày tới)
     */
    @Query("SELECT bt FROM BorrowingTransaction bt " +
           "JOIN FETCH bt.bookItem bi " +
           "JOIN FETCH bi.bookTitle " +
           "JOIN FETCH bt.borrower " +
           "WHERE bt.status = 'ACTIVE' " +
           "AND bt.dueDate BETWEEN :currentDate AND :futureDate")
    List<BorrowingTransaction> findTransactionsDueSoon(@Param("currentDate") LocalDate currentDate, 
                                                      @Param("futureDate") LocalDate futureDate);

    /**
     * Đếm số lượng sách đang mượn của một người
     */
    @Query("SELECT COUNT(bt) FROM BorrowingTransaction bt " +
           "WHERE bt.borrower.id = :borrowerId AND bt.status = 'ACTIVE'")
    long countActiveBorrowingsByBorrowerId(@Param("borrowerId") Integer borrowerId);

    /**
     * Tìm tất cả transaction active của một book item
     */
    List<BorrowingTransaction> findByBookItemIdAndStatus(Integer bookItemId, 
                                                        BorrowingTransaction.TransactionStatus status);

    /**
     * Tìm transaction đang active của một book item (chỉ có 1)
     */
    @Query("SELECT bt FROM BorrowingTransaction bt " +
           "WHERE bt.bookItem.id = :bookItemId AND bt.status = 'ACTIVE'")
    Optional<BorrowingTransaction> findActiveTransactionByBookItemId(@Param("bookItemId") Integer bookItemId);

    /**
     * Tìm tất cả transaction trong khoảng thời gian
     */
    @Query("SELECT bt FROM BorrowingTransaction bt " +
           "WHERE bt.borrowDate BETWEEN :startDate AND :endDate")
    List<BorrowingTransaction> findTransactionsInDateRange(@Param("startDate") LocalDate startDate, 
                                                          @Param("endDate") LocalDate endDate);

    /**
     * Thống kê số lượng transaction theo trạng thái
     */
    @Query("SELECT bt.status, COUNT(bt) FROM BorrowingTransaction bt GROUP BY bt.status")
    List<Object[]> getTransactionCountByStatus();

    /**
     * Tìm transaction theo librarian
     */
    Page<BorrowingTransaction> findByLibrarianId(Integer librarianId, Pageable pageable);
    List<BorrowingTransaction> findByBorrowerIdAndStatus(Integer borrowerId, BorrowingTransaction.TransactionStatus status);


    // Tìm transactions quá hạn
    @Query("SELECT bt FROM BorrowingTransaction bt " +
           "JOIN FETCH bt.bookItem bi " +
           "JOIN FETCH bi.bookTitle " +
           "JOIN FETCH bt.borrower " +
           "WHERE bt.dueDate < :today AND bt.status = 'ACTIVE'")
    List<BorrowingTransaction> findOverdueTransactions(@Param("today") LocalDate today);

    // Tìm transactions sắp đến hạn (trong N ngày tới)
    @Query("SELECT bt FROM BorrowingTransaction bt " +
           "JOIN FETCH bt.bookItem bi " +
           "JOIN FETCH bi.bookTitle " +
           "JOIN FETCH bt.borrower " +
           "WHERE bt.dueDate = :dueDate AND bt.status = 'ACTIVE'")
    List<BorrowingTransaction> findUpcomingDueTransactions(@Param("dueDate") LocalDate dueDate);

    // Tìm transactions sắp đến hạn trong khoảng thời gian
    @Query("SELECT bt FROM BorrowingTransaction bt " +
           "JOIN FETCH bt.bookItem bi " +
           "JOIN FETCH bi.bookTitle " +
           "JOIN FETCH bt.borrower " +
           "WHERE bt.dueDate BETWEEN :startDate AND :endDate AND bt.status = 'ACTIVE'")
    List<BorrowingTransaction> findTransactionsDueInRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Thống kê các transaction overdue theo thư viện
    @Query("SELECT bt FROM BorrowingTransaction bt JOIN bt.bookItem bi WHERE bi.library.id = :libraryId AND bt.status = 'OVERDUE'")
    List<BorrowingTransaction> findOverdueByLibrary(@Param("libraryId") Integer libraryId);
}
