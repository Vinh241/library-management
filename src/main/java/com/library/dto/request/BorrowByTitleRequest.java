package com.library.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class BorrowByTitleRequest {

    @NotNull(message = "Book title ID is required")
    private Integer bookTitleId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Requested borrow date is required")
    private LocalDate requestedBorrowDate;

    @NotNull(message = "Requested return date is required")
    @Future(message = "Requested return date must be in the future")
    private LocalDate requestedReturnDate;

    @Size(max = 1000, message = "Request reason must not exceed 1000 characters")
    private String requestReason;

    public BorrowByTitleRequest() {}

    public Integer getBookTitleId() {
        return bookTitleId;
    }

    public void setBookTitleId(Integer bookTitleId) {
        this.bookTitleId = bookTitleId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDate getRequestedBorrowDate() {
        return requestedBorrowDate;
    }

    public void setRequestedBorrowDate(LocalDate requestedBorrowDate) {
        this.requestedBorrowDate = requestedBorrowDate;
    }

    public LocalDate getRequestedReturnDate() {
        return requestedReturnDate;
    }

    public void setRequestedReturnDate(LocalDate requestedReturnDate) {
        this.requestedReturnDate = requestedReturnDate;
    }

    public String getRequestReason() {
        return requestReason;
    }

    public void setRequestReason(String requestReason) {
        this.requestReason = requestReason;
    }
}


