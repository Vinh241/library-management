// File: src/main/java/com/library/repository/BookTitleRepository.java
package com.library.repository;

import com.library.entity.BookTitle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookTitleRepository extends JpaRepository<BookTitle, Integer> {

    // === SEARCH METHODS ===

    // Tìm kiếm theo title (có phân trang)
    Page<BookTitle> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // Tìm kiếm theo author (có phân trang)
    Page<BookTitle> findByAuthorContainingIgnoreCase(String author, Pageable pageable);

    // Tìm kiếm theo category (có phân trang)
    Page<BookTitle> findByCategoryContainingIgnoreCase(String category, Pageable pageable);

    // Tìm kiếm theo ISBN
    Optional<BookTitle> findByIsbn(String isbn);

    // Tìm kiếm tổng hợp (title, author, isbn) với phân trang
    @Query("SELECT b FROM BookTitle b WHERE " +
            "(:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(:keyword IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(:keyword IS NULL OR b.isbn = :keyword)")
    Page<BookTitle> searchBooks(@Param("keyword") String keyword, Pageable pageable);

    // Tìm kiếm nâng cao với nhiều điều kiện
    @Query("SELECT b FROM BookTitle b WHERE " +
            "(:title IS NULL OR :title = '' OR LOWER(COALESCE(b.title, '')) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:author IS NULL OR :author = '' OR LOWER(COALESCE(b.author, '')) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
            "(:category IS NULL OR :category = '' OR LOWER(COALESCE(b.category, '')) LIKE LOWER(CONCAT('%', :category, '%'))) AND " +
            "(:publisher IS NULL OR :publisher = '' OR LOWER(COALESCE(b.publisher, '')) LIKE LOWER(CONCAT('%', :publisher, '%'))) AND " +
            "(:yearFrom IS NULL OR b.publishedYear >= :yearFrom) AND " +
            "(:yearTo IS NULL OR b.publishedYear <= :yearTo)")
    Page<BookTitle> advancedSearch(@Param("title") String title,
                                   @Param("author") String author,
                                   @Param("category") String category,
                                   @Param("publisher") String publisher,
                                   @Param("yearFrom") Integer yearFrom,
                                   @Param("yearTo") Integer yearTo,
                                   Pageable pageable);

    // === STATISTICAL QUERIES ===

    // Đếm sách theo category
    @Query("SELECT b.category, COUNT(b) FROM BookTitle b GROUP BY b.category")
    List<Object[]> countBooksByCategory();

    // Top sách mới nhất
    Page<BookTitle> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Sách theo năm xuất bản
    Page<BookTitle> findByPublishedYearBetween(Integer startYear, Integer endYear, Pageable pageable);

    // Kiểm tra ISBN có tồn tại không
    boolean existsByIsbn(String isbn);
}
