package com.library.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class BorrowRequestRequest {

    @NotNull(message = "Book item ID is required")
    private Integer bookItemId;

    @NotNull(message = "Requested borrow date is required")
    private LocalDate requestedBorrowDate;

    @NotNull(message = "Requested return date is required")
    @Future(message = "Requested return date must be in the future")
    private LocalDate requestedReturnDate;

    @Size(max = 1000, message = "Request reason must not exceed 1000 characters")
    private String requestReason;

    // Constructors
    public BorrowRequestRequest() {}

    public BorrowRequestRequest(Integer bookItemId, LocalDate requestedBorrowDate, 
                               LocalDate requestedReturnDate, String requestReason) {
        this.bookItemId = bookItemId;
        this.requestedBorrowDate = requestedBorrowDate;
        this.requestedReturnDate = requestedReturnDate;
        this.requestReason = requestReason;
    }

    // Getters and Setters
    public Integer getBookItemId() {
        return bookItemId;
    }

    public void setBookItemId(Integer bookItemId) {
        this.bookItemId = bookItemId;
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

    @Override
    public String toString() {
        return "BorrowRequestRequest{" +
                "bookItemId=" + bookItemId +
                ", requestedBorrowDate=" + requestedBorrowDate +
                ", requestedReturnDate=" + requestedReturnDate +
                ", requestReason='" + requestReason + '\'' +
                '}';
    }
}
