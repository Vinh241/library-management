// File: src/main/java/com/library/controller/BookTitleController.java
package com.library.controller;

import com.library.dto.request.BookTitleRequest;
import com.library.dto.response.BookTitleResponse;
import com.library.dto.response.PagedResponse;
import com.library.service.BookTitleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class BookTitleController {

    @Autowired
    private BookTitleService bookTitleService;

    // === CREATE BOOK ===
    /**
     * POST /api/books - Tạo book title mới
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('PROVINCE_MANAGER')")
    public ResponseEntity<BookTitleResponse> createBook(@Valid @RequestBody BookTitleRequest bookTitleRequest) {
        try {
            BookTitleResponse createdBook = bookTitleService.createBook(bookTitleRequest);
            return new ResponseEntity<>(createdBook, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // === UPDATE BOOK ===
    /**
     * PUT /api/books/{id} - Cập nhật book title
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('PROVINCE_MANAGER')")
    public ResponseEntity<BookTitleResponse> updateBook(@PathVariable Integer id, 
                                                   @Valid @RequestBody BookTitleRequest bookTitleRequest) {
        try {
            BookTitleResponse updatedBook = bookTitleService.updateBook(id, bookTitleRequest);
            return ResponseEntity.ok(updatedBook);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // === DELETE BOOK ===
    /**
     * DELETE /api/books/{id} - Xóa book title
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or hasRole('PROVINCE_MANAGER')")
    public ResponseEntity<Void> deleteBook(@PathVariable Integer id) {
        try {
            boolean deleted = bookTitleService.deleteBook(id);
            if (deleted) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // === GET ALL BOOKS WITH PAGINATION ===
    /**
     * GET /api/books?page=0&size=10&sortBy=title&sortDir=asc
     */
    @GetMapping
    public ResponseEntity<PagedResponse<BookTitleResponse>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PagedResponse<BookTitleResponse> response = bookTitleService.getAllBooks(page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    // === SEARCH BOOKS ===
    /**
     * GET /api/books/search?keyword=java&page=0&size=10
     */
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<BookTitleResponse>> searchBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        PagedResponse<BookTitleResponse> response = bookTitleService.searchBooks(keyword, page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    // === ADVANCED SEARCH ===
    /**
     * GET /api/books/advanced-search?title=java&author=robert&category=technology&yearFrom=2020&yearTo=2024
     */
    @GetMapping("/advanced-search")
    public ResponseEntity<PagedResponse<BookTitleResponse>> advancedSearch(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String publisher,
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PagedResponse<BookTitleResponse> response = bookTitleService.advancedSearch(
                title, author, category, publisher, yearFrom, yearTo, page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/books/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookTitleResponse> getBookById(@PathVariable Integer id) {
        Optional<BookTitleResponse> book = bookTitleService.getBookById(id);
        return book.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // === GET BOOK BY ISBN ===
    /**
     * GET /api/books/isbn/{isbn}
     */
    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<BookTitleResponse> getBookByIsbn(@PathVariable String isbn) {
        Optional<BookTitleResponse> book = bookTitleService.getBookByIsbn(isbn);
        return book.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
