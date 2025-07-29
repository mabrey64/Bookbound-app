package com.bookbound.service;

import com.bookbound.model.Book;
import com.bookbound.model.ReadingStatus;
import com.bookbound.model.User;
import com.bookbound.model.UserBookTracking;
import com.bookbound.repository.BookRepository;
import com.bookbound.repository.UserBookTrackingRepository;
import com.bookbound.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for UserBookTracking entity business logic.
 */
@Service
@RequiredArgsConstructor
public class TrackingService {
    
    private final UserBookTrackingRepository trackingRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    
    /**
     * Add a book to user's reading list.
     * @param userId the user ID
     * @param bookId the book ID
     * @param status the initial reading status
     * @return the created tracking entry
     * @throws IllegalArgumentException if user/book not found or already tracking
     */
    public UserBookTracking addBookToUserList(String userId, String bookId, ReadingStatus status) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + bookId));
        
        if (trackingRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new IllegalArgumentException("User is already tracking this book");
        }
        
        UserBookTracking tracking = new UserBookTracking();
        tracking.setUser(user);
        tracking.setBook(book);
        tracking.setStatus(status != null ? status : ReadingStatus.TO_READ);
        tracking.setCurrentPage(0);
        
        if (status == ReadingStatus.READING) {
            tracking.startReading();
        }
        
        return trackingRepository.save(tracking);
    }
    
    /**
     * Get all tracking entries for a user.
     * @param userId the user ID
     * @return list of tracking entries
     */
    public List<UserBookTracking> getUserTrackingList(String userId) {
        return trackingRepository.findByUserId(userId);
    }
    
    /**
     * Get tracking entries by user and status.
     * @param userId the user ID
     * @param status the reading status
     * @return list of tracking entries with the specified status
     */
    public List<UserBookTracking> getUserTrackingByStatus(String userId, ReadingStatus status) {
        return trackingRepository.findByUserIdAndStatus(userId, status);
    }
    
    /**
     * Get a specific tracking entry by ID.
     * @param trackingId the tracking entry ID
     * @return Optional containing the tracking entry if found
     */
    public Optional<UserBookTracking> getTrackingById(String trackingId) {
        return trackingRepository.findById(trackingId);
    }
    
    /**
     * Update reading progress.
     * @param trackingId the tracking entry ID
     * @param currentPage the current page number
     * @return the updated tracking entry
     * @throws IllegalArgumentException if tracking entry not found or invalid page
     */
    public UserBookTracking updateProgress(String trackingId, Integer currentPage) {
        UserBookTracking tracking = trackingRepository.findById(trackingId)
            .orElseThrow(() -> new IllegalArgumentException("Tracking entry not found with id: " + trackingId));
        
        if (currentPage < 0) {
            throw new IllegalArgumentException("Current page cannot be negative");
        }
        
        if (currentPage > tracking.getBook().getTotalPages()) {
            throw new IllegalArgumentException("Current page cannot exceed total pages");
        }
        
        tracking.setCurrentPage(currentPage);
        
        // Auto-update status based on progress
        if (currentPage == 0) {
            tracking.setStatus(ReadingStatus.TO_READ);
        } else if (currentPage >= tracking.getBook().getTotalPages()) {
            tracking.finishReading();
        } else if (tracking.getStatus() == ReadingStatus.TO_READ) {
            tracking.startReading();
        }
        
        return trackingRepository.save(tracking);
    }
    
    /**
     * Update reading status.
     * @param trackingId the tracking entry ID
     * @param status the new reading status
     * @return the updated tracking entry
     * @throws IllegalArgumentException if tracking entry not found
     */
    public UserBookTracking updateStatus(String trackingId, ReadingStatus status) {
        UserBookTracking tracking = trackingRepository.findById(trackingId)
            .orElseThrow(() -> new IllegalArgumentException("Tracking entry not found with id: " + trackingId));
        
        ReadingStatus oldStatus = tracking.getStatus();
        tracking.setStatus(status);
        
        // Handle status-specific logic
        switch (status) {
            case READING:
                if (oldStatus == ReadingStatus.TO_READ) {
                    tracking.startReading();
                }
                break;
            case COMPLETED:
                tracking.finishReading();
                break;
            case TO_READ:
                tracking.setStartedAt(null);
                tracking.setFinishedAt(null);
                tracking.setCurrentPage(0);
                break;
        }
        
        return trackingRepository.save(tracking);
    }
    
    /**
     * Mark a book as finished.
     * @param trackingId the tracking entry ID
     * @return the updated tracking entry
     */
    public UserBookTracking markAsFinished(String trackingId) {
        UserBookTracking tracking = trackingRepository.findById(trackingId)
            .orElseThrow(() -> new IllegalArgumentException("Tracking entry not found with id: " + trackingId));
        
        tracking.finishReading();
        return trackingRepository.save(tracking);
    }
    
    /**
     * Start reading a book.
     * @param trackingId the tracking entry ID
     * @return the updated tracking entry
     */
    public UserBookTracking startReading(String trackingId) {
        UserBookTracking tracking = trackingRepository.findById(trackingId)
            .orElseThrow(() -> new IllegalArgumentException("Tracking entry not found with id: " + trackingId));
        
        tracking.startReading();
        return trackingRepository.save(tracking);
    }
    
    /**
     * Remove a book from user's tracking list.
     * @param trackingId the tracking entry ID
     * @throws IllegalArgumentException if tracking entry not found
     */
    public void removeBookFromUserList(String trackingId) {
        if (!trackingRepository.existsById(trackingId)) {
            throw new IllegalArgumentException("Tracking entry not found with id: " + trackingId);
        }
        trackingRepository.deleteById(trackingId);
    }
    
    /**
     * Get reading statistics for a user.
     * @param userId the user ID
     * @return list of status counts
     */
    public List<Object[]> getUserReadingStats(String userId) {
        return trackingRepository.getReadingStatsByUserId(userId);
    }
    
    /**
     * Get books to read next for a user.
     * @param userId the user ID
     * @return list of books marked as TO_READ
     */
    public List<UserBookTracking> getBooksToReadNext(String userId) {
        return trackingRepository.findByUserIdAndStatus(userId, ReadingStatus.TO_READ);
    }
    
    /**
     * Get currently reading books for a user.
     * @param userId the user ID
     * @return list of books currently being read
     */
    public List<UserBookTracking> getCurrentlyReadingBooks(String userId) {
        return trackingRepository.findByUserIdAndStatus(userId, ReadingStatus.READING);
    }
    
    /**
     * Get completed books for a user.
     * @param userId the user ID
     * @return list of completed books
     */
    public List<UserBookTracking> getCompletedBooks(String userId) {
        return trackingRepository.findByUserIdAndStatus(userId, ReadingStatus.COMPLETED);
    }
} 