// File: src/main/java/com/library/dto/request/BookItemRequest.java
package com.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BookItemRequest {
    
    @NotNull(message = "Book title ID is required")
    private Integer bookTitleId;
    
    @NotNull(message = "Library ID is required")
    private Integer libraryId;
    
    @NotBlank(message = "Item code is required")
    @Size(max = 50, message = "Item code must not exceed 50 characters")
    private String itemCode;
    
    @Size(max = 100, message = "Shelf location must not exceed 100 characters")
    private String shelfLocation;
    
    @NotBlank(message = "Condition is required")
    @Size(max = 20, message = "Condition must not exceed 20 characters")
    private String condition = "GOOD"; // EXCELLENT, GOOD, FAIR, DAMAGED
    
    @NotBlank(message = "Status is required")
    @Size(max = 20, message = "Status must not exceed 20 characters")
    private String status = "AVAILABLE"; // AVAILABLE, BORROWED, RESERVED, LOST, MAINTENANCE
    
    @NotNull(message = "Acquisition date is required")
    private LocalDate acquisitionDate;
    
    @DecimalMin(value = "0.0", message = "Acquisition cost must be non-negative")
    private BigDecimal acquisitionCost;
    
    private String notes;

    // Constructors
    public BookItemRequest() {}

    public BookItemRequest(Integer bookTitleId, Integer libraryId, String itemCode) {
        this.bookTitleId = bookTitleId;
        this.libraryId = libraryId;
        this.itemCode = itemCode;
        this.acquisitionDate = LocalDate.now();
    }

    // Getters and Setters
    public Integer getBookTitleId() { return bookTitleId; }
    public void setBookTitleId(Integer bookTitleId) { this.bookTitleId = bookTitleId; }

    public Integer getLibraryId() { return libraryId; }
    public void setLibraryId(Integer libraryId) { this.libraryId = libraryId; }

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

    @Override
    public String toString() {
        return "BookItemRequest{" +
                "bookTitleId=" + bookTitleId +
                ", libraryId=" + libraryId +
                ", itemCode='" + itemCode + '\'' +
                ", status='" + status + '\'' +
                ", condition='" + condition + '\'' +
                ", acquisitionDate=" + acquisitionDate +
                '}';
    }
}
