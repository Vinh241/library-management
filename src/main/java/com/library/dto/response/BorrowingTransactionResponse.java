package com.library.dto.response;

import com.library.entity.BorrowingTransaction;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class BorrowingTransactionResponse {
    
    private Integer id;
    private Integer borrowRequestId;
    private Integer bookItemId;
    private String bookTitle;
    private String bookIsbn;
    private String bookItemCode;
    private Integer borrowerId;
    private String borrowerName;
    private String borrowerEmail;
    private Integer librarianId;
    private String librarianName;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private Integer returnedToId;
    private String returnedToName;
    private String status;
    private String notes;
    private String libraryName;
    private Integer libraryId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isOverdue;

    // Constructors
    public BorrowingTransactionResponse() {}

    public BorrowingTransactionResponse(BorrowingTransaction transaction) {
        this.id = transaction.getId();
        this.borrowRequestId = transaction.getBorrowRequest().getId();
        this.bookItemId = transaction.getBookItem().getId();
        this.bookTitle = transaction.getBookItem().getBookTitle().getTitle();
        this.bookIsbn = transaction.getBookItem().getBookTitle().getIsbn();
        this.bookItemCode = transaction.getBookItem().getItemCode();
        this.borrowerId = transaction.getBorrower().getId();
        this.borrowerName = transaction.getBorrower().getFullName();
        this.borrowerEmail = transaction.getBorrower().getEmail();
        this.librarianId = transaction.getLibrarian().getId();
        this.librarianName = transaction.getLibrarian().getFullName();
        this.borrowDate = transaction.getBorrowDate();
        this.dueDate = transaction.getDueDate();
        this.returnDate = transaction.getReturnDate();
        if (transaction.getReturnedTo() != null) {
            this.returnedToId = transaction.getReturnedTo().getId();
            this.returnedToName = transaction.getReturnedTo().getFullName();
        }
        this.status = transaction.getStatus().toString();
        this.notes = transaction.getNotes();
        this.libraryName = transaction.getBookItem().getLibrary().getName();
        this.libraryId = transaction.getBookItem().getLibrary().getId();
        this.createdAt = transaction.getCreatedAt();
        this.updatedAt = transaction.getUpdatedAt();
        this.isOverdue = transaction.isOverdue();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBorrowRequestId() {
        return borrowRequestId;
    }

    public void setBorrowRequestId(Integer borrowRequestId) {
        this.borrowRequestId = borrowRequestId;
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

    public String getBookIsbn() {
        return bookIsbn;
    }

    public void setBookIsbn(String bookIsbn) {
        this.bookIsbn = bookIsbn;
    }

    public String getBookItemCode() {
        return bookItemCode;
    }

    public void setBookItemCode(String bookItemCode) {
        this.bookItemCode = bookItemCode;
    }

    public Integer getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerId(Integer borrowerId) {
        this.borrowerId = borrowerId;
    }

    public String getBorrowerName() {
        return borrowerName;
    }

    public void setBorrowerName(String borrowerName) {
        this.borrowerName = borrowerName;
    }

    public String getBorrowerEmail() {
        return borrowerEmail;
    }

    public void setBorrowerEmail(String borrowerEmail) {
        this.borrowerEmail = borrowerEmail;
    }

    public Integer getLibrarianId() {
        return librarianId;
    }

    public void setLibrarianId(Integer librarianId) {
        this.librarianId = librarianId;
    }

    public String getLibrarianName() {
        return librarianName;
    }

    public void setLibrarianName(String librarianName) {
        this.librarianName = librarianName;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public Integer getReturnedToId() {
        return returnedToId;
    }

    public void setReturnedToId(Integer returnedToId) {
        this.returnedToId = returnedToId;
    }

    public String getReturnedToName() {
        return returnedToName;
    }

    public void setReturnedToName(String returnedToName) {
        this.returnedToName = returnedToName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getLibraryName() {
        return libraryName;
    }

    public void setLibraryName(String libraryName) {
        this.libraryName = libraryName;
    }

    public Integer getLibraryId() {
        return libraryId;
    }

    public void setLibraryId(Integer libraryId) {
        this.libraryId = libraryId;
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

    public boolean isOverdue() {
        return isOverdue;
    }

    public void setOverdue(boolean overdue) {
        isOverdue = overdue;
    }
}
