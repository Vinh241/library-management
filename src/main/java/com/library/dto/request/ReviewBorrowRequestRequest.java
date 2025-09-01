package com.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ReviewBorrowRequestRequest {

    @NotNull(message = "Decision is required")
    private Boolean approved; // true for approve, false for reject

    @Size(max = 1000, message = "Review notes must not exceed 1000 characters")
    private String reviewNotes;

    @Size(max = 500, message = "Rejection reason must not exceed 500 characters")
    private String rejectionReason; // Required only when approved = false

    // Constructors
    public ReviewBorrowRequestRequest() {}

    public ReviewBorrowRequestRequest(Boolean approved, String reviewNotes, String rejectionReason) {
        this.approved = approved;
        this.reviewNotes = reviewNotes;
        this.rejectionReason = rejectionReason;
    }

    // Getters and Setters
    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
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

    @Override
    public String toString() {
        return "ReviewBorrowRequestRequest{" +
                "approved=" + approved +
                ", reviewNotes='" + reviewNotes + '\'' +
                ", rejectionReason='" + rejectionReason + '\'' +
                '}';
    }
}
