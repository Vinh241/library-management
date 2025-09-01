package com.library.dto.response;

import com.library.entity.BorrowRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class BorrowRequestResponse {

    private Integer id;
    private Integer bookItemId;
    private String bookTitle;
    private String bookAuthor;
    private String bookItemCode;
    private Integer requesterId;
    private String requesterName;
    private String requesterEmail;
    private LocalDate requestedBorrowDate;
    private LocalDate requestedReturnDate;
    private String requestReason;
    private String status;
    private Integer reviewedById;
    private String reviewedByName;
    private LocalDateTime reviewedAt;
    private String reviewNotes;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public BorrowRequestResponse() {}

    public BorrowRequestResponse(BorrowRequest borrowRequest) {
        this.id = borrowRequest.getId();
        this.bookItemId = borrowRequest.getBookItem().getId();
        this.bookTitle = borrowRequest.getBookItem().getBookTitle().getTitle();
        this.bookAuthor = borrowRequest.getBookItem().getBookTitle().getAuthor();
        this.bookItemCode = borrowRequest.getBookItem().getItemCode();
        this.requesterId = borrowRequest.getRequester().getId();
        this.requesterName = borrowRequest.getRequester().getFullName();
        this.requesterEmail = borrowRequest.getRequester().getEmail();
        this.requestedBorrowDate = borrowRequest.getRequestedBorrowDate();
        this.requestedReturnDate = borrowRequest.getRequestedReturnDate();
        this.requestReason = borrowRequest.getRequestReason();
        this.status = borrowRequest.getStatus().toString();
        
        if (borrowRequest.getReviewedBy() != null) {
            this.reviewedById = borrowRequest.getReviewedBy().getId();
            this.reviewedByName = borrowRequest.getReviewedBy().getFullName();
        }
        
        this.reviewedAt = borrowRequest.getReviewedAt();
        this.reviewNotes = borrowRequest.getReviewNotes();
        this.rejectionReason = borrowRequest.getRejectionReason();
        this.createdAt = borrowRequest.getCreatedAt();
        this.updatedAt = borrowRequest.getUpdatedAt();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBookItemId() {
        return bookItemId;
    }

    public void setBookItemId(Integer bookItemId) {
        this.bookItemId = bookItemId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    public String getBookItemCode() {
        return bookItemCode;
    }

    public void setBookItemCode(String bookItemCode) {
        this.bookItemCode = bookItemCode;
    }

    public Integer getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(Integer requesterId) {
        this.requesterId = requesterId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public String getRequesterEmail() {
        return requesterEmail;
    }

    public void setRequesterEmail(String requesterEmail) {
        this.requesterEmail = requesterEmail;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getReviewedById() {
        return reviewedById;
    }

    public void setReviewedById(Integer reviewedById) {
        this.reviewedById = reviewedById;
    }

    public String getReviewedByName() {
        return reviewedByName;
    }

    public void setReviewedByName(String reviewedByName) {
        this.reviewedByName = reviewedByName;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getReviewNotes() {
        return reviewNotes;
    }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "BorrowRequestResponse{" +
                "id=" + id +
                ", bookTitle='" + bookTitle + '\'' +
                ", requesterName='" + requesterName + '\'' +
                ", status='" + status + '\'' +
                ", requestedBorrowDate=" + requestedBorrowDate +
                ", requestedReturnDate=" + requestedReturnDate +
                '}';
    }
}
