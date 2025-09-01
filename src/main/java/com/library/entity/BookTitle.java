// File: src/main/java/com/library/entity/Book.java
package com.library.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "book_titles")
public class BookTitle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 20, unique = true)
    private String isbn;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, length = 300)
    private String author;

    @Column(length = 200)
    private String publisher;

    @Column(name = "published_year")
    private Integer publishedYear;

    @Column(length = 100)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image", length = 500)
    private String coverImage;

    @Column(length = 10)
    private String language = "vi";

    @Column(name = "total_pages")
    private Integer totalPages;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // One-to-Many với BookItem (book_items)
    @OneToMany(mappedBy = "bookTitle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookItem> bookItems;

    // Constructors
    public BookTitle() {
        this.createdAt = LocalDateTime.now();
    }

    public BookTitle(String title, String author) {
        this();
        this.title = title;
        this.author = author;
    }

    public BookTitle(String title, String author, String isbn, String publisher) {
        this();
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publisher = publisher;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public Integer getPublishedYear() { return publishedYear; }
    public void setPublishedYear(Integer publishedYear) { this.publishedYear = publishedYear; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Integer getTotalPages() { return totalPages; }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<BookItem> getBookItems() { return bookItems; }
    public void setBookItems(List<BookItem> bookItems) { this.bookItems = bookItems; }

    // Utility methods
    public int getTotalCopies() {
        return bookItems != null ? bookItems.size() : 0;
    }

    public long getAvailableCopies() {
        if (bookItems == null) return 0;
        return bookItems.stream()
                .filter(item -> "AVAILABLE".equals(item.getStatus()))
                .count();
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", isbn='" + isbn + '\'' +
                ", publisher='" + publisher + '\'' +
                ", publishedYear=" + publishedYear +
                '}';
    }
}