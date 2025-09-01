// File: src/main/java/com/library/repository/BookItemRepository.java
package com.library.repository;

import com.library.entity.BookItem;
import com.library.entity.BookTitle;
import com.library.entity.Library;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookItemRepository extends JpaRepository<BookItem, Integer> {

    // === BASIC SEARCH METHODS ===

    /**
     * Tìm book item theo item code
     */
    Optional<BookItem> findByItemCode(String itemCode);

    /**
     * Tìm book item theo ID với pessimistic write lock để tránh race condition khi mượn sách
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT bi FROM BookItem bi WHERE bi.id = :id")
    Optional<BookItem> findByIdWithLock(@Param("id") Integer id);

    /**
     * Kiểm tra item code đã tồn tại chưa
     */
    boolean existsByItemCode(String itemCode);

    /**
     * Tìm tất cả book items của một book title
     */
    List<BookItem> findByBookTitle(BookTitle bookTitle);

    /**
     * Tìm tất cả book items của một thư viện
     */
    Page<BookItem> findByLibrary(Library library, Pageable pageable);

    /**
     * Tìm book items theo status
     */
    Page<BookItem> findByStatus(String status, Pageable pageable);

    /**
     * Tìm book items theo condition
     */
    Page<BookItem> findByCondition(String condition, Pageable pageable);

    // === ADVANCED SEARCH METHODS ===

    /**
     * Tìm book items theo book title và library
     */
    List<BookItem> findByBookTitleAndLibrary(BookTitle bookTitle, Library library);

    /**
     * Tìm book items theo book title và status
     */
    List<BookItem> findByBookTitleAndStatus(BookTitle bookTitle, String status);

    /**
     * Tìm available book items của một book title
     */
    @Query("SELECT bi FROM BookItem bi WHERE bi.bookTitle = :bookTitle AND bi.status = 'AVAILABLE'")
    List<BookItem> findAvailableItemsByBookTitle(@Param("bookTitle") BookTitle bookTitle);

    /**
     * Tìm book items theo shelf location
     */
    List<BookItem> findByShelfLocationContainingIgnoreCase(String shelfLocation);

    // === STATISTICAL QUERIES ===

    /**
     * Đếm tổng số book items
     */
    long count();

    /**
     * Đếm book items theo status
     */
    @Query("SELECT COUNT(bi) FROM BookItem bi WHERE bi.status = :status")
    long countByStatus(@Param("status") String status);

    /**
     * Đếm book items theo library
     */
    long countByLibrary(Library library);

    /**
     * Đếm book items theo book title
     */
    long countByBookTitle(BookTitle bookTitle);

    /**
     * Thống kê book items theo status
     */
    @Query("SELECT bi.status, COUNT(bi) FROM BookItem bi GROUP BY bi.status")
    List<Object[]> countBookItemsByStatus();

    /**
     * Thống kê book items theo condition
     */
    @Query("SELECT bi.condition, COUNT(bi) FROM BookItem bi GROUP BY bi.condition")
    List<Object[]> countBookItemsByCondition();

    /**
     * Thống kê book items theo library
     */
    @Query("SELECT l.name, COUNT(bi) FROM BookItem bi JOIN bi.library l GROUP BY l.name")
    List<Object[]> countBookItemsByLibrary();

    // === COMPLEX QUERIES ===

    /**
     * Tìm book items cần bảo trì (condition = DAMAGED)
     */
    @Query("SELECT bi FROM BookItem bi WHERE bi.condition = 'DAMAGED' ORDER BY bi.updatedAt DESC")
    Page<BookItem> findItemsNeedingMaintenance(Pageable pageable);

    /**
     * Tìm book items theo nhiều điều kiện
     */
    @Query("SELECT bi FROM BookItem bi WHERE " +
            "(:status IS NULL OR bi.status = :status) AND " +
            "(:condition IS NULL OR bi.condition = :condition) AND " +
            "(:libraryId IS NULL OR bi.library.id = :libraryId) AND " +
            "(:shelfLocation IS NULL OR LOWER(bi.shelfLocation) LIKE LOWER(CONCAT('%', :shelfLocation, '%')))")
    Page<BookItem> findItemsByMultipleCriteria(
            @Param("status") String status,
            @Param("condition") String condition,
            @Param("libraryId") Integer libraryId,
            @Param("shelfLocation") String shelfLocation,
            Pageable pageable);
}
