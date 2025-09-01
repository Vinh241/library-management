package com.library.dto.request;

import jakarta.validation.constraints.Size;

public class UpdateBookMovementRequest {

    private Integer transactionId;

    @Size(max = 20, message = "Old status must not exceed 20 characters")
    private String oldStatus;

    @Size(max = 20, message = "New status must not exceed 20 characters")
    private String newStatus;

    @Size(max = 100, message = "Old location must not exceed 100 characters")
    private String oldLocation;

    @Size(max = 100, message = "New location must not exceed 100 characters")
    private String newLocation;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    // Constructors
    public UpdateBookMovementRequest() {}

    public UpdateBookMovementRequest(String notes) {
        this.notes = notes;
    }

    public UpdateBookMovementRequest(String oldStatus, String newStatus, String notes) {
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.notes = notes;
    }

    // Getters and Setters
    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getOldLocation() {
        return oldLocation;
    }

    public void setOldLocation(String oldLocation) {
        this.oldLocation = oldLocation;
    }

    public String getNewLocation() {
        return newLocation;
    }

    public void setNewLocation(String newLocation) {
        this.newLocation = newLocation;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "UpdateBookMovementRequest{" +
                "transactionId=" + transactionId +
                ", oldStatus='" + oldStatus + '\'' +
                ", newStatus='" + newStatus + '\'' +
                ", oldLocation='" + oldLocation + '\'' +
                ", newLocation='" + newLocation + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }
}
