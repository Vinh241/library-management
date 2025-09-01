package com.library.controller;

import com.library.dto.request.BookMovementRequest;
import com.library.dto.request.UpdateBookMovementRequest;
import com.library.dto.response.BookMovementResponse;
import com.library.dto.response.PagedResponse;
import com.library.service.BookMovementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/book-movements")
@CrossOrigin(origins = "*")
public class BookMovementController {

    @Autowired
    private BookMovementService bookMovementService;

    /**
     * POST /api/book-movements - Tạo lịch sử di chueyen sách mới (chỉ admin, thủ thư, quản lý tỉnh)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('PROVINCE_MANAGER')")
    public ResponseEntity<?> createMovement(@Valid @RequestBody BookMovementRequest request) {
        try {
            BookMovementResponse movement = bookMovementService.createMovement(request);
            return new ResponseEntity<>(movement, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * GET /api/book-movements - Lấy tất cả movements với phân trang
     */
    @GetMapping
    public ResponseEntity<PagedResponse<BookMovementResponse>> getAllMovements(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "performedAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir) {
        try {
            PagedResponse<BookMovementResponse> movements = bookMovementService.getAllMovements(page, size, sortBy, sortDir);
            return new ResponseEntity<>(movements, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/book-movements/{id} - Lấy movement theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMovementById(@PathVariable Integer id) {
        try {
            BookMovementResponse movement = bookMovementService.getMovementById(id);
            return new ResponseEntity<>(movement, HttpStatus.OK);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * GET /api/book-movements/book-item/{bookItemId} - Lấy movements của một book item
     */
    @GetMapping("/book-item/{bookItemId}")
    public ResponseEntity<PagedResponse<BookMovementResponse>> getMovementsByBookItem(
            @PathVariable Integer bookItemId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            PagedResponse<BookMovementResponse> movements = bookMovementService.getMovementsByBookItem(bookItemId, page, size);
            return new ResponseEntity<>(movements, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/book-movements/book-item/{bookItemId}/count - Đếm số movements của book item
     */
    @GetMapping("/book-item/{bookItemId}/count")
    public ResponseEntity<Map<String, Long>> countMovementsByBookItem(@PathVariable Integer bookItemId) {
        try {
            long count = bookMovementService.countMovementsByBookItem(bookItemId);
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/book-movements/book-item/{bookItemId}/latest - Lấy movement gần nhất của book item
     */
    @GetMapping("/book-item/{bookItemId}/latest")
    public ResponseEntity<?> getLatestMovementByBookItem(@PathVariable Integer bookItemId) {
        try {
            Optional<BookMovementResponse> movement = bookMovementService.getLatestMovementByBookItem(bookItemId);
            if (movement.isPresent()) {
                return new ResponseEntity<>(movement.get(), HttpStatus.OK);
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("message", "No movements found for this book item");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/book-movements/action/{action} - Lấy movements theo action
     */
    @GetMapping("/action/{action}")
    public ResponseEntity<PagedResponse<BookMovementResponse>> getMovementsByAction(
            @PathVariable String action,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            PagedResponse<BookMovementResponse> movements = bookMovementService.getMovementsByAction(action, page, size);
            return new ResponseEntity<>(movements, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/book-movements/performer/{performerId} - Lấy movements theo người thực hiện
     */
    @GetMapping("/performer/{performerId}")
    public ResponseEntity<PagedResponse<BookMovementResponse>> getMovementsByPerformer(
            @PathVariable Integer performerId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            PagedResponse<BookMovementResponse> movements = bookMovementService.getMovementsByPerformer(performerId, page, size);
            return new ResponseEntity<>(movements, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/book-movements/library/{libraryId} - Lấy movements theo thư viện
     */
    @GetMapping("/library/{libraryId}")
    public ResponseEntity<PagedResponse<BookMovementResponse>> getMovementsByLibrary(
            @PathVariable Integer libraryId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            PagedResponse<BookMovementResponse> movements = bookMovementService.getMovementsByLibrary(libraryId, page, size);
            return new ResponseEntity<>(movements, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/book-movements/search - Tìm kiếm movements với nhiều điều kiện
     */
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<BookMovementResponse>> searchMovements(
            @RequestParam(value = "bookItemId", required = false) Integer bookItemId,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "performerId", required = false) Integer performerId,
            @RequestParam(value = "startDate", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(value = "endDate", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            PagedResponse<BookMovementResponse> movements = bookMovementService.searchMovements(
                    bookItemId, action, performerId, startDate, endDate, page, size);
            return new ResponseEntity<>(movements, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET /api/book-movements/statistics - Thống kê movements theo action trong khoảng thời gian
     */
    @GetMapping("/statistics")
    public ResponseEntity<List<Object[]>> getMovementStatistics(
            @RequestParam(value = "startDate", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(value = "endDate", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            // Nếu không có ngày, lấy thống kê 30 ngày gần đây
            if (startDate == null) {
                startDate = LocalDateTime.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDateTime.now();
            }

            List<Object[]> statistics = bookMovementService.getMovementStatistics(startDate, endDate);
            return new ResponseEntity<>(statistics, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * PUT /api/book-movements/{id} - Cập nhật movement (chỉ admin, thủ thư, quản lý tỉnh, hoặc người tạo)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('PROVINCE_MANAGER') or hasRole('READER')")
    public ResponseEntity<?> updateMovement(@PathVariable Integer id, @Valid @RequestBody UpdateBookMovementRequest request) {
        try {
            BookMovementResponse movement = bookMovementService.updateMovement(id, request);
            return new ResponseEntity<>(movement, HttpStatus.OK);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * DELETE /api/book-movements/{id} - Xóa movement (chỉ admin hoặc người tạo)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('PROVINCE_MANAGER') or hasRole('READER')")
    public ResponseEntity<?> deleteMovement(@PathVariable Integer id) {
        try {
            bookMovementService.deleteMovement(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Movement deleted successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }
}
