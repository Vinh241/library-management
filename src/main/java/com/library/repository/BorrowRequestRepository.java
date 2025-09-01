package com.library.repository;

import com.library.entity.BorrowRequest;
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
public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Integer> {

    /**
     * Tìm tất cả yêu cầu mượn sách theo người yêu cầu
     */
    Page<BorrowRequest> findByRequesterId(Integer requesterId, Pageable pageable);

    /**
     * Tìm tất cả yêu cầu mượn sách theo trạng thái
     */
    Page<BorrowRequest> findByStatus(BorrowRequest.BorrowRequestStatus status, Pageable pageable);

    /**
     * Tìm tất cả yêu cầu mượn sách theo người yêu cầu và trạng thái
     */
    Page<BorrowRequest> findByRequesterIdAndStatus(Integer requesterId, 
                                                  BorrowRequest.BorrowRequestStatus status, 
                                                  Pageable pageable);

    /**
     * Tìm tất cả yêu cầu mượn sách theo thư viện (thông qua book item)
     */
    @Query("SELECT br FROM BorrowRequest br " +
           "JOIN br.bookItem bi " +
           "WHERE bi.library.id = :libraryId")
    Page<BorrowRequest> findByLibraryId(@Param("libraryId") Integer libraryId, Pageable pageable);

    /**
     * Tìm tất cả yêu cầu mượn sách theo thư viện và trạng thái
     */
    @Query("SELECT br FROM BorrowRequest br " +
           "JOIN br.bookItem bi " +
           "WHERE bi.library.id = :libraryId AND br.status = :status")
    Page<BorrowRequest> findByLibraryIdAndStatus(@Param("libraryId") Integer libraryId, 
                                               @Param("status") BorrowRequest.BorrowRequestStatus status, 
                                               Pageable pageable);

    /**
     * Tìm yêu cầu mượn sách theo book item và người yêu cầu
     */
    Optional<BorrowRequest> findByBookItemIdAndRequesterId(Integer bookItemId, Integer requesterId);

    /**
     * Kiểm tra xem có yêu cầu mượn sách đang PENDING cho book item và người yêu cầu hay không
     */
    boolean existsByBookItemIdAndRequesterIdAndStatus(Integer bookItemId,
                                                      Integer requesterId,
                                                      BorrowRequest.BorrowRequestStatus status);

    /**
     * Tìm tất cả yêu cầu mượn sách đang pending cho một book item
     */
    List<BorrowRequest> findByBookItemIdAndStatus(Integer bookItemId, BorrowRequest.BorrowRequestStatus status);

    /**
     * Tìm tất cả yêu cầu mượn sách đã hết hạn (quá ngày yêu cầu mượn mà vẫn pending)
     */
    @Query("SELECT br FROM BorrowRequest br " +
           "WHERE br.status = 'PENDING' AND br.requestedBorrowDate < :currentDate")
    List<BorrowRequest> findExpiredPendingRequests(@Param("currentDate") LocalDate currentDate);

    /**
     * Đếm số lượng yêu cầu mượn sách theo trạng thái của một người dùng
     */
    @Query("SELECT COUNT(br) FROM BorrowRequest br " +
           "WHERE br.requester.id = :requesterId AND br.status = :status")
    long countByRequesterIdAndStatus(@Param("requesterId") Integer requesterId, 
                                   @Param("status") BorrowRequest.BorrowRequestStatus status);

    /**
     * Tìm tất cả yêu cầu mượn sách pending trong một khoảng thời gian
     */
    @Query("SELECT br FROM BorrowRequest br " +
           "WHERE br.status = 'PENDING' " +
           "AND br.requestedBorrowDate BETWEEN :startDate AND :endDate")
    List<BorrowRequest> findPendingRequestsInDateRange(@Param("startDate") LocalDate startDate, 
                                                      @Param("endDate") LocalDate endDate);

    /**
     * Tìm tất cả yêu cầu mượn sách được tạo trong ngày hôm nay
     */
    @Query("SELECT br FROM BorrowRequest br " +
           "WHERE DATE(br.createdAt) = :date")
    List<BorrowRequest> findRequestsCreatedOnDate(@Param("date") LocalDate date);

    /**
     * Kiểm tra xem người dùng có yêu cầu mượn sách nào đang pending không
     */
    @Query("SELECT COUNT(br) > 0 FROM BorrowRequest br " +
           "WHERE br.requester.id = :requesterId AND br.status = 'PENDING'")
    boolean hasActivePendingRequest(@Param("requesterId") Integer requesterId);

    /**
     * Tìm tất cả yêu cầu mượn sách cần được review (pending và trong thời hạn)
     */
    @Query("SELECT br FROM BorrowRequest br " +
           "JOIN br.bookItem bi " +
           "WHERE br.status = 'PENDING' " +
           "AND br.requestedBorrowDate >= :currentDate " +
           "AND bi.library.id = :libraryId " +
           "ORDER BY br.createdAt ASC")
    Page<BorrowRequest> findPendingRequestsForReview(@Param("libraryId") Integer libraryId, 
                                                   @Param("currentDate") LocalDate currentDate, 
                                                   Pageable pageable);

    /**
     * Tìm tất cả yêu cầu mượn sách được phê duyệt nhưng chưa được thực hiện
     */
    @Query("SELECT br FROM BorrowRequest br " +
           "WHERE br.status = 'APPROVED' " +
           "AND br.id NOT IN (SELECT bt.borrowRequest.id FROM BorrowingTransaction bt)")
    List<BorrowRequest> findApprovedRequestsNotProcessed();
}
