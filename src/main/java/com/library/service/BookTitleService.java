// File: src/main/java/com/library/service/BookTitleService.java
package com.library.service;

import com.library.dto.request.BookTitleRequest;
import com.library.dto.response.BookTitleResponse;
import com.library.dto.response.PagedResponse;
import com.library.entity.BookTitle;
import com.library.repository.BookTitleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookTitleService {

    @Autowired
    private BookTitleRepository bookTitleRepository;

    /**
     * Tạo book title mới
     */
    public BookTitleResponse createBook(BookTitleRequest bookTitleRequest) {
        // Kiểm tra ISBN đã tồn tại chưa (nếu có)
        if (bookTitleRequest.getIsbn() != null && !bookTitleRequest.getIsbn().trim().isEmpty()) {
            if (bookTitleRepository.existsByIsbn(bookTitleRequest.getIsbn())) {
                throw new RuntimeException("ISBN already exists: " + bookTitleRequest.getIsbn());
            }
        }

        BookTitle bookTitle = new BookTitle();
        bookTitle.setIsbn(bookTitleRequest.getIsbn());
        bookTitle.setTitle(bookTitleRequest.getTitle());
        bookTitle.setAuthor(bookTitleRequest.getAuthor());
        bookTitle.setPublisher(bookTitleRequest.getPublisher());
        bookTitle.setPublishedYear(bookTitleRequest.getPublishedYear());
        bookTitle.setCategory(bookTitleRequest.getCategory());
        bookTitle.setDescription(bookTitleRequest.getDescription());
        bookTitle.setCoverImage(bookTitleRequest.getCoverImage());
        bookTitle.setLanguage(bookTitleRequest.getLanguage() != null ? bookTitleRequest.getLanguage() : "vi");
        bookTitle.setTotalPages(bookTitleRequest.getTotalPages());
        bookTitle.setCreatedAt(LocalDateTime.now());

        BookTitle savedBook = bookTitleRepository.save(bookTitle);
        return new BookTitleResponse(savedBook);
    }

    /**
     * Cập nhật book title
     */
    public BookTitleResponse updateBook(Integer id, BookTitleRequest bookTitleRequest) {
        Optional<BookTitle> optionalBook = bookTitleRepository.findById(id);
        if (optionalBook.isEmpty()) {
            throw new RuntimeException("Book not found with id: " + id);
        }

        BookTitle bookTitle = optionalBook.get();
        

        // Cập nhật các field
        bookTitle.setIsbn(bookTitleRequest.getIsbn());
        bookTitle.setTitle(bookTitleRequest.getTitle());
        bookTitle.setAuthor(bookTitleRequest.getAuthor());
        bookTitle.setPublisher(bookTitleRequest.getPublisher());
        bookTitle.setPublishedYear(bookTitleRequest.getPublishedYear());
        bookTitle.setCategory(bookTitleRequest.getCategory());
        bookTitle.setDescription(bookTitleRequest.getDescription());
        bookTitle.setCoverImage(bookTitleRequest.getCoverImage());
        bookTitle.setLanguage(bookTitleRequest.getLanguage() != null ? bookTitleRequest.getLanguage() : "vi");
        bookTitle.setTotalPages(bookTitleRequest.getTotalPages());

        BookTitle updatedBook = bookTitleRepository.save(bookTitle);
        return new BookTitleResponse(updatedBook);
    }

    /**
     * Xóa book title
     */
    public boolean deleteBook(Integer id) {
        Optional<BookTitle> optionalBook = bookTitleRepository.findById(id);
        if (optionalBook.isEmpty()) {
            return false;
        }

        BookTitle bookTitle = optionalBook.get();
        
        // Kiểm tra xem có book items nào đang sử dụng không
        if (bookTitle.getTotalCopies() > 0) {
            throw new RuntimeException("Cannot delete book with existing book items. Please delete all book items first.");
        }

        bookTitleRepository.deleteById(id);
        return true;
    }

    // === GET ALL BOOKS WITH PAGINATION ===
    public PagedResponse<BookTitleResponse> getAllBooks(int page, int size, String sortBy, String sortDir) {
        // Validate page and size
        page = Math.max(page, 0);
        size = size <= 0 ? 10 : Math.min(size, 100); // Max 100 items per page

        // Create sort
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<BookTitle> bookPage = bookTitleRepository.findAll(pageable);

        return createPagedResponse(bookPage);
    }

    // === SEARCH BOOKS ===
    public PagedResponse<BookTitleResponse> searchBooks(String keyword, int page, int size, String sortBy, String sortDir) {
        page = Math.max(page, 0);
        size = size <= 0 ? 10 : Math.min(size, 100);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<BookTitle> bookPage;
        if (keyword == null || keyword.trim().isEmpty()) {
            bookPage = bookTitleRepository.findAll(pageable);
        } else {
            bookPage = bookTitleRepository.searchBooks(keyword.trim(), pageable);
        }

        return createPagedResponse(bookPage);
    }

    // === ADVANCED SEARCH ===
    public PagedResponse<BookTitleResponse> advancedSearch(String title, String author, String category,
                                                      String publisher, Integer yearFrom, Integer yearTo,
                                                      int page, int size, String sortBy, String sortDir) {
        page = Math.max(page, 0);
        size = size <= 0 ? 10 : Math.min(size, 100);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // Convert empty strings to null
        title = (title != null && title.trim().isEmpty()) ? null : title;
        author = (author != null && author.trim().isEmpty()) ? null : author;
        category = (category != null && category.trim().isEmpty()) ? null : category;
        publisher = (publisher != null && publisher.trim().isEmpty()) ? null : publisher;

        Page<BookTitle> bookPage = bookTitleRepository.advancedSearch(
                title, author, category, publisher, yearFrom, yearTo, pageable);

        return createPagedResponse(bookPage);
    }

    // === GET BOOK BY ID ===
    public Optional<BookTitleResponse> getBookById(Integer id) {
        return bookTitleRepository.findById(id)
                .map(BookTitleResponse::new);
    }

    // === GET BOOK TITLE ENTITY BY ID ===
    public Optional<BookTitle> getBookTitleById(Integer id) {
        return bookTitleRepository.findById(id);
    }

    // === GET BOOK BY ISBN ===
    public Optional<BookTitleResponse> getBookByIsbn(String isbn) {
        return bookTitleRepository.findByIsbn(isbn)
                .map(BookTitleResponse::new);
    }

    // === GET BOOKS BY CATEGORY ===
    public PagedResponse<BookTitleResponse> getBooksByCategory(String category, int page, int size, String sortBy, String sortDir) {
        page = Math.max(page, 0);
        size = size <= 0 ? 10 : Math.min(size, 100);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<BookTitle> bookPage = bookTitleRepository.findByCategoryContainingIgnoreCase(category, pageable);

        return createPagedResponse(bookPage);
    }

    // === GET LATEST BOOKS ===
    public PagedResponse<BookTitleResponse> getLatestBooks(int page, int size) {
        page = Math.max(page, 0);
        size = size <= 0 ? 10 : Math.min(size, 50);

        Pageable pageable = PageRequest.of(page, size);
        Page<BookTitle> bookPage = bookTitleRepository.findAllByOrderByCreatedAtDesc(pageable);

        return createPagedResponse(bookPage);
    }

    // === COUNT METHODS ===
    public long getTotalBooks() {
        return bookTitleRepository.count();
    }

    public List<Object[]> getBookStatsByCategory() {
        return bookTitleRepository.countBooksByCategory();
    }

    // === UTILITY METHODS ===
    
    /**
     * Kiểm tra ISBN đã tồn tại chưa
     */
    public boolean isIsbnExists(String isbn) {
        return bookTitleRepository.existsByIsbn(isbn);
    }

    /**
     * Utility method để tạo PagedResponse
     */
    private PagedResponse<BookTitleResponse> createPagedResponse(Page<BookTitle> bookPage) {
        List<BookTitleResponse> bookTitleResponses = bookPage.getContent().stream()
                .map(BookTitleResponse::new)
                .collect(Collectors.toList());

        PagedResponse<BookTitleResponse> response = new PagedResponse<>();
        response.setContent(bookTitleResponses);
        response.setPage(bookPage.getNumber());
        response.setSize(bookPage.getSize());
        response.setTotalElements(bookPage.getTotalElements());
        response.setTotalPages(bookPage.getTotalPages());
        response.setFirst(bookPage.isFirst());
        response.setLast(bookPage.isLast());
        response.setHasNext(bookPage.hasNext());
        response.setHasPrevious(bookPage.hasPrevious());

        return response;
    }
}
