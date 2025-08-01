package com.bookbound.repository;

import com.bookbound.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Book entity operations.
 */
@Repository
public interface BookRepository extends JpaRepository<Book, String> {
    
    /**
     * Find books by genre.
     * @param genre the genre to search for
     * @return list of books in the specified genre
     */
    List<Book> findByGenre(String genre);
    
    /**
     * Find books by title containing the given string (case-insensitive).
     * @param title the title fragment to search for
     * @return list of matching books
     */
    List<Book> findByTitleContainingIgnoreCase(String title);
    
    /**
     * Find books by author containing the given string (case-insensitive).
     * @param author the author name fragment to search for
     * @return list of matching books
     */
    List<Book> findByAuthorContainingIgnoreCase(String author);
    
    /**
     * Find books by series name.
     * @param seriesName the series name to search for
     * @return list of books in the specified series
     */
    List<Book> findBySeriesName(String seriesName);
    
    /**
     * Find books by series name ordered by series order.
     * @param seriesName the series name to search for
     * @return list of books in the series, ordered by series order
     */
    List<Book> findBySeriesNameOrderBySeriesOrder(String seriesName);
    
    /**
     * Find books within a page range.
     * @param minPages minimum number of pages
     * @param maxPages maximum number of pages
     * @return list of books within the page range
     */
    List<Book> findByTotalPagesBetween(Integer minPages, Integer maxPages);
    
    /**
     * Complex search query supporting multiple filters.
     * @param title title filter (can be null)
     * @param author author filter (can be null)
     * @param genre genre filter (can be null)
     * @param seriesName series filter (can be null)
     * @param minPages minimum pages (can be null)
     * @param maxPages maximum pages (can be null)
     * @return list of books matching the criteria
     */
    @Query("SELECT b FROM Book b WHERE " +
           "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
           "(:genre IS NULL OR b.genre = :genre) AND " +
           "(:seriesName IS NULL OR b.seriesName = :seriesName) AND " +
           "(:minPages IS NULL OR b.totalPages >= :minPages) AND " +
           "(:maxPages IS NULL OR b.totalPages <= :maxPages)")
    List<Book> searchBooks(@Param("title") String title,
                          @Param("author") String author,
                          @Param("genre") String genre,
                          @Param("seriesName") String seriesName,
                          @Param("minPages") Integer minPages,
                          @Param("maxPages") Integer maxPages);
} 