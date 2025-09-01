package com.library.controller;

import com.library.dto.request.BookItemRequest;
import com.library.dto.response.BookItemResponse;
import com.library.dto.response.PagedResponse;
import com.library.entity.BookTitle;
import com.library.entity.Library;
import com.library.service.BookItemService;
import com.library.service.BookTitleService;
import com.library.service.LibraryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/book-items")
@CrossOrigin(origins = "*")
public class BookItemController {

    @Autowired
    private BookItemService bookItemService;

    @Autowired
    private BookTitleService bookTitleService;

    @Autowired
    private LibraryService libraryService;

    //Tạo sách - Chỉ ADMIN, PROVINCE_MANAGER, LIBRARIAN
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROVINCE_MANAGER') or hasRole('LIBRARIAN')")
    public ResponseEntity<BookItemResponse> createBookItem(@Valid @RequestBody BookItemRequest bookItemRequest) {
        try {
            BookItemResponse createdBookItem = bookItemService.createBookItem(bookItemRequest);
            return new ResponseEntity<>(createdBookItem, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

   //Cập nhật sách - Chỉ ADMIN, PROVINCE_MANAGER, LIBRARIAN
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROVINCE_MANAGER') or hasRole('LIBRARIAN')")
    public ResponseEntity<BookItemResponse> updateBookItem(@PathVariable Integer id, 
                                                   @Valid @RequestBody BookItemRequest bookItemRequest) {
        try {
            BookItemResponse updatedBookItem = bookItemService.updateBookItem(id, bookItemRequest);
            return ResponseEntity.ok(updatedBookItem);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    //Xoá sách - Chỉ ADMIN, PROVINCE_MANAGER
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROVINCE_MANAGER')")
    public ResponseEntity<Void> deleteBookItem(@PathVariable Integer id) {
        try {
            boolean deleted = bookItemService.deleteBookItem(id);
            if (deleted) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    //Lấy các quyển sách
    @GetMapping
    public ResponseEntity<PagedResponse<BookItemResponse>> getAllBookItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "itemCode") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PagedResponse<BookItemResponse> response = bookItemService.getAllBookItems(page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }
    //Tìm sách
    /**
     * GET /api/book-items/search?status=AVAILABLE&condition=GOOD&libraryId=1&shelfLocation=A1
     */
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<BookItemResponse>> searchBookItems(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) Integer libraryId,
            @RequestParam(required = false) String shelfLocation,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<BookItemResponse> response = bookItemService.searchBookItems(status, condition, libraryId, shelfLocation, page, size);
        return ResponseEntity.ok(response);
    }

    // === GET BOOK ITEM BY ID ===
    /**
     * GET /api/book-items/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookItemResponse> getBookItemById(@PathVariable Integer id) {
        Optional<BookItemResponse> bookItem = bookItemService.getBookItemById(id);
        return bookItem.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // === GET BOOK ITEM BY ITEM CODE ===
    /**
     * GET /api/book-items/code/{itemCode}
     */
    @GetMapping("/code/{itemCode}")
    public ResponseEntity<BookItemResponse> getBookItemByItemCode(@PathVariable String itemCode) {
        Optional<BookItemResponse> bookItem = bookItemService.getBookItemByItemCode(itemCode);
        return bookItem.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // === GET BOOK ITEMS BY STATUS ===
    /**
     * GET /api/book-items/status/{status}?page=0&size=10
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<PagedResponse<BookItemResponse>> getBookItemsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<BookItemResponse> response = bookItemService.getBookItemsByStatus(status, page, size);
        return ResponseEntity.ok(response);
    }


    /**
     * PUT /api/book-items/{id}/borrow - Đánh dấu là đã mượn
     * Chỉ LIBRARIAN, PROVINCE_MANAGER, ADMIN
     */
    @PutMapping("/{id}/borrow")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROVINCE_MANAGER') or hasRole('LIBRARIAN')")
    public ResponseEntity<BookItemResponse> markAsBorrowed(@PathVariable Integer id) {
        try {
            BookItemResponse bookItem = bookItemService.markAsBorrowed(id);
            return ResponseEntity.ok(bookItem);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * PUT /api/book-items/{id}/return - Đánh dấu là đã trả
     * Chỉ LIBRARIAN, PROVINCE_MANAGER, ADMIN
     */
    @PutMapping("/{id}/return")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROVINCE_MANAGER') or hasRole('LIBRARIAN')")
    public ResponseEntity<BookItemResponse> markAsReturned(@PathVariable Integer id) {
        try {
            BookItemResponse bookItem = bookItemService.markAsReturned(id);
            return ResponseEntity.ok(bookItem);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * PUT /api/book-items/{id}/lost - Đánh dấu là bị mất
     * Chỉ ADMIN, PROVINCE_MANAGER
     */
    @PutMapping("/{id}/lost")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROVINCE_MANAGER')")
    public ResponseEntity<BookItemResponse> markAsLost(@PathVariable Integer id) {
        try {
            BookItemResponse bookItem = bookItemService.markAsLost(id);
            return ResponseEntity.ok(bookItem);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * PUT /api/book-items/{id}/reserve - Đánh dấu là được đặt trước
     * Chỉ LIBRARIAN, PROVINCE_MANAGER, ADMIN
     */
    @PutMapping("/{id}/reserve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROVINCE_MANAGER') or hasRole('LIBRARIAN')")
    public ResponseEntity<BookItemResponse> markAsReserved(@PathVariable Integer id) {
        try {
            BookItemResponse bookItem = bookItemService.markAsReserved(id);
            return ResponseEntity.ok(bookItem);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}