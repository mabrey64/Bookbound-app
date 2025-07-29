package com.bookbound.dto;

import com.bookbound.model.Book;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for series progress tracking response.
 * Contains information about user's progress in a series and recommendations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeriesProgressResponse {
    
    private String seriesName;
    private int totalBooksInSeries;
    private int booksRead;
    private int booksInProgress;
    private Book nextBook;
    private List<Book> genreSuggestions;
    private double completionPercentage;
    
    // Convenience methods
    
    /**
     * Check if the user has completed the entire series.
     * @return true if all books in the series have been read
     */
    public boolean isSeriesCompleted() {
        return booksRead >= totalBooksInSeries && totalBooksInSeries > 0;
    }
    
    /**
     * Check if the user has started reading the series.
     * @return true if at least one book has been read or is in progress
     */
    public boolean isSeriesStarted() {
        return booksRead > 0 || booksInProgress > 0;
    }
    
    /**
     * Get the number of books remaining in the series.
     * @return number of unread books in the series
     */
    public int getBooksRemaining() {
        return Math.max(0, totalBooksInSeries - booksRead - booksInProgress);
    }
    
    /**
     * Get a summary message about the series progress.
     * @return human-readable progress summary
     */
    public String getProgressSummary() {
        if (totalBooksInSeries == 0) {
            return "Series not found";
        }
        
        if (isSeriesCompleted()) {
            return String.format("Series completed! You've read all %d books.", totalBooksInSeries);
        }
        
        if (!isSeriesStarted()) {
            return String.format("Series not started. %d books available to read.", totalBooksInSeries);
        }
        
        return String.format("Progress: %d/%d books read (%.1f%% complete)", 
                           booksRead, totalBooksInSeries, completionPercentage);
    }
} 