package com.library.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "book_movements")
public class BookMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_item_id", nullable = false)
    private BookItem bookItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private BorrowingTransaction transaction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MovementAction action;

    @Column(name = "old_status", length = 20)
    private String oldStatus;

    @Column(name = "new_status", length = 20)
    private String newStatus;

    @Column(name = "old_location", length = 100)
    private String oldLocation;

    @Column(name = "new_location", length = 100)
    private String newLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by", nullable = false)
    private Account performedBy;

    @Column(name = "performed_at")
    private LocalDateTime performedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Constructors
    public BookMovement() {
        this.performedAt = LocalDateTime.now();
    }

    public BookMovement(BookItem bookItem, MovementAction action, Account performedBy) {
        this();
        this.bookItem = bookItem;
        this.action = action;
        this.performedBy = performedBy;
    }

    public BookMovement(BookItem bookItem, BorrowingTransaction transaction, MovementAction action, 
                       String oldStatus, String newStatus, Account performedBy, String notes) {
        this();
        this.bookItem = bookItem;
        this.transaction = transaction;
        this.action = action;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.performedBy = performedBy;
        this.notes = notes;
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

    public BorrowingTransaction getTransaction() {
        return transaction;
    }

    public void setTransaction(BorrowingTransaction transaction) {
        this.transaction = transaction;
    }

    public MovementAction getAction() {
        return action;
    }

    public void setAction(MovementAction action) {
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

    public Account getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(Account performedBy) {
        this.performedBy = performedBy;
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
        return "BookMovement{" +
                "id=" + id +
                ", action=" + action +
                ", oldStatus='" + oldStatus + '\'' +
                ", newStatus='" + newStatus + '\'' +
                ", performedAt=" + performedAt +
                '}';
    }

    public enum MovementAction {
        RESERVED,    // Sách được đặt chỗ
        BORROWED,    // Sách được mượn
        RETURNED,    // Sách được trả
        MOVED,       // Sách được chuyển vị trí
        LOST,        // Sách bị mất
        FOUND,       // Sách được tìm thấy
        DAMAGED      // Sách bị hỏng
    }
}
