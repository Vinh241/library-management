package com.library.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "borrowing_transactions")
public class BorrowingTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "borrow_request_id", nullable = false, unique = true)
    private BorrowRequest borrowRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_item_id", nullable = false)
    private BookItem bookItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrower_id", nullable = false)
    private Account borrower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "librarian_id", nullable = false)
    private Account librarian;

    @Column(name = "borrow_date")
    private LocalDate borrowDate = LocalDate.now();

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "returned_to")
    private Account returnedTo;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private TransactionStatus status = TransactionStatus.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public BorrowingTransaction() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public BorrowingTransaction(BorrowRequest borrowRequest, Account librarian, LocalDate dueDate) {
        this();
        this.borrowRequest = borrowRequest;
        this.bookItem = borrowRequest.getBookItem();
        this.borrower = borrowRequest.getRequester();
        this.librarian = librarian;
        this.dueDate = dueDate;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BorrowRequest getBorrowRequest() {
        return borrowRequest;
    }

    public void setBorrowRequest(BorrowRequest borrowRequest) {
        this.borrowRequest = borrowRequest;
    }

    public BookItem getBookItem() {
        return bookItem;
    }

    public void setBookItem(BookItem bookItem) {
        this.bookItem = bookItem;
    }

    public Account getBorrower() {
        return borrower;
    }

    public void setBorrower(Account borrower) {
        this.borrower = borrower;
    }

    public Account getLibrarian() {
        return librarian;
    }

    public void setLibrarian(Account librarian) {
        this.librarian = librarian;
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

    public Account getReturnedTo() {
        return returnedTo;
    }

    public void setReturnedTo(Account returnedTo) {
        this.returnedTo = returnedTo;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
    public boolean isActive() {
        return status == TransactionStatus.ACTIVE;
    }

    public boolean isReturned() {
        return status == TransactionStatus.RETURNED;
    }

    public boolean isOverdue() {
        return status == TransactionStatus.OVERDUE || 
               (status == TransactionStatus.ACTIVE && LocalDate.now().isAfter(dueDate));
    }

    public boolean isLost() {
        return status == TransactionStatus.LOST;
    }

    public void markAsReturned(Account returnedTo, String notes) {
        this.status = TransactionStatus.RETURNED;
        this.returnDate = LocalDate.now();
        this.returnedTo = returnedTo;
        this.notes = notes;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsOverdue() {
        this.status = TransactionStatus.OVERDUE;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsLost(String notes) {
        this.status = TransactionStatus.LOST;
        this.notes = notes;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "BorrowingTransaction{" +
                "id=" + id +
                ", borrowDate=" + borrowDate +
                ", dueDate=" + dueDate +
                ", returnDate=" + returnDate +
                ", status=" + status +
                '}';
    }

    public enum TransactionStatus {
        ACTIVE, RETURNED, OVERDUE, LOST
    }
}
