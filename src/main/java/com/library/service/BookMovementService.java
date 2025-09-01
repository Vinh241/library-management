package com.library.service;

import com.library.dto.request.BookMovementRequest;
import com.library.dto.request.UpdateBookMovementRequest;
import com.library.dto.response.BookMovementResponse;
import com.library.dto.response.PagedResponse;
import com.library.entity.*;
import com.library.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookMovementService {

    @Autowired
    private BookMovementRepository bookMovementRepository;

    @Autowired
    private BookItemRepository bookItemRepository;

    @Autowired
    private BorrowingTransactionRepository borrowingTransactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    /**
     * Tạo movement mới (thủ công)
     */
    public BookMovementResponse createMovement(BookMovementRequest request) {
        // Lấy thông tin người thực hiện từ Security Context
        Account performedBy = getCurrentUser();

        // Tìm book item
        BookItem bookItem = bookItemRepository.findById(request.getBookItemId())
                .orElseThrow(() -> new RuntimeException("Book item not found: " + request.getBookItemId()));

        // Tìm transaction nếu có
        BorrowingTransaction transaction = null;
        if (request.getTransactionId() != null) {
            transaction = borrowingTransactionRepository.findById(request.getTransactionId())
                    .orElseThrow(() -> new RuntimeException("Transaction not found: " + request.getTransactionId()));
        }

        // Tạo movement
        BookMovement movement = new BookMovement();
        movement.setBookItem(bookItem);
        movement.setTransaction(transaction);
        movement.setAction(BookMovement.MovementAction.valueOf(request.getAction()));
        movement.setOldStatus(request.getOldStatus());
        movement.setNewStatus(request.getNewStatus());
        movement.setOldLocation(request.getOldLocation());
        movement.setNewLocation(request.getNewLocation());
        movement.setPerformedBy(performedBy);
        movement.setNotes(request.getNotes());

        movement = bookMovementRepository.save(movement);

        return BookMovementResponse.fromEntity(movement);
    }

    /**
     * Log movement tự động (được gọi từ các service khác)
     */
    public void logMovement(BookItem bookItem, BookMovement.MovementAction action, 
                           String oldStatus, String newStatus, Account performedBy, String notes) {
        logMovement(bookItem, null, action, oldStatus, newStatus, null, null, performedBy, notes);
    }

    /**
     * Log movement với transaction
     */
    public void logMovement(BookItem bookItem, BorrowingTransaction transaction, 
                           BookMovement.MovementAction action, String oldStatus, String newStatus, 
                           Account performedBy, String notes) {
        logMovement(bookItem, transaction, action, oldStatus, newStatus, null, null, performedBy, notes);
    }

    /**
     * Log movement đầy đủ
     */
    public void logMovement(BookItem bookItem, BorrowingTransaction transaction, 
                           BookMovement.MovementAction action, String oldStatus, String newStatus,
                           String oldLocation, String newLocation, Account performedBy, String notes) {
        BookMovement movement = new BookMovement();
        movement.setBookItem(bookItem);
        movement.setTransaction(transaction);
        movement.setAction(action);
        movement.setOldStatus(oldStatus);
        movement.setNewStatus(newStatus);
        movement.setOldLocation(oldLocation);
        movement.setNewLocation(newLocation);
        movement.setPerformedBy(performedBy);
        movement.setNotes(notes);

        bookMovementRepository.save(movement);
    }

    /**
     * Lấy tất cả movements với phân trang
     */
    public PagedResponse<BookMovementResponse> getAllMovements(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<BookMovement> movementsPage = bookMovementRepository.findAll(pageable);

        List<BookMovementResponse> movements = movementsPage.getContent().stream()
                .map(BookMovementResponse::fromEntity)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                movements,
                movementsPage.getNumber(),
                movementsPage.getSize(),
                movementsPage.getTotalElements(),
                movementsPage.getTotalPages(),
                movementsPage.isLast()
        );
    }

    /**
     * Lấy movements của một book item
     */
    public PagedResponse<BookMovementResponse> getMovementsByBookItem(Integer bookItemId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("performedAt").descending());

        Page<BookMovement> movementsPage = bookMovementRepository.findByBookItemIdOrderByPerformedAtDesc(bookItemId, pageable);

        List<BookMovementResponse> movements = movementsPage.getContent().stream()
                .map(BookMovementResponse::fromEntity)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                movements,
                movementsPage.getNumber(),
                movementsPage.getSize(),
                movementsPage.getTotalElements(),
                movementsPage.getTotalPages(),
                movementsPage.isLast()
        );
    }

    /**
     * Lấy movements theo action
     */
    public PagedResponse<BookMovementResponse> getMovementsByAction(String action, int page, int size) {
        BookMovement.MovementAction movementAction = BookMovement.MovementAction.valueOf(action);
        Pageable pageable = PageRequest.of(page, size, Sort.by("performedAt").descending());

        Page<BookMovement> movementsPage = bookMovementRepository.findByActionOrderByPerformedAtDesc(movementAction, pageable);

        List<BookMovementResponse> movements = movementsPage.getContent().stream()
                .map(BookMovementResponse::fromEntity)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                movements,
                movementsPage.getNumber(),
                movementsPage.getSize(),
                movementsPage.getTotalElements(),
                movementsPage.getTotalPages(),
                movementsPage.isLast()
        );
    }

    /**
     * Lấy movements theo người thực hiện
     */
    public PagedResponse<BookMovementResponse> getMovementsByPerformer(Integer performerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("performedAt").descending());

        Page<BookMovement> movementsPage = bookMovementRepository.findByPerformedByIdOrderByPerformedAtDesc(performerId, pageable);

        List<BookMovementResponse> movements = movementsPage.getContent().stream()
                .map(BookMovementResponse::fromEntity)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                movements,
                movementsPage.getNumber(),
                movementsPage.getSize(),
                movementsPage.getTotalElements(),
                movementsPage.getTotalPages(),
                movementsPage.isLast()
        );
    }

    /**
     * Lấy movements theo thư viện
     */
    public PagedResponse<BookMovementResponse> getMovementsByLibrary(Integer libraryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("performedAt").descending());

        Page<BookMovement> movementsPage = bookMovementRepository.findByLibraryIdOrderByPerformedAtDesc(libraryId, pageable);

        List<BookMovementResponse> movements = movementsPage.getContent().stream()
                .map(BookMovementResponse::fromEntity)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                movements,
                movementsPage.getNumber(),
                movementsPage.getSize(),
                movementsPage.getTotalElements(),
                movementsPage.getTotalPages(),
                movementsPage.isLast()
        );
    }

    /**
     * Tìm kiếm movements với nhiều điều kiện
     */
    public PagedResponse<BookMovementResponse> searchMovements(
            Integer bookItemId, String action, Integer performerId,
            LocalDateTime startDate, LocalDateTime endDate,
            int page, int size) {
        
        BookMovement.MovementAction movementAction = action != null ? BookMovement.MovementAction.valueOf(action) : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by("performedAt").descending());

        Page<BookMovement> movementsPage = bookMovementRepository.findMovementsWithFilters(
                bookItemId, movementAction, performerId, startDate, endDate, pageable);

        List<BookMovementResponse> movements = movementsPage.getContent().stream()
                .map(BookMovementResponse::fromEntity)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                movements,
                movementsPage.getNumber(),
                movementsPage.getSize(),
                movementsPage.getTotalElements(),
                movementsPage.getTotalPages(),
                movementsPage.isLast()
        );
    }

    /**
     * Lấy movement theo ID
     */
    public BookMovementResponse getMovementById(Integer id) {
        BookMovement movement = bookMovementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movement not found: " + id));

        return BookMovementResponse.fromEntity(movement);
    }

    /**
     * Cập nhật movement
     */
    public BookMovementResponse updateMovement(Integer id, UpdateBookMovementRequest request) {
        // Tìm movement cần cập nhật
        BookMovement movement = bookMovementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movement not found: " + id));

        // Lấy thông tin người thực hiện từ Security Context
        Account currentUser = getCurrentUser();

        // Kiểm tra quyền cập nhật (chỉ người tạo hoặc admin/librarian/province_manager có thể cập nhật)
        if (!canUpdateMovement(movement, currentUser)) {
            throw new RuntimeException("You don't have permission to update this movement");
        }

        // Tìm transaction nếu có trong request
        if (request.getTransactionId() != null && !request.getTransactionId().equals(
                movement.getTransaction() != null ? movement.getTransaction().getId() : null)) {
            BorrowingTransaction transaction = borrowingTransactionRepository.findById(request.getTransactionId())
                    .orElseThrow(() -> new RuntimeException("Transaction not found: " + request.getTransactionId()));
            movement.setTransaction(transaction);
        }

        // Cập nhật các field nếu có trong request
        if (request.getOldStatus() != null) {
            movement.setOldStatus(request.getOldStatus());
        }
        
        if (request.getNewStatus() != null) {
            movement.setNewStatus(request.getNewStatus());
        }
        
        if (request.getOldLocation() != null) {
            movement.setOldLocation(request.getOldLocation());
        }
        
        if (request.getNewLocation() != null) {
            movement.setNewLocation(request.getNewLocation());
        }
        
        if (request.getNotes() != null) {
            movement.setNotes(request.getNotes());
        }

        // Lưu movement đã cập nhật
        movement = bookMovementRepository.save(movement);

        return BookMovementResponse.fromEntity(movement);
    }

    /**
     * Xóa movement (chỉ admin hoặc người tạo có thể xóa)
     */
    public void deleteMovement(Integer id) {
        BookMovement movement = bookMovementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movement not found: " + id));

        Account currentUser = getCurrentUser();

        // Kiểm tra quyền xóa (chỉ admin hoặc người tạo có thể xóa)
        if (!canDeleteMovement(movement, currentUser)) {
            throw new RuntimeException("You don't have permission to delete this movement");
        }

        bookMovementRepository.delete(movement);
    }

    /**
     * Đếm số lượng movements của một book item
     */
    public long countMovementsByBookItem(Integer bookItemId) {
        return bookMovementRepository.countByBookItemId(bookItemId);
    }

    /**
     * Lấy movement gần nhất của một book item
     */
    public Optional<BookMovementResponse> getLatestMovementByBookItem(Integer bookItemId) {
        BookMovement movement = bookMovementRepository.findLatestByBookItemId(bookItemId);
        return movement != null ? Optional.of(BookMovementResponse.fromEntity(movement)) : Optional.empty();
    }

    /**
     * Thống kê movements theo action trong khoảng thời gian
     */
    public List<Object[]> getMovementStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        return bookMovementRepository.countMovementsByActionInPeriod(startDate, endDate);
    }

    /**
     * Lấy thông tin user hiện tại từ Security Context
     */
    private Account getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        String username = authentication.getName();
        return accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    /**
     * Kiểm tra quyền cập nhật movement
     */
    private boolean canUpdateMovement(BookMovement movement, Account currentUser) {
        // Admin có thể cập nhật tất cả
        if (currentUser.getRole() == Account.Role.ADMIN) {
            return true;
        }

        // Province manager có thể cập nhật movements trong tỉnh của mình
        if (currentUser.getRole() == Account.Role.PROVINCE_MANAGER) {
            return movement.getBookItem().getLibrary().getProvince().getId().equals(currentUser.getProvinceId());
        }

        // Librarian có thể cập nhật movements trong thư viện của mình
        if (currentUser.getRole() == Account.Role.LIBRARIAN) {
            return movement.getBookItem().getLibrary().getId().equals(currentUser.getLibraryId());
        }

        // Người tạo có thể cập nhật movement của mình
        return movement.getPerformedBy().getId().equals(currentUser.getId());
    }

    /**
     * Kiểm tra quyền xóa movement
     */
    private boolean canDeleteMovement(BookMovement movement, Account currentUser) {
        // Chỉ admin hoặc người tạo có thể xóa
        return currentUser.getRole() == Account.Role.ADMIN || 
               movement.getPerformedBy().getId().equals(currentUser.getId());
    }
}
