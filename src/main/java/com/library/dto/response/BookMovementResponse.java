package com.library.dto.response;

import com.library.entity.BookMovement;
import java.time.LocalDateTime;

public class BookMovementResponse {

    private Integer id;
    private Integer bookItemId;
    private String bookItemCode;
    private String bookTitle;
    private String bookAuthor;
    private Integer transactionId;
    private String action;
    private String oldStatus;
    private String newStatus;
    private String oldLocation;
    private String newLocation;
    private Integer performedById;
    private String performedByName;
    private String performedByRole;
    private LocalDateTime performedAt;
    private String notes;

    // Constructors
    public BookMovementResponse() {}

    public BookMovementResponse(BookMovement movement) {
        this.id = movement.getId();
        this.bookItemId = movement.getBookItem().getId();
        this.bookItemCode = movement.getBookItem().getItemCode();
        this.bookTitle = movement.getBookItem().getBookTitle().getTitle();
        this.bookAuthor = movement.getBookItem().getBookTitle().getAuthor();
        this.transactionId = movement.getTransaction() != null ? movement.getTransaction().getId() : null;
        this.action = movement.getAction().toString();
        this.oldStatus = movement.getOldStatus();
        this.newStatus = movement.getNewStatus();
        this.oldLocation = movement.getOldLocation();
        this.newLocation = movement.getNewLocation();
        this.performedById = movement.getPerformedBy().getId();
        this.performedByName = movement.getPerformedBy().getFullName();
        this.performedByRole = movement.getPerformedBy().getRole().toString();
        this.performedAt = movement.getPerformedAt();
        this.notes = movement.getNotes();
    }

    // Static factory method for safe creation
    public static BookMovementResponse fromEntity(BookMovement movement) {
        BookMovementResponse response = new BookMovementResponse();
        response.setId(movement.getId());
        
        // Safely get book item info
        if (movement.getBookItem() != null) {
            response.setBookItemId(movement.getBookItem().getId());
            response.setBookItemCode(movement.getBookItem().getItemCode());
            
            // Safely get book title info
            if (movement.getBookItem().getBookTitle() != null) {
                response.setBookTitle(movement.getBookItem().getBookTitle().getTitle());
                response.setBookAuthor(movement.getBookItem().getBookTitle().getAuthor());
            }
        }
        
        // Safely get transaction info
        if (movement.getTransaction() != null) {
            response.setTransactionId(movement.getTransaction().getId());
        }
        
        response.setAction(movement.getAction().toString());
        response.setOldStatus(movement.getOldStatus());
        response.setNewStatus(movement.getNewStatus());
        response.setOldLocation(movement.getOldLocation());
        response.setNewLocation(movement.getNewLocation());
        
        // Safely get performer info
        if (movement.getPerformedBy() != null) {
            response.setPerformedById(movement.getPerformedBy().getId());
            response.setPerformedByName(movement.getPerformedBy().getFullName());
            response.setPerformedByRole(movement.getPerformedBy().getRole().toString());
        }
        
        response.setPerformedAt(movement.getPerformedAt());
        response.setNotes(movement.getNotes());
        
        return response;
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

    public String getBookItemCode() {
        return bookItemCode;
    }

    public void setBookItemCode(String bookItemCode) {
        this.bookItemCode = bookItemCode;
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

    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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

    public Integer getPerformedById() {
        return performedById;
    }

    public void setPerformedById(Integer performedById) {
        this.performedById = performedById;
    }

    public String getPerformedByName() {
        return performedByName;
    }

    public void setPerformedByName(String performedByName) {
        this.performedByName = performedByName;
    }

    public String getPerformedByRole() {
        return performedByRole;
    }

    public void setPerformedByRole(String performedByRole) {
        this.performedByRole = performedByRole;
    }

    public LocalDateTime getPerformedAt() {
        return performedAt;
    }

    public void setPerformedAt(LocalDateTime performedAt) {
        this.performedAt = performedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "BookMovementResponse{" +
                "id=" + id +
                ", bookItemCode='" + bookItemCode + '\'' +
                ", action='" + action + '\'' +
                ", performedByName='" + performedByName + '\'' +
                ", performedAt=" + performedAt +
                '}';
    }
}
