package com.bookbound.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for external book data from Google Books API.
 * This is not a JPA entity - only used for API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalBookDto {
    
    private String title;
    private List<String> authors;
    private String description;
    private List<String> categories;
    private Integer pageCount;
    private String publisher;
    private String previewLink;
    private String thumbnailUrl;
    private String publishedDate;
    private String language;
    
    // Convenience method to get primary author
    public String getPrimaryAuthor() {
        return (authors != null && !authors.isEmpty()) ? authors.get(0) : "Unknown Author";
    }
    
    // Convenience method to get primary category/genre
    public String getPrimaryCategory() {
        return (categories != null && !categories.isEmpty()) ? categories.get(0) : "Unknown";
    }
    
    // Convenience method to get authors as comma-separated string
    public String getAuthorsAsString() {
        return (authors != null) ? String.join(", ", authors) : "Unknown Author";
    }
    
    // Convenience method to get categories as comma-separated string
    public String getCategoriesAsString() {
        return (categories != null) ? String.join(", ", categories) : "Unknown";
    }
    
    // Convenience method to check if book has valid data
    public boolean isValid() {
        return title != null && !title.trim().isEmpty() && 
               authors != null && !authors.isEmpty();
    }
} 