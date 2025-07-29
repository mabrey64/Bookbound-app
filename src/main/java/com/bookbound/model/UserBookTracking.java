package com.bookbound.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

/**
 * UserBookTracking entity representing a user's reading progress for a specific book.
 * This is the junction entity between User and Book with additional tracking data.
 */
@Entity
@Table(name = "user_book_tracking")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBookTracking {
    
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
    
    @Column(nullable = false)
    private Integer currentPage = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReadingStatus status = ReadingStatus.TO_READ;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "finished_at")
    private LocalDateTime finishedAt;
    
    // Convenience method to start reading
    public void startReading() {
        this.status = ReadingStatus.READING;
        this.startedAt = LocalDateTime.now();
        if (this.currentPage == 0) {
            this.currentPage = 1;
        }
    }
    
    // Convenience method to finish reading
    public void finishReading() {
        this.status = ReadingStatus.COMPLETED;
        this.finishedAt = LocalDateTime.now();
        if (this.book != null) {
            this.currentPage = this.book.getTotalPages();
        }
    }
    
    // Convenience method to calculate reading progress percentage
    public double getProgressPercentage() {
        if (book == null || book.getTotalPages() == null || book.getTotalPages() == 0) {
            return 0.0;
        }
        return (double) currentPage / book.getTotalPages() * 100.0;
    }
} 