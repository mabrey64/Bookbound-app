package com.bookbound.controller;

import com.bookbound.model.ReadingStatus;
import com.bookbound.model.UserBookTracking;
import com.bookbound.service.TrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for UserBookTracking operations.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TrackingController {
    
    private final TrackingService trackingService;
    
    /**
     * Add a book to user's reading list.
     * POST /api/users/{userId}/track
     * @param userId the user ID
     * @param request the tracking request containing bookId and optional status
     * @return the created tracking entry with 201 status
     */
    @PostMapping("/users/{userId}/track")
    public ResponseEntity<UserBookTracking> addBookToUserList(
            @PathVariable String userId,
            @RequestBody TrackingRequest request) {
        try {
            UserBookTracking tracking = trackingService.addBookToUserList(
                userId, 
                request.getBookId(), 
                request.getStatus()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(tracking);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get all tracked books for a user.
     * GET /api/users/{userId}/track
     * @param userId the user ID
     * @param status optional status filter
     * @return list of tracking entries
     */
    @GetMapping("/users/{userId}/track")
    public ResponseEntity<List<UserBookTracking>> getUserTrackingList(
            @PathVariable String userId,
            @RequestParam(required = false) ReadingStatus status) {
        
        List<UserBookTracking> trackingList;
        if (status != null) {
            trackingList = trackingService.getUserTrackingByStatus(userId, status);
        } else {
            trackingList = trackingService.getUserTrackingList(userId);
        }
        
        return ResponseEntity.ok(trackingList);
    }
    
    /**
     * Get books to read next for a user.
     * GET /api/users/{userId}/to-read
     * @param userId the user ID
     * @return list of books marked as TO_READ
     */
    @GetMapping("/users/{userId}/to-read")
    public ResponseEntity<List<UserBookTracking>> getBooksToReadNext(@PathVariable String userId) {
        List<UserBookTracking> books = trackingService.getBooksToReadNext(userId);
        return ResponseEntity.ok(books);
    }
    
    /**
     * Get currently reading books for a user.
     * GET /api/users/{userId}/reading
     * @param userId the user ID
     * @return list of books currently being read
     */
    @GetMapping("/users/{userId}/reading")
    public ResponseEntity<List<UserBookTracking>> getCurrentlyReadingBooks(@PathVariable String userId) {
        List<UserBookTracking> books = trackingService.getCurrentlyReadingBooks(userId);
        return ResponseEntity.ok(books);
    }
    
    /**
     * Get completed books for a user.
     * GET /api/users/{userId}/completed
     * @param userId the user ID
     * @return list of completed books
     */
    @GetMapping("/users/{userId}/completed")
    public ResponseEntity<List<UserBookTracking>> getCompletedBooks(@PathVariable String userId) {
        List<UserBookTracking> books = trackingService.getCompletedBooks(userId);
        return ResponseEntity.ok(books);
    }
    
    /**
     * Get reading statistics for a user.
     * GET /api/users/{userId}/stats
     * @param userId the user ID
     * @return reading statistics
     */
    @GetMapping("/users/{userId}/stats")
    public ResponseEntity<List<Object[]>> getUserReadingStats(@PathVariable String userId) {
        List<Object[]> stats = trackingService.getUserReadingStats(userId);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Get a specific tracking entry.
     * GET /api/track/{trackingId}
     * @param trackingId the tracking entry ID
     * @return the tracking entry if found, 404 if not found
     */
    @GetMapping("/track/{trackingId}")
    public ResponseEntity<UserBookTracking> getTrackingById(@PathVariable String trackingId) {
        Optional<UserBookTracking> tracking = trackingService.getTrackingById(trackingId);
        return tracking.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Update reading progress or status.
     * PUT /api/track/{trackingId}
     * @param trackingId the tracking entry ID
     * @param request the update request
     * @return the updated tracking entry
     */
    @PutMapping("/track/{trackingId}")
    public ResponseEntity<UserBookTracking> updateTracking(
            @PathVariable String trackingId,
            @RequestBody TrackingUpdateRequest request) {
        try {
            UserBookTracking tracking;
            
            if (request.getCurrentPage() != null) {
                tracking = trackingService.updateProgress(trackingId, request.getCurrentPage());
            } else if (request.getStatus() != null) {
                tracking = trackingService.updateStatus(trackingId, request.getStatus());
            } else {
                return ResponseEntity.badRequest().build();
            }
            
            return ResponseEntity.ok(tracking);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Mark a book as finished.
     * PUT /api/track/{trackingId}/finish
     * @param trackingId the tracking entry ID
     * @return the updated tracking entry
     */
    @PutMapping("/track/{trackingId}/finish")
    public ResponseEntity<UserBookTracking> markAsFinished(@PathVariable String trackingId) {
        try {
            UserBookTracking tracking = trackingService.markAsFinished(trackingId);
            return ResponseEntity.ok(tracking);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Start reading a book.
     * PUT /api/track/{trackingId}/start
     * @param trackingId the tracking entry ID
     * @return the updated tracking entry
     */
    @PutMapping("/track/{trackingId}/start")
    public ResponseEntity<UserBookTracking> startReading(@PathVariable String trackingId) {
        try {
            UserBookTracking tracking = trackingService.startReading(trackingId);
            return ResponseEntity.ok(tracking);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Remove a book from user's tracking list.
     * DELETE /api/track/{trackingId}
     * @param trackingId the tracking entry ID
     * @return 204 No Content if successful, 404 if not found
     */
    @DeleteMapping("/track/{trackingId}")
    public ResponseEntity<Void> removeBookFromUserList(@PathVariable String trackingId) {
        try {
            trackingService.removeBookFromUserList(trackingId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Inner classes for request DTOs
    public static class TrackingRequest {
        private String bookId;
        private ReadingStatus status;
        
        // Getters and setters
        public String getBookId() { return bookId; }
        public void setBookId(String bookId) { this.bookId = bookId; }
        public ReadingStatus getStatus() { return status; }
        public void setStatus(ReadingStatus status) { this.status = status; }
    }
    
    public static class TrackingUpdateRequest {
        private Integer currentPage;
        private ReadingStatus status;
        
        // Getters and setters
        public Integer getCurrentPage() { return currentPage; }
        public void setCurrentPage(Integer currentPage) { this.currentPage = currentPage; }
        public ReadingStatus getStatus() { return status; }
        public void setStatus(ReadingStatus status) { this.status = status; }
    }
} 