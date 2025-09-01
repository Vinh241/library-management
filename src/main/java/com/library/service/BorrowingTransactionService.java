package com.library.service;

import com.library.entity.*;
import com.library.dto.response.BorrowingTransactionResponse;
import com.library.dto.response.PagedResponse;
import com.library.repository.BorrowingTransactionRepository;
import com.library.repository.BookItemRepository;
import com.library.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class BorrowingTransactionService {

    @Autowired
    private BorrowingTransactionRepository borrowingTransactionRepository;

    @Autowired
    private BookItemRepository bookItemRepository;

    @Autowired
    private BookMovementService bookMovementService;

    @Autowired
    private AccountRepository accountRepository;

    /**
     * Tạo transaction mượn sách từ borrow request đã được approve
     */
    public void createBorrowingTransaction(BorrowRequest borrowRequest, Account librarian) {
        // Validate borrow request đã được approve
        if (!borrowRequest.isApproved()) {
            throw new RuntimeException("Only approved borrow requests can be processed");
        }

        // Kiểm tra xem đã có transaction cho request này chưa
        Optional<BorrowingTransaction> existingTransaction = 
                borrowingTransactionRepository.findByBorrowRequestId(borrowRequest.getId());
        
        if (existingTransaction.isPresent()) {
            throw new RuntimeException("Transaction already exists for this borrow request");
        }

        // Validate book item vẫn available hoặc reserved
        BookItem bookItem = borrowRequest.getBookItem();
        if (!bookItem.isAvailable() && !"RESERVED".equals(bookItem.getStatus())) {
            throw new RuntimeException("Book item is not available for borrowing");
        }

        // Lưu trạng thái cũ để log movement
        String oldStatus = bookItem.getStatus();
        String oldLocation = bookItem.getShelfLocation();

        // Tính toán due date (mặc định là requested return date)
        LocalDate dueDate = borrowRequest.getRequestedReturnDate();
        
        // Tạo transaction
        BorrowingTransaction transaction = new BorrowingTransaction(borrowRequest, librarian, dueDate);
        
        // Cập nhật trạng thái book item
        bookItem.markAsBorrowed();
        bookItemRepository.save(bookItem);

        // Lưu transaction
        transaction = borrowingTransactionRepository.save(transaction);

        // Log movement: sách được mượn
        bookMovementService.logMovement(
            bookItem,
            transaction,
            BookMovement.MovementAction.BORROWED,
            oldStatus,
            bookItem.getStatus(),
            librarian,
            "Sách được mượn bởi " + borrowRequest.getRequester().getFullName()
        );
    }

    /**
     * Trả sách
     */
    public BorrowingTransaction returnBook(Integer transactionId, Account returnedTo, String notes) {
        BorrowingTransaction transaction = borrowingTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + transactionId));

        if (!transaction.isActive()) {
            throw new RuntimeException("Only active transactions can be returned");
        }

        // Lưu trạng thái cũ để log movement
        BookItem bookItem = transaction.getBookItem();
        String oldStatus = bookItem.getStatus();

        // Đánh dấu transaction là đã trả
        transaction.markAsReturned(returnedTo, notes);
        
        // Cập nhật trạng thái book item
        bookItem.markAsReturned(); // Chuyển về AVAILABLE
        bookItemRepository.save(bookItem);

        transaction = borrowingTransactionRepository.save(transaction);

        // Log movement: sách được trả
        bookMovementService.logMovement(
            bookItem,
            transaction,
            BookMovement.MovementAction.RETURNED,
            oldStatus,
            bookItem.getStatus(),
            returnedTo,
            notes != null ? notes : "Sách được trả về thư viện"
        );

        return transaction;
    }

    /**
     * Đánh dấu sách bị mất
     */
    public BorrowingTransaction markAsLost(Integer transactionId, String notes) {
        BorrowingTransaction transaction = borrowingTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + transactionId));

        if (!transaction.isActive()) {
            throw new RuntimeException("Only active transactions can be marked as lost");
        }

        // Lưu trạng thái cũ để log movement
        BookItem bookItem = transaction.getBookItem();
        String oldStatus = bookItem.getStatus();

        // Đánh dấu transaction là lost
        transaction.markAsLost(notes);
        
        // Cập nhật trạng thái book item
        bookItem.markAsLost();
        bookItemRepository.save(bookItem);

        transaction = borrowingTransactionRepository.save(transaction);

        // Log movement: sách bị mất
        Account performer = getCurrentUserOrDefault();
        bookMovementService.logMovement(
            bookItem,
            transaction,
            BookMovement.MovementAction.LOST,
            oldStatus,
            bookItem.getStatus(),
            performer,
            notes != null ? notes : "Sách được đánh dấu là bị mất"
        );

        return transaction;
    }

    /**
     * Tự động đánh dấu các transaction quá hạn
     */
    public void markOverdueTransactions() {
        List<BorrowingTransaction> overdueTransactions = 
                borrowingTransactionRepository.findOverdueTransactions(LocalDate.now());
        
        for (BorrowingTransaction transaction : overdueTransactions) {
            transaction.markAsOverdue();
        }
        
        if (!overdueTransactions.isEmpty()) {
            borrowingTransactionRepository.saveAll(overdueTransactions);
        }
    }

    /**
     * Lấy danh sách transaction sắp đến hạn (trong N ngày tới)
     */
    @Transactional(readOnly = true)
    public List<BorrowingTransaction> getTransactionsDueSoon(int daysAhead) {
        LocalDate currentDate = LocalDate.now();
        LocalDate futureDate = currentDate.plusDays(daysAhead);
        
        return borrowingTransactionRepository.findTransactionsDueSoon(currentDate, futureDate);
    }

    /**
     * Đếm số lượng sách đang mượn của một người
     */
    @Transactional(readOnly = true)
    public long countActiveBorrowings(Integer borrowerId) {
        return borrowingTransactionRepository.countActiveBorrowingsByBorrowerId(borrowerId);
    }

    /**
     * Kiểm tra xem book item có đang được mượn không
     */
    @Transactional(readOnly = true)
    public boolean isBookItemCurrentlyBorrowed(Integer bookItemId) {
        return borrowingTransactionRepository.findActiveTransactionByBookItemId(bookItemId).isPresent();
    }

    /**
     * Lấy transaction đang active của một book item
     */
    @Transactional(readOnly = true)
    public Optional<BorrowingTransaction> getActiveTransactionByBookItem(Integer bookItemId) {
        return borrowingTransactionRepository.findActiveTransactionByBookItemId(bookItemId);
    }

    /**
     * Kiểm tra xem transaction đã tồn tại cho borrow request chưa
     */
    @Transactional(readOnly = true)
    public boolean transactionExistsForBorrowRequest(Integer borrowRequestId) {
        return borrowingTransactionRepository.findByBorrowRequestId(borrowRequestId).isPresent();
    }

    /**
     * Lấy transaction theo borrow request
     */
    @Transactional(readOnly = true)
    public Optional<BorrowingTransaction> getTransactionByBorrowRequest(Integer borrowRequestId) {
        return borrowingTransactionRepository.findByBorrowRequestId(borrowRequestId);
    }

    // ===== GET TRANSACTIONS METHODS =====

    /**
     * Lấy tất cả transactions với phân trang
     */
    @Transactional(readOnly = true)
    public PagedResponse<BorrowingTransactionResponse> getAllTransactions(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<BorrowingTransaction> transactionPage = borrowingTransactionRepository.findAll(pageable);
        
        List<BorrowingTransactionResponse> content = transactionPage.getContent()
                .stream()
                .map(BorrowingTransactionResponse::new)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                content,
                transactionPage.getNumber(),
                transactionPage.getSize(),
                transactionPage.getTotalElements(),
                transactionPage.getTotalPages(),
                transactionPage.isLast()
        );
    }

    /**
     * Lấy transactions theo borrower với phân trang
     */
    @Transactional(readOnly = true)
    public PagedResponse<BorrowingTransactionResponse> getTransactionsByBorrower(Integer borrowerId, int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<BorrowingTransaction> transactionPage = borrowingTransactionRepository.findByBorrowerId(borrowerId, pageable);
        
        List<BorrowingTransactionResponse> content = transactionPage.getContent()
                .stream()
                .map(BorrowingTransactionResponse::new)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                content,
                transactionPage.getNumber(),
                transactionPage.getSize(),
                transactionPage.getTotalElements(),
                transactionPage.getTotalPages(),
                transactionPage.isLast()
        );
    }

    /**
     * Lấy transactions theo trạng thái với phân trang
     */
    @Transactional(readOnly = true)
    public PagedResponse<BorrowingTransactionResponse> getTransactionsByStatus(BorrowingTransaction.TransactionStatus status, int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<BorrowingTransaction> transactionPage = borrowingTransactionRepository.findByStatus(status, pageable);
        
        List<BorrowingTransactionResponse> content = transactionPage.getContent()
                .stream()
                .map(BorrowingTransactionResponse::new)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                content,
                transactionPage.getNumber(),
                transactionPage.getSize(),
                transactionPage.getTotalElements(),
                transactionPage.getTotalPages(),
                transactionPage.isLast()
        );
    }

    /**
     * Lấy transactions theo thư viện với phân trang
     */
    @Transactional(readOnly = true)
    public PagedResponse<BorrowingTransactionResponse> getTransactionsByLibrary(Integer libraryId, int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<BorrowingTransaction> transactionPage = borrowingTransactionRepository.findByLibraryId(libraryId, pageable);
        
        List<BorrowingTransactionResponse> content = transactionPage.getContent()
                .stream()
                .map(BorrowingTransactionResponse::new)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                content,
                transactionPage.getNumber(),
                transactionPage.getSize(),
                transactionPage.getTotalElements(),
                transactionPage.getTotalPages(),
                transactionPage.isLast()
        );
    }

    /**
     * Lấy transactions theo librarian với phân trang
     */
    @Transactional(readOnly = true)
    public PagedResponse<BorrowingTransactionResponse> getTransactionsByLibrarian(Integer librarianId, int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<BorrowingTransaction> transactionPage = borrowingTransactionRepository.findByLibrarianId(librarianId, pageable);
        
        List<BorrowingTransactionResponse> content = transactionPage.getContent()
                .stream()
                .map(BorrowingTransactionResponse::new)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                content,
                transactionPage.getNumber(),
                transactionPage.getSize(),
                transactionPage.getTotalElements(),
                transactionPage.getTotalPages(),
                transactionPage.isLast()
        );
    }

    /**
     * Lấy transaction theo ID
     */
    @Transactional(readOnly = true)
    public BorrowingTransactionResponse getTransactionById(Integer transactionId) {
        BorrowingTransaction transaction = borrowingTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + transactionId));
        
        return new BorrowingTransactionResponse(transaction);
    }

    /**
     * Lấy transactions quá hạn với phân trang
     */
    @Transactional(readOnly = true)
    public List<BorrowingTransactionResponse> getOverdueTransactions() {
        List<BorrowingTransaction> overdueTransactions = borrowingTransactionRepository.findOverdueTransactions(LocalDate.now());
        
        return overdueTransactions.stream()
                .map(BorrowingTransactionResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Lấy transactions sắp đến hạn với response format
     */
    @Transactional(readOnly = true)
    public List<BorrowingTransactionResponse> getTransactionsDueSoonResponse(int daysAhead) {
        LocalDate currentDate = LocalDate.now();
        LocalDate futureDate = currentDate.plusDays(daysAhead);
        
        List<BorrowingTransaction> dueSoonTransactions = borrowingTransactionRepository.findTransactionsDueSoon(currentDate, futureDate);
        
        return dueSoonTransactions.stream()
                .map(BorrowingTransactionResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Lấy thông tin user hiện tại từ Security Context hoặc trả về null
     */
    private Account getCurrentUserOrDefault() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }

            String username = authentication.getName();
            return accountRepository.findByUsername(username).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}
