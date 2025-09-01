// File: src/main/java/com/library/dto/response/BookItemResponse.java
package com.library.dto.response;

import com.library.entity.BookItem;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class BookItemResponse {
    private Integer id;
    private Integer bookTitleId;
    private String bookTitleTitle;
    private String bookTitleAuthor;
    private String bookTitleIsbn;
    private Integer libraryId;
    private String libraryName;
    private String itemCode;
    private String shelfLocation;
    private String condition;
    private String status;
    private LocalDate acquisitionDate;
    private BigDecimal acquisitionCost;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public BookItemResponse() {}

    public BookItemResponse(BookItem bookItem) {
        this.id = bookItem.getId();
        this.itemCode = bookItem.getItemCode();
        this.shelfLocation = bookItem.getShelfLocation();
        this.condition = bookItem.getCondition();
        this.status = bookItem.getStatus();
        this.acquisitionDate = bookItem.getAcquisitionDate();
        this.acquisitionCost = bookItem.getAcquisitionCost();
        this.notes = bookItem.getNotes();
        this.createdAt = bookItem.getCreatedAt();
        this.updatedAt = bookItem.getUpdatedAt();
        
        // Set book title information
        if (bookItem.getBookTitle() != null) {
            this.bookTitleId = bookItem.getBookTitle().getId();
            this.bookTitleTitle = bookItem.getBookTitle().getTitle();
            this.bookTitleAuthor = bookItem.getBookTitle().getAuthor();
            this.bookTitleIsbn = bookItem.getBookTitle().getIsbn();
        }
        
        // Set library information
        if (bookItem.getLibrary() != null) {
            this.libraryId = bookItem.getLibrary().getId();
            this.libraryName = bookItem.getLibrary().getName();
        }
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getBookTitleId() { return bookTitleId; }
    public void setBookTitleId(Integer bookTitleId) { this.bookTitleId = bookTitleId; }

    public String getBookTitleTitle() { return bookTitleTitle; }
    public void setBookTitleTitle(String bookTitleTitle) { this.bookTitleTitle = bookTitleTitle; }

    public String getBookTitleAuthor() { return bookTitleAuthor; }
    public void setBookTitleAuthor(String bookTitleAuthor) { this.bookTitleAuthor = bookTitleAuthor; }

    public String getBookTitleIsbn() { return bookTitleIsbn; }
    public void setBookTitleIsbn(String bookTitleIsbn) { this.bookTitleIsbn = bookTitleIsbn; }

    public Integer getLibraryId() { return libraryId; }
    public void setLibraryId(Integer libraryId) { this.libraryId = libraryId; }

    public String getLibraryName() { return libraryName; }
    public void setLibraryName(String libraryName) { this.libraryName = libraryName; }

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

    @Override
    public String toString() {
        return "BookItemResponse{" +
                "id=" + id +
                ", itemCode='" + itemCode + '\'' +
                ", bookTitleTitle='" + bookTitleTitle + '\'' +
                ", libraryName='" + libraryName + '\'' +
                ", status='" + status + '\'' +
                ", condition='" + condition + '\'' +
                ", shelfLocation='" + shelfLocation + '\'' +
                '}';
    }
}
