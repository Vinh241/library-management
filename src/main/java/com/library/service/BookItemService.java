// File: src/main/java/com/library/service/BookItemService.java
package com.library.service;

import com.library.dto.request.BookItemRequest;
import com.library.dto.response.BookItemResponse;
import com.library.dto.response.PagedResponse;
import com.library.entity.BookItem;
import com.library.entity.BookTitle;
import com.library.entity.Library;
import com.library.entity.BookMovement;
import com.library.entity.Account;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookItemService {

    @Autowired
    private BookItemRepository bookItemRepository;
    
    @Autowired
    private BookTitleService bookTitleService;
    
    @Autowired
    private LibraryService libraryService;

    @Autowired
    private BookMovementService bookMovementService;

    @Autowired
    private AccountRepository accountRepository;

    // === BASIC CRUD OPERATIONS ===

    /**
     * Lấy tất cả book items với phân trang (trả về DTO)
     */
    public PagedResponse<BookItemResponse> getAllBookItems(int page, int size, String sortBy, String sortDir) {
        page = Math.max(page, 0);
        size = size <= 0 ? 10 : Math.min(size, 100);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<BookItem> bookItemPage = bookItemRepository.findAll(pageable);
        
        return createPagedResponse(bookItemPage);
    }

    /**
     * Lấy book item theo ID (trả về DTO)
     */
    public Optional<BookItemResponse> getBookItemById(Integer id) {
        return bookItemRepository.findById(id)
                .map(BookItemResponse::new);
    }
    
    /**
     * Lấy book item entity theo ID (cho internal use)
     */
    public Optional<BookItem> getBookItemEntityById(Integer id) {
        return bookItemRepository.findById(id);
    }

    /**
     * Lấy book item theo item code (trả về DTO)
     */
    public Optional<BookItemResponse> getBookItemByItemCode(String itemCode) {
        return bookItemRepository.findByItemCode(itemCode)
                .map(BookItemResponse::new);
    }

    /**
     * Tạo book item mới từ DTO
     */
    public BookItemResponse createBookItem(BookItemRequest bookItemRequest) {
        // Kiểm tra item code đã tồn tại chưa
        if (bookItemRepository.existsByItemCode(bookItemRequest.getItemCode())) {
            throw new RuntimeException("Item code already exists: " + bookItemRequest.getItemCode());
        }
        
        // Lấy BookTitle và Library entities
        BookTitle bookTitle = bookTitleService.getBookTitleById(bookItemRequest.getBookTitleId())
                .orElseThrow(() -> new RuntimeException("Book title not found with id: " + bookItemRequest.getBookTitleId()));
        
        Library library = libraryService.getLibraryById(bookItemRequest.getLibraryId())
                .orElseThrow(() -> new RuntimeException("Library not found with id: " + bookItemRequest.getLibraryId()));
        
        // Tạo BookItem entity
        BookItem bookItem = new BookItem();
        bookItem.setBookTitle(bookTitle);
        bookItem.setLibrary(library);
        bookItem.setItemCode(bookItemRequest.getItemCode());
        bookItem.setShelfLocation(bookItemRequest.getShelfLocation());
        bookItem.setCondition(bookItemRequest.getCondition());
        bookItem.setStatus(bookItemRequest.getStatus());
        bookItem.setAcquisitionDate(bookItemRequest.getAcquisitionDate());
        bookItem.setAcquisitionCost(bookItemRequest.getAcquisitionCost());
        bookItem.setNotes(bookItemRequest.getNotes());
        bookItem.setCreatedAt(LocalDateTime.now());
        bookItem.setUpdatedAt(LocalDateTime.now());
        
        BookItem savedBookItem = bookItemRepository.save(bookItem);
        return new BookItemResponse(savedBookItem);
    }

    /**
     * Cập nhật book item từ DTO
     */
    public BookItemResponse updateBookItem(Integer id, BookItemRequest bookItemRequest) {
        Optional<BookItem> optionalBookItem = bookItemRepository.findById(id);
        if (optionalBookItem.isEmpty()) {
            throw new RuntimeException("Book item not found with id: " + id);
        }
        
        BookItem bookItem = optionalBookItem.get();
        
        // Lưu trạng thái cũ để log movement
        String oldStatus = bookItem.getStatus();
        String oldLocation = bookItem.getShelfLocation();
        
        // Kiểm tra item code conflict (nếu thay đổi)
        if (!bookItem.getItemCode().equals(bookItemRequest.getItemCode()) && 
            bookItemRepository.existsByItemCode(bookItemRequest.getItemCode())) {
            throw new RuntimeException("Item code already exists: " + bookItemRequest.getItemCode());
        }
        
        // Cập nhật BookTitle và Library nếu thay đổi
        if (!bookItem.getBookTitle().getId().equals(bookItemRequest.getBookTitleId())) {
            BookTitle bookTitle = bookTitleService.getBookTitleById(bookItemRequest.getBookTitleId())
                    .orElseThrow(() -> new RuntimeException("Book title not found with id: " + bookItemRequest.getBookTitleId()));
            bookItem.setBookTitle(bookTitle);
        }
        
        if (!bookItem.getLibrary().getId().equals(bookItemRequest.getLibraryId())) {
            Library library = libraryService.getLibraryById(bookItemRequest.getLibraryId())
                    .orElseThrow(() -> new RuntimeException("Library not found with id: " + bookItemRequest.getLibraryId()));
            bookItem.setLibrary(library);
        }
        
        // Cập nhật các field khác
        bookItem.setItemCode(bookItemRequest.getItemCode());
        bookItem.setShelfLocation(bookItemRequest.getShelfLocation());
        bookItem.setCondition(bookItemRequest.getCondition());
        bookItem.setStatus(bookItemRequest.getStatus());
        bookItem.setAcquisitionDate(bookItemRequest.getAcquisitionDate());
        bookItem.setAcquisitionCost(bookItemRequest.getAcquisitionCost());
        bookItem.setNotes(bookItemRequest.getNotes());
        bookItem.setUpdatedAt(LocalDateTime.now());
        
        BookItem updatedBookItem = bookItemRepository.save(bookItem);
        
        // Log movement nếu có thay đổi vị trí hoặc trạng thái
        Account performer = getCurrentUserOrDefault();
        if (performer != null) {
            boolean statusChanged = !oldStatus.equals(updatedBookItem.getStatus());
            boolean locationChanged = !java.util.Objects.equals(oldLocation, updatedBookItem.getShelfLocation());
            
            if (statusChanged || locationChanged) {
                String notes = "Cập nhật thông tin sách";
                if (statusChanged && locationChanged) {
                    notes = "Cập nhật trạng thái và vị trí sách";
                } else if (statusChanged) {
                    notes = "Cập nhật trạng thái sách: " + oldStatus + " → " + updatedBookItem.getStatus();
                } else if (locationChanged) {
                    notes = "Chuyển vị trí sách: " + (oldLocation != null ? oldLocation : "N/A") + " → " + 
                           (updatedBookItem.getShelfLocation() != null ? updatedBookItem.getShelfLocation() : "N/A");
                }
                
                bookMovementService.logMovement(
                    updatedBookItem,
                    null,
                    BookMovement.MovementAction.MOVED,
                    oldStatus,
                    updatedBookItem.getStatus(),
                    oldLocation,
                    updatedBookItem.getShelfLocation(),
                    performer,
                    notes
                );
            }
        }
        
        return new BookItemResponse(updatedBookItem);
    }

    /**
     * Xóa book item
     */
    public boolean deleteBookItem(Integer id) {
        if (bookItemRepository.existsById(id)) {
            bookItemRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // === SEARCH METHODS ===

    /**
     * Lấy book items theo book title
     */
    public List<BookItem> getBookItemsByBookTitle(BookTitle bookTitle) {
        return bookItemRepository.findByBookTitle(bookTitle);
    }

    /**
     * Lấy book items theo library với phân trang
     */
    public Page<BookItem> getBookItemsByLibrary(Library library, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookItemRepository.findByLibrary(library, pageable);
    }

    /**
     * Lấy book items theo status với phân trang (trả về DTO)
     */
    public PagedResponse<BookItemResponse> getBookItemsByStatus(String status, int page, int size) {
        page = Math.max(page, 0);
        size = size <= 0 ? 10 : Math.min(size, 100);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BookItem> bookItemPage = bookItemRepository.findByStatus(status, pageable);
        
        return createPagedResponse(bookItemPage);
    }

    /**
     * Lấy available book items của một book title
     */
    public List<BookItem> getAvailableBookItems(BookTitle bookTitle) {
        return bookItemRepository.findAvailableItemsByBookTitle(bookTitle);
    }

    /**
     * Tìm kiếm book items theo nhiều điều kiện (trả về DTO)
     */
    public PagedResponse<BookItemResponse> searchBookItems(String status, String condition, Integer libraryId, 
                                         String shelfLocation, int page, int size) {
        page = Math.max(page, 0);
        size = size <= 0 ? 10 : Math.min(size, 100);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BookItem> bookItemPage = bookItemRepository.findItemsByMultipleCriteria(status, condition, libraryId, shelfLocation, pageable);
        
        return createPagedResponse(bookItemPage);
    }

    // === STATUS MANAGEMENT METHODS ===

    /**
     * Đánh dấu book item là đã mượn (trả về DTO)
     */
    public BookItemResponse markAsBorrowed(Integer id) {
        Optional<BookItem> optionalBookItem = bookItemRepository.findById(id);
        if (optionalBookItem.isEmpty()) {
            throw new RuntimeException("Book item not found with id: " + id);
        }
        
        BookItem bookItem = optionalBookItem.get();
        if (!bookItem.isAvailable()) {
            throw new RuntimeException("Book item is not available for borrowing");
        }
        
        bookItem.markAsBorrowed();
        BookItem savedBookItem = bookItemRepository.save(bookItem);
        return new BookItemResponse(savedBookItem);
    }

    /**
     * Đánh dấu book item là đã trả (trả về DTO)
     */
    public BookItemResponse markAsReturned(Integer id) {
        Optional<BookItem> optionalBookItem = bookItemRepository.findById(id);
        if (optionalBookItem.isEmpty()) {
            throw new RuntimeException("Book item not found with id: " + id);
        }
        
        BookItem bookItem = optionalBookItem.get();
        bookItem.markAsReturned();
        BookItem savedBookItem = bookItemRepository.save(bookItem);
        return new BookItemResponse(savedBookItem);
    }

    /**
     * Đánh dấu book item là bị mất (trả về DTO)
     */
    public BookItemResponse markAsLost(Integer id) {
        Optional<BookItem> optionalBookItem = bookItemRepository.findById(id);
        if (optionalBookItem.isEmpty()) {
            throw new RuntimeException("Book item not found with id: " + id);
        }
        
        BookItem bookItem = optionalBookItem.get();
        bookItem.markAsLost();
        BookItem savedBookItem = bookItemRepository.save(bookItem);
        return new BookItemResponse(savedBookItem);
    }

    /**
     * Đánh dấu book item là được đặt trước (trả về DTO)
     */
    public BookItemResponse markAsReserved(Integer id) {
        Optional<BookItem> optionalBookItem = bookItemRepository.findById(id);
        if (optionalBookItem.isEmpty()) {
            throw new RuntimeException("Book item not found with id: " + id);
        }
        
        BookItem bookItem = optionalBookItem.get();
        if (!bookItem.isAvailable()) {
            throw new RuntimeException("Book item is not available for reservation");
        }
        
        bookItem.markAsReserved();
        BookItem savedBookItem = bookItemRepository.save(bookItem);
        return new BookItemResponse(savedBookItem);
    }

    // === STATISTICAL METHODS ===

    /**
     * Đếm tổng số book items
     */
    public long getTotalBookItems() {
        return bookItemRepository.count();
    }

    /**
     * Đếm book items theo status
     */
    public long countByStatus(String status) {
        return bookItemRepository.countByStatus(status);
    }

    /**
     * Đếm book items theo library
     */
    public long countByLibrary(Library library) {
        return bookItemRepository.countByLibrary(library);
    }

    /**
     * Thống kê book items theo status
     */
    public List<Object[]> getBookItemStatsByStatus() {
        return bookItemRepository.countBookItemsByStatus();
    }

    /**
     * Thống kê book items theo condition
     */
    public List<Object[]> getBookItemStatsByCondition() {
        return bookItemRepository.countBookItemsByCondition();
    }

    /**
     * Lấy book items cần bảo trì (trả về DTO)
     */
    public PagedResponse<BookItemResponse> getItemsNeedingMaintenance(int page, int size) {
        page = Math.max(page, 0);
        size = size <= 0 ? 10 : Math.min(size, 100);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BookItem> bookItemPage = bookItemRepository.findItemsNeedingMaintenance(pageable);
        
        return createPagedResponse(bookItemPage);
    }

    // === UTILITY METHODS ===

    /**
     * Kiểm tra item code đã tồn tại chưa
     */
    public boolean isItemCodeExists(String itemCode) {
        return bookItemRepository.existsByItemCode(itemCode);
    }

    /**
     * Tạo item code tự động
     */
    public String generateItemCode(BookTitle bookTitle, Library library) {
        String prefix = library.getName().substring(0, Math.min(3, library.getName().length())).toUpperCase();
        String bookPrefix = bookTitle.getTitle().substring(0, Math.min(3, bookTitle.getTitle().length())).toUpperCase();
        
        // Tìm số thứ tự tiếp theo
        long count = bookItemRepository.countByBookTitle(bookTitle) + 1;
        
        return prefix + "-" + bookPrefix + "-" + String.format("%04d", count);
    }
    
    /**
     * Utility method để tạo PagedResponse
     */
    private PagedResponse<BookItemResponse> createPagedResponse(Page<BookItem> bookItemPage) {
        List<BookItemResponse> bookItemResponses = bookItemPage.getContent().stream()
                .map(BookItemResponse::new)
                .collect(Collectors.toList());

        PagedResponse<BookItemResponse> response = new PagedResponse<>();
        response.setContent(bookItemResponses);
        response.setPage(bookItemPage.getNumber());
        response.setSize(bookItemPage.getSize());
        response.setTotalElements(bookItemPage.getTotalElements());
        response.setTotalPages(bookItemPage.getTotalPages());
        response.setFirst(bookItemPage.isFirst());
        response.setLast(bookItemPage.isLast());
        response.setHasNext(bookItemPage.hasNext());
        response.setHasPrevious(bookItemPage.hasPrevious());

        return response;
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
