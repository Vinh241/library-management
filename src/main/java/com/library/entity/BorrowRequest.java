package com.library.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "borrow_requests")
public class BorrowRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_item_id", nullable = false)
    private BookItem bookItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private Account requester;

    @Column(name = "requested_borrow_date", nullable = false)
    private LocalDate requestedBorrowDate;

    @Column(name = "requested_return_date", nullable = false)
    private LocalDate requestedReturnDate;

    @Column(name = "request_reason", columnDefinition = "TEXT")
    private String requestReason;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private BorrowRequestStatus status = BorrowRequestStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private Account reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public BorrowRequest() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public BorrowRequest(BookItem bookItem, Account requester, LocalDate requestedBorrowDate, 
                        LocalDate requestedReturnDate, String requestReason) {
        this();
        this.bookItem = bookItem;
        this.requester = requester;
        this.requestedBorrowDate = requestedBorrowDate;
        this.requestedReturnDate = requestedReturnDate;
        this.requestReason = requestReason;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BookItem getBookItem() {
        return bookItem;
    }

    public void setBookItem(BookItem bookItem) {
        this.bookItem = bookItem;
    }

    public Account getRequester() {
        return requester;
    }

    public void setRequester(Account requester) {
        this.requester = requester;
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

    public BorrowRequestStatus getStatus() {
        return status;
    }

    public void setStatus(BorrowRequestStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public Account getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(Account reviewedBy) {
        this.reviewedBy = reviewedBy;
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

    // Utility methods
    public boolean isPending() {
        return status == BorrowRequestStatus.PENDING;
    }

    public boolean isApproved() {
        return status == BorrowRequestStatus.APPROVED;
    }

    public boolean isRejected() {
        return status == BorrowRequestStatus.REJECTED;
    }

    public boolean isCancelled() {
        return status == BorrowRequestStatus.CANCELLED;
    }

    public boolean isExpired() {
        return status == BorrowRequestStatus.EXPIRED;
    }

    public void approve(Account reviewer, String notes) {
        this.status = BorrowRequestStatus.APPROVED;
        this.reviewedBy = reviewer;
        this.reviewedAt = LocalDateTime.now();
        this.reviewNotes = notes;
        this.updatedAt = LocalDateTime.now();
    }

    public void reject(Account reviewer, String rejectionReason, String notes) {
        this.status = BorrowRequestStatus.REJECTED;
        this.reviewedBy = reviewer;
        this.reviewedAt = LocalDateTime.now();
        this.rejectionReason = rejectionReason;
        this.reviewNotes = notes;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = BorrowRequestStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsExpired() {
        this.status = BorrowRequestStatus.EXPIRED;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "BorrowRequest{" +
                "id=" + id +
                ", status=" + status +
                ", requestedBorrowDate=" + requestedBorrowDate +
                ", requestedReturnDate=" + requestedReturnDate +
                ", createdAt=" + createdAt +
                '}';
    }

    public enum BorrowRequestStatus {
        PENDING, APPROVED, REJECTED, CANCELLED, EXPIRED
    }
}
