// File: src/main/java/com/library/dto/request/BookTitleRequest.java
package com.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BookTitleRequest {
    
    @Size(max = 20, message = "ISBN must not exceed 20 characters")
    private String isbn;
    
    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;
    
    @NotBlank(message = "Author is required")
    @Size(max = 300, message = "Author must not exceed 300 characters")
    private String author;
    
    @Size(max = 200, message = "Publisher must not exceed 200 characters")
    private String publisher;
    
    private Integer publishedYear;
    
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;
    
    private String description;
    
    @Size(max = 500, message = "Cover image URL must not exceed 500 characters")
    private String coverImage;
    
    @Size(max = 10, message = "Language must not exceed 10 characters")
    private String language = "vi";
    
    private Integer totalPages;

    // Constructors
    public BookTitleRequest() {}

    public BookTitleRequest(String title, String author) {
        this.title = title;
        this.author = author;
    }

    // Getters and Setters
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

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

    @Override
    public String toString() {
        return "BookTitleRequest{" +
                "isbn='" + isbn + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", publisher='" + publisher + '\'' +
                ", publishedYear=" + publishedYear +
                ", category='" + category + '\'' +
                '}';
    }
}
