// File: src/main/java/com/library/entity/BookItem.java
package com.library.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "book_items")
public class BookItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_title_id", nullable = false)
    private BookTitle bookTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_id", nullable = false)
    private Library library;

    @Column(name = "item_code", length = 50, nullable = false, unique = true)
    private String itemCode;

    @Column(name = "shelf_location", length = 100)
    private String shelfLocation;

    @Column(length = 20, nullable = false)
    private String condition = "GOOD"; // EXCELLENT, GOOD, FAIR, DAMAGED

    @Column(length = 20, nullable = false)
    private String status = "AVAILABLE"; // AVAILABLE, BORROWED, RESERVED, LOST, MAINTENANCE

    @Column(name = "acquisition_date", nullable = false)
    private LocalDate acquisitionDate;

    @Column(name = "acquisition_cost", precision = 10, scale = 2)
    private BigDecimal acquisitionCost;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public BookItem() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public BookItem(BookTitle bookTitle, Library library, String itemCode) {
        this();
        this.bookTitle = bookTitle;
        this.library = library;
        this.itemCode = itemCode;
        this.acquisitionDate = LocalDate.now();
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public BookTitle getBookTitle() { return bookTitle; }
    public void setBookTitle(BookTitle bookTitle) { this.bookTitle = bookTitle; }

    public Library getLibrary() { return library; }
    public void setLibrary(Library library) { this.library = library; }

    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }

    public String getShelfLocation() { return shelfLocation; }
    public void setShelfLocation(String shelfLocation) { this.shelfLocation = shelfLocation; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getAcquisitionDate() { return acquisitionDate; }
    public void setAcquisitionDate(LocalDate acquisitionDate) { this.acquisitionDate = acquisitionDate; }

    public BigDecimal getAcquisitionCost() { return acquisitionCost; }
    public void setAcquisitionCost(BigDecimal acquisitionCost) { this.acquisitionCost = acquisitionCost; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Utility methods
    public boolean isAvailable() {
        return "AVAILABLE".equals(this.status);
    }

    public boolean isBorrowed() {
        return "BORROWED".equals(this.status);
    }

    public void markAsBorrowed() {
        this.status = "BORROWED";
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsReturned() {
        this.status = "AVAILABLE";
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsLost() {
        this.status = "LOST";
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsReserved() {
        this.status = "RESERVED";
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "BookItem{" +
                "id=" + id +
                ", itemCode='" + itemCode + '\'' +
                ", status='" + status + '\'' +
                ", condition='" + condition + '\'' +
                ", shelfLocation='" + shelfLocation + '\'' +
                '}';
    }
}
