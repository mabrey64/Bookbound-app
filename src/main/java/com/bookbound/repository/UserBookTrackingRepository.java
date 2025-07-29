package com.bookbound.repository;

import com.bookbound.model.ReadingStatus;
import com.bookbound.model.UserBookTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for UserBookTracking entity operations.
 */
@Repository
public interface UserBookTrackingRepository extends JpaRepository<UserBookTracking, String> {
    
    /**
     * Find all tracking entries for a specific user.
     * @param userId the user ID
     * @return list of tracking entries for the user
     */
    @Query("SELECT ubt FROM UserBookTracking ubt WHERE ubt.user.id = :userId")
    List<UserBookTracking> findByUserId(@Param("userId") String userId);
    
    /**
     * Find all tracking entries for a specific book.
     * @param bookId the book ID
     * @return list of tracking entries for the book
     */
    @Query("SELECT ubt FROM UserBookTracking ubt WHERE ubt.book.id = :bookId")
    List<UserBookTracking> findByBookId(@Param("bookId") String bookId);
    
    /**
     * Find tracking entries by user and status.
     * @param userId the user ID
     * @param status the reading status
     * @return list of tracking entries matching the criteria
     */
    @Query("SELECT ubt FROM UserBookTracking ubt WHERE ubt.user.id = :userId AND ubt.status = :status")
    List<UserBookTracking> findByUserIdAndStatus(@Param("userId") String userId, 
                                                 @Param("status") ReadingStatus status);
    
    /**
     * Find a specific tracking entry by user and book.
     * @param userId the user ID
     * @param bookId the book ID
     * @return Optional containing the tracking entry if found
     */
    @Query("SELECT ubt FROM UserBookTracking ubt WHERE ubt.user.id = :userId AND ubt.book.id = :bookId")
    Optional<UserBookTracking> findByUserIdAndBookId(@Param("userId") String userId, 
                                                     @Param("bookId") String bookId);
    
    /**
     * Check if a user is already tracking a specific book.
     * @param userId the user ID
     * @param bookId the book ID
     * @return true if tracking entry exists, false otherwise
     */
    @Query("SELECT COUNT(ubt) > 0 FROM UserBookTracking ubt WHERE ubt.user.id = :userId AND ubt.book.id = :bookId")
    boolean existsByUserIdAndBookId(@Param("userId") String userId, @Param("bookId") String bookId);
    
    /**
     * Get reading statistics for a user.
     * @param userId the user ID
     * @return list of status counts [status, count]
     */
    @Query("SELECT ubt.status, COUNT(ubt) FROM UserBookTracking ubt WHERE ubt.user.id = :userId GROUP BY ubt.status")
    List<Object[]> getReadingStatsByUserId(@Param("userId") String userId);
} 