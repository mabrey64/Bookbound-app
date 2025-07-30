package com.bookbound.service;

import com.bookbound.dto.SeriesProgressResponse;
import com.bookbound.model.Book;
import com.bookbound.model.ReadingStatus;
import com.bookbound.model.User;
import com.bookbound.model.UserBookTracking;
import com.bookbound.repository.BookRepository;
import com.bookbound.repository.UserBookTrackingRepository;
import com.bookbound.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service class for UserBookTracking entity business logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingService {
    
    private final UserBookTrackingRepository trackingRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    
    /**
     * Add a book to user's reading list.
     * @param userId the user ID
     * @param bookId the book ID
     * @param status the initial reading status
     * @param toReadNext whether to mark as to read next
     * @return the created tracking entry
     * @throws IllegalArgumentException if user/book not found or already tracking
     */
    public UserBookTracking addBookToUserList(String userId, String bookId, ReadingStatus status, boolean toReadNext) {
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
        tracking.setToReadNext(toReadNext);
        
        if (status == ReadingStatus.READING) {
            tracking.startReading();
        }
        
        return trackingRepository.save(tracking);
    }
    
    /**
     * Add a book to user's reading list (overloaded method for backward compatibility).
     * @param userId the user ID
     * @param bookId the book ID
     * @param status the initial reading status
     * @return the created tracking entry
     * @throws IllegalArgumentException if user/book not found or already tracking
     */
    public UserBookTracking addBookToUserList(String userId, String bookId, ReadingStatus status) {
        return addBookToUserList(userId, bookId, status, false);
    }
    
    /**
     * Get series progress for a user.
     * @param userId the user ID
     * @param seriesName the series name
     * @return series progress information with recommendations
     * @throws IllegalArgumentException if user not found
     */
    public SeriesProgressResponse getSeriesProgress(String userId, String seriesName) {
        log.info("Getting series progress for user {} and series {}", userId, seriesName);
        
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }
        
        // Get all books in the series, ordered by series order
        List<Book> seriesBooks = bookRepository.findBySeriesNameOrderBySeriesOrder(seriesName);
        
        if (seriesBooks.isEmpty()) {
            log.warn("No books found for series: {}", seriesName);
            return createEmptySeriesResponse(seriesName);
        }
        
        // Get user's tracking entries for this series
        List<UserBookTracking> userTrackingList = trackingRepository.findByUserId(userId);
        Set<String> userBookIds = userTrackingList.stream()
            .map(tracking -> tracking.getBook().getId())
            .collect(Collectors.toSet());
        
        // Categorize user's progress in this series
        List<Book> completedBooks = seriesBooks.stream()
            .filter(book -> userTrackingList.stream()
                .anyMatch(tracking -> tracking.getBook().getId().equals(book.getId()) && 
                         tracking.getStatus() == ReadingStatus.COMPLETED))
            .toList();
        
        List<Book> inProgressBooks = seriesBooks.stream()
            .filter(book -> userTrackingList.stream()
                .anyMatch(tracking -> tracking.getBook().getId().equals(book.getId()) && 
                         tracking.getStatus() == ReadingStatus.READING))
            .toList();
        
        // Find the next book to read in the series
        Book nextBook = findNextBookInSeries(seriesBooks, completedBooks, inProgressBooks);
        
        // Get genre suggestions if series is completed or user wants variety
        List<Book> genreSuggestions = getGenreSuggestions(userId, seriesBooks.get(0).getGenre(), userBookIds);
        
        // Calculate completion percentage
        double completionPercentage = seriesBooks.isEmpty() ? 0.0 : 
            (double) completedBooks.size() / seriesBooks.size() * 100.0;
        
        SeriesProgressResponse response = new SeriesProgressResponse(
            seriesName,
            seriesBooks.size(),
            completedBooks.size(),
            inProgressBooks.size(),
            nextBook,
            genreSuggestions,
            completionPercentage
        );
        
        log.info("Series progress for {}: {} books read out of {} total", 
                seriesName, completedBooks.size(), seriesBooks.size());
        
        return response;
    }
    
    /**
     * Get reading recommendations for a user based on their reading history.
     * @param userId the user ID
     * @param limit maximum number of recommendations
     * @return list of recommended books
     */
    public List<Book> getReadingRecommendations(String userId, int limit) {
        log.info("Getting reading recommendations for user {}", userId);
        
        List<UserBookTracking> userTrackingList = trackingRepository.findByUserId(userId);
        
        if (userTrackingList.isEmpty()) {
            // New user - recommend popular books from various genres
            return bookRepository.findAll().stream()
                .limit(limit)
                .toList();
        }
        
        // Get user's read book IDs
        Set<String> readBookIds = userTrackingList.stream()
            .map(tracking -> tracking.getBook().getId())
            .collect(Collectors.toSet());
        
        // Get user's favorite genres based on completed books
        List<String> favoriteGenres = userTrackingList.stream()
            .filter(tracking -> tracking.getStatus() == ReadingStatus.COMPLETED)
            .map(tracking -> tracking.getBook().getGenre())
            .collect(Collectors.groupingBy(genre -> genre, Collectors.counting()))
            .entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .map(entry -> entry.getKey())
            .limit(3)
            .toList();
        
        // Get recommendations from favorite genres
        return favoriteGenres.stream()
            .flatMap(genre -> bookRepository.findByGenre(genre).stream())
            .filter(book -> !readBookIds.contains(book.getId()))
            .distinct()
            .limit(limit)
            .toList();
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
    
    /**
     * Mark or unmark a book as "to read next".
     * @param trackingId the tracking entry ID
     * @param toReadNext whether to mark as to read next
     * @return the updated tracking entry
     * @throws IllegalArgumentException if tracking entry not found
     */
    public UserBookTracking setToReadNext(String trackingId, boolean toReadNext) {
        UserBookTracking tracking = trackingRepository.findById(trackingId)
            .orElseThrow(() -> new IllegalArgumentException("Tracking entry not found with id: " + trackingId));
        
        tracking.setToReadNext(toReadNext);
        return trackingRepository.save(tracking);
    }
    
    /**
     * Get books marked as "to read next" for a user.
     * @param userId the user ID
     * @return list of books marked as to read next
     */
    public List<UserBookTracking> getBooksMarkedToReadNext(String userId) {
        return trackingRepository.findByUserIdAndToReadNextTrue(userId);
    }
    
    // Private helper methods
    
    /**
     * Create an empty series response for non-existent series.
     * @param seriesName the series name
     * @return empty series progress response
     */
    private SeriesProgressResponse createEmptySeriesResponse(String seriesName) {
        return new SeriesProgressResponse(seriesName, 0, 0, 0, null, List.of(), 0.0);
    }
    
    /**
     * Find the next book to read in a series.
     * @param seriesBooks all books in the series
     * @param completedBooks books the user has completed
     * @param inProgressBooks books the user is currently reading
     * @return the next book to read, or null if series is completed
     */
    private Book findNextBookInSeries(List<Book> seriesBooks, List<Book> completedBooks, List<Book> inProgressBooks) {
        // If user hasn't started the series, return the first book
        if (completedBooks.isEmpty() && inProgressBooks.isEmpty()) {
            return seriesBooks.get(0);
        }
        
        // If there are books in progress, don't recommend a new one
        if (!inProgressBooks.isEmpty()) {
            return null;
        }
        
        // Find the highest series order the user has completed
        int maxCompletedOrder = completedBooks.stream()
            .mapToInt(Book::getSeriesOrder)
            .max()
            .orElse(0);
        
        // Find the next book in order
        return seriesBooks.stream()
            .filter(book -> book.getSeriesOrder() != null && book.getSeriesOrder() > maxCompletedOrder)
            .min((b1, b2) -> Integer.compare(b1.getSeriesOrder(), b2.getSeriesOrder()))
            .orElse(null);
    }
    
    /**
     * Get genre-based book suggestions for a user.
     * @param userId the user ID
     * @param genre the genre to get suggestions from
     * @param excludeBookIds book IDs to exclude from suggestions
     * @return list of suggested books
     */
    private List<Book> getGenreSuggestions(String userId, String genre, Set<String> excludeBookIds) {
        return bookRepository.findByGenre(genre).stream()
            .filter(book -> !excludeBookIds.contains(book.getId()))
            .limit(5)
            .toList();
    }
} 