package com.library.service;

import com.library.dto.request.BorrowRequestRequest;
import com.library.dto.request.ReviewBorrowRequestRequest;
import com.library.dto.response.BorrowRequestResponse;
import com.library.dto.response.PagedResponse;
import com.library.entity.*;
import com.library.repository.BorrowRequestRepository;
import com.library.repository.BookItemRepository;
import com.library.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class BorrowRequestService {

    @Autowired
    private BorrowRequestRepository borrowRequestRepository;

    @Autowired
    private BookItemRepository bookItemRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BorrowingTransactionService borrowingTransactionService;

    // === CREATE BORROW REQUEST ===

    /**
     * Tạo yêu cầu mượn sách mới (đã xử lý race condition)
     */
    public BorrowRequestResponse createBorrowRequest(BorrowRequestRequest request, Integer requesterId) {
        // Sử dụng pessimistic lock để tránh race condition khi nhiều người cùng mượn 1 sách
        BookItem bookItem = bookItemRepository.findByIdWithLock(request.getBookItemId())
                .orElseThrow(() -> new RuntimeException("Book item not found with id: " + request.getBookItemId()));

        if (!bookItem.isAvailable()) {
            throw new RuntimeException("Book item is not available for borrowing");
        }

        // Validate requester exists
        Account requester = accountRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester not found with id: " + requesterId));

        // Check if user already has a PENDING request for this book item (ignore approved/processed)
        boolean hasPendingRequest = borrowRequestRepository
                .existsByBookItemIdAndRequesterIdAndStatus(
                        request.getBookItemId(),
                        requesterId,
                        BorrowRequest.BorrowRequestStatus.PENDING
                );

        if (hasPendingRequest) {
            throw new RuntimeException("You already have a pending request for this book item");
        }

        // Validate dates
        if (request.getRequestedBorrowDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Requested borrow date cannot be in the past");
        }

        if (request.getRequestedReturnDate().isBefore(request.getRequestedBorrowDate())) {
            throw new RuntimeException("Requested return date must be after borrow date");
        }

        // Check if user has too many active pending requests
        long pendingRequestCount = borrowRequestRepository.countByRequesterIdAndStatus(
                requesterId, BorrowRequest.BorrowRequestStatus.PENDING);
        
        if (pendingRequestCount >= 5) { // Maximum 5 pending requests per user
            throw new RuntimeException("You have reached the maximum number of pending requests (5)");
        }

        // Create new borrow request
        BorrowRequest borrowRequest = new BorrowRequest(
                bookItem, 
                requester, 
                request.getRequestedBorrowDate(),
                request.getRequestedReturnDate(),
                request.getRequestReason()
        );

        // Check if auto-approval is needed based on user role and library/province permissions
        boolean shouldAutoApprove = shouldAutoApproveBorrowRequest(requester, bookItem);
        
        if (shouldAutoApprove) {
            // Auto-approve and create transaction immediately
            borrowRequest.approve(requester, "Auto-approved based on role and library/province permissions");
            
            // Save the approved request first
            BorrowRequest savedRequest = borrowRequestRepository.save(borrowRequest);
            
            // Create borrowing transaction immediately
            try {
                borrowingTransactionService.createBorrowingTransaction(savedRequest, requester);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create borrowing transaction: " + e.getMessage());
            }
            
            return new BorrowRequestResponse(savedRequest);
        } else {
            // Normal flow for READER - reserve the book item temporarily
            bookItem.markAsReserved();
            bookItemRepository.save(bookItem);

            BorrowRequest savedRequest = borrowRequestRepository.save(borrowRequest);
            return new BorrowRequestResponse(savedRequest);
        }
    }

    // === REVIEW BORROW REQUEST ===

    /**
     * Phê duyệt hoặc từ chối yêu cầu mượn sách
     */
    public BorrowRequestResponse reviewBorrowRequest(Integer requestId, ReviewBorrowRequestRequest review, Integer reviewerId) {
        BorrowRequest borrowRequest = borrowRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Borrow request not found with id: " + requestId));

        if (!borrowRequest.isPending()) {
            throw new RuntimeException("Only pending requests can be reviewed");
        }

        Account reviewer = accountRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("Reviewer not found with id: " + reviewerId));

        // Check if reviewer has permission (should be LIBRARIAN or higher)
        if (reviewer.getRole() != Account.Role.LIBRARIAN && 
            reviewer.getRole() != Account.Role.PROVINCE_MANAGER && 
            reviewer.getRole() != Account.Role.ADMIN) {
            throw new RuntimeException("You don't have permission to review borrow requests");
        }

        BookItem bookItem = borrowRequest.getBookItem();

        //check xem có được accept không
        boolean checkPermissionApprove = shouldAutoApproveBorrowRequest(reviewer, bookItem);
        if(!checkPermissionApprove){
            throw new RuntimeException("You don't have permission to review borrow requests");
        }
        if (review.getApproved()) {

            // Approve the request
            borrowRequest.approve(reviewer, review.getReviewNotes());
            
            // Tạo borrowing transaction ngay lập tức
            try {
                borrowingTransactionService.createBorrowingTransaction(borrowRequest, reviewer);
            } catch (Exception e) {
                // Nếu tạo transaction thất bại, rollback approval
                throw new RuntimeException("Failed to create borrowing transaction: " + e.getMessage());
            }
        } else {
            // Reject the request
            if (review.getRejectionReason() == null || review.getRejectionReason().trim().isEmpty()) {
                throw new RuntimeException("Rejection reason is required when rejecting a request");
            }
            borrowRequest.reject(reviewer, review.getRejectionReason(), review.getReviewNotes());
            
            // Make book item available again
            bookItem.markAsReturned(); // This sets status to AVAILABLE
            bookItemRepository.save(bookItem);
        }

        BorrowRequest savedRequest = borrowRequestRepository.save(borrowRequest);
        return new BorrowRequestResponse(savedRequest);
    }

    // === CANCEL BORROW REQUEST ===

    /**
     * Hủy yêu cầu mượn sách (chỉ người tạo yêu cầu mới có thể hủy)
     */
    public BorrowRequestResponse cancelBorrowRequest(Integer requestId, Integer requesterId) {
        BorrowRequest borrowRequest = borrowRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Borrow request not found with id: " + requestId));

        if (!borrowRequest.getRequester().getId().equals(requesterId)) {
            throw new RuntimeException("You can only cancel your own requests");
        }

        if (!borrowRequest.isPending()) {
            throw new RuntimeException("Only pending requests can be cancelled");
        }

        borrowRequest.cancel();
        
        // Make book item available again
        BookItem bookItem = borrowRequest.getBookItem();
        bookItem.markAsReturned(); // This sets status to AVAILABLE
        bookItemRepository.save(bookItem);

        BorrowRequest savedRequest = borrowRequestRepository.save(borrowRequest);
        return new BorrowRequestResponse(savedRequest);
    }

    // === GET BORROW REQUESTS ===

    /**
     * Lấy danh sách yêu cầu mượn sách của người dùng
     */
    @Transactional(readOnly = true)
    public PagedResponse<BorrowRequestResponse> getUserBorrowRequests(Integer userId, String status, 
                                                                    int page, int size) {
        page = Math.max(page, 0);
        size = size <= 0 ? 10 : Math.min(size, 100);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<BorrowRequest> requestPage;

        if (status != null && !status.trim().isEmpty()) {
            try {
                BorrowRequest.BorrowRequestStatus requestStatus = BorrowRequest.BorrowRequestStatus.valueOf(status.toUpperCase());
                requestPage = borrowRequestRepository.findByRequesterIdAndStatus(userId, requestStatus, pageable);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status: " + status);
            }
        } else {
            requestPage = borrowRequestRepository.findByRequesterId(userId, pageable);
        }

        return createPagedResponse(requestPage);
    }

    /**
     * Lấy danh sách yêu cầu mượn sách theo thư viện (cho thủ thư)
     */
    @Transactional(readOnly = true)
    public PagedResponse<BorrowRequestResponse> getLibraryBorrowRequests(Integer libraryId, String status, 
                                                                       int page, int size) {
        page = Math.max(page, 0);
        size = size <= 0 ? 10 : Math.min(size, 100);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<BorrowRequest> requestPage;

        if (status != null && !status.trim().isEmpty()) {
            try {
                BorrowRequest.BorrowRequestStatus requestStatus = BorrowRequest.BorrowRequestStatus.valueOf(status.toUpperCase());
                requestPage = borrowRequestRepository.findByLibraryIdAndStatus(libraryId, requestStatus, pageable);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status: " + status);
            }
        } else {
            requestPage = borrowRequestRepository.findByLibraryId(libraryId, pageable);
        }

        return createPagedResponse(requestPage);
    }

    /**
     * Lấy danh sách yêu cầu mượn sách cần được review
     */
    @Transactional(readOnly = true)
    public PagedResponse<BorrowRequestResponse> getPendingRequestsForReview(Integer libraryId, 
                                                                          int page, int size) {
        page = Math.max(page, 0);
        size = size <= 0 ? 10 : Math.min(size, 100);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BorrowRequest> requestPage = borrowRequestRepository.findPendingRequestsForReview(
                libraryId, LocalDate.now(), pageable);

        return createPagedResponse(requestPage);
    }

    /**
     * Lấy chi tiết một yêu cầu mượn sách
     */
    @Transactional(readOnly = true)
    public BorrowRequestResponse getBorrowRequestById(Integer requestId) {
        BorrowRequest borrowRequest = borrowRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Borrow request not found with id: " + requestId));

        return new BorrowRequestResponse(borrowRequest);
    }

    // === UTILITY METHODS ===

    /**
     * Kiểm tra xem có nên tự động phê duyệt yêu cầu mượn sách không
     */
    private boolean shouldAutoApproveBorrowRequest(Account requester, BookItem bookItem) {
        Account.Role role = requester.getRole();
        
        switch (role) {
            case ADMIN:
                // Admin có thể mượn bất kỳ sách nào
                return true;
                
            case PROVINCE_MANAGER:
                // Province Manager chỉ có thể mượn sách từ thư viện trong tỉnh mình quản lý
                return isBookItemInRequesterProvince(requester, bookItem);
                
            case LIBRARIAN:
                // Librarian chỉ có thể mượn sách từ thư viện mình làm việc
                return isBookItemInRequesterLibrary(requester, bookItem);
                
            case READER:
            default:
                // Reader cần phải qua quy trình phê duyệt thông thường
                return false;
        }
    }
    
    /**
     * Kiểm tra xem book item có thuộc thư viện mà librarian đang làm việc không
     */
    private boolean isBookItemInRequesterLibrary(Account requester, BookItem bookItem) {
        if (requester.getLibraryId() == null) {
            return false;
        }
        
        return requester.getLibraryId().equals(bookItem.getLibrary().getId());
    }
    
    /**
     * Kiểm tra xem book item có thuộc tỉnh mà province manager đang quản lý không
     */
    private boolean isBookItemInRequesterProvince(Account requester, BookItem bookItem) {
        if (requester.getProvinceId() == null) {
            return false;
        }
        
        // Book item thuộc library, library thuộc province
        Integer bookItemProvinceId = bookItem.getLibrary().getProvince().getId();
        return requester.getProvinceId().equals(bookItemProvinceId);
    }

    /**
     * Đánh dấu các yêu cầu mượn sách đã hết hạn
     */
    @Transactional
    public void markExpiredRequests() {
        List<BorrowRequest> expiredRequests = borrowRequestRepository.findExpiredPendingRequests(LocalDate.now());
        
        for (BorrowRequest request : expiredRequests) {
            request.markAsExpired();
            
            // Make book item available again
            BookItem bookItem = request.getBookItem();
            if ("RESERVED".equals(bookItem.getStatus())) {
                bookItem.markAsReturned(); // This sets status to AVAILABLE
                bookItemRepository.save(bookItem);
            }
        }
        
        if (!expiredRequests.isEmpty()) {
            borrowRequestRepository.saveAll(expiredRequests);
        }
    }

    /**
     * Kiểm tra xem người dùng có yêu cầu mượn sách nào đang pending không
     */
    @Transactional(readOnly = true)
    public boolean hasActivePendingRequest(Integer userId) {
        return borrowRequestRepository.hasActivePendingRequest(userId);
    }

    /**
     * Tạo PagedResponse từ Page<BorrowRequest>
     */
    private PagedResponse<BorrowRequestResponse> createPagedResponse(Page<BorrowRequest> requestPage) {
        List<BorrowRequestResponse> responseList = requestPage.getContent()
                .stream()
                .map(BorrowRequestResponse::new)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                responseList,
                requestPage.getNumber(),
                requestPage.getSize(),
                requestPage.getTotalElements(),
                requestPage.getTotalPages(),
                requestPage.isFirst(),
                requestPage.isLast()
        );
    }
}
