package com.bookbound.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SearchFilter DTO for handling book search requests.
 * This is not a JPA entity, just a plain POJO for receiving search parameters.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchFilter {
    
    private String title;
    private String author;
    private String genre;
    private Integer minPages;
    private Integer maxPages;
    private String seriesName;
    
    // Convenience method to check if filter has any criteria
    public boolean isEmpty() {
        return (title == null || title.trim().isEmpty()) &&
               (author == null || author.trim().isEmpty()) &&
               (genre == null || genre.trim().isEmpty()) &&
               (seriesName == null || seriesName.trim().isEmpty()) &&
               minPages == null &&
               maxPages == null;
    }
    
    // Convenience method to validate page range
    public boolean isValidPageRange() {
        if (minPages == null && maxPages == null) {
            return true;
        }
        if (minPages != null && minPages < 0) {
            return false;
        }
        if (maxPages != null && maxPages < 0) {
            return false;
        }
        if (minPages != null && maxPages != null) {
            return minPages <= maxPages;
        }
        return true;
    }
} 