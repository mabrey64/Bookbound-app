package com.bookbound.service;

import com.bookbound.dto.SearchFilter;
import com.bookbound.model.Book;
import com.bookbound.model.Store;
import com.bookbound.repository.BookRepository;
import com.bookbound.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for Book entity business logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {
    
    private final BookRepository bookRepository;
    private final StoreRepository storeRepository;
    
    /**
     * Create a new book.
     * @param book the book to create
     * @return the created book
     */
    public Book createBook(Book book) {
        Book savedBook = bookRepository.save(book);
        
        // Save associated stores if any
        if (book.getPurchaseLocations() != null && !book.getPurchaseLocations().isEmpty()) {
            for (Store store : book.getPurchaseLocations()) {
                store.setBook(savedBook);
                storeRepository.save(store);
            }
        }
        
        return savedBook;
    }
    
    /**
     * Get book by ID.
     * @param id the book ID
     * @return Optional containing the book if found
     */
    public Optional<Book> getBookById(String id) {
        return bookRepository.findById(id);
    }
    
    /**
     * Get all books.
     * @return list of all books
     */
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }
    
    /**
     * Search books using multiple filters with comprehensive validation.
     * @param searchFilter the search criteria
     * @return list of books matching the criteria
     */
    public List<Book> searchBooks(SearchFilter searchFilter) {
        log.info("Searching books with filter: {}", searchFilter);
        
        if (searchFilter == null) {
            log.warn("SearchFilter is null, returning all books");
            return getAllBooks();
        }
        
        if (searchFilter.isEmpty()) {
            log.info("SearchFilter is empty, returning all books");
            return getAllBooks();
        }
        
        if (!searchFilter.isValidPageRange()) {
            log.error("Invalid page range in SearchFilter: min={}, max={}", 
                     searchFilter.getMinPages(), searchFilter.getMaxPages());
            throw new IllegalArgumentException("Invalid page range specified");
        }
        
        // Clean up string fields (trim and handle empty strings)
        String cleanTitle = cleanSearchString(searchFilter.getTitle());
        String cleanAuthor = cleanSearchString(searchFilter.getAuthor());
        String cleanGenre = cleanSearchString(searchFilter.getGenre());
        String cleanSeriesName = cleanSearchString(searchFilter.getSeriesName());
        
        List<Book> results = bookRepository.searchBooks(
            cleanTitle,
            cleanAuthor,
            cleanGenre,
            cleanSeriesName,
            searchFilter.getMinPages(),
            searchFilter.getMaxPages()
        );
        
        log.info("Found {} books matching search criteria", results.size());
        return results;
    }
    
    /**
     * Search books by title only (convenience method).
     * @param title the title to search for
     * @return list of books with matching titles
     */
    public List<Book> searchBooksByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return getAllBooks();
        }
        
        SearchFilter filter = new SearchFilter();
        filter.setTitle(title);
        return searchBooks(filter);
    }
    
    /**
     * Search books by author only (convenience method).
     * @param author the author to search for
     * @return list of books by matching author
     */
    public List<Book> searchBooksByAuthor(String author) {
        if (author == null || author.trim().isEmpty()) {
            return getAllBooks();
        }
        
        SearchFilter filter = new SearchFilter();
        filter.setAuthor(author);
        return searchBooks(filter);
    }
    
    /**
     * Search books by genre only (convenience method).
     * @param genre the genre to search for
     * @return list of books in the specified genre
     */
    public List<Book> searchBooksByGenre(String genre) {
        if (genre == null || genre.trim().isEmpty()) {
            return getAllBooks();
        }
        
        SearchFilter filter = new SearchFilter();
        filter.setGenre(genre);
        return searchBooks(filter);
    }
    
    /**
     * Search books within a page range (convenience method).
     * @param minPages minimum pages
     * @param maxPages maximum pages
     * @return list of books within the page range
     */
    public List<Book> searchBooksByPageRange(Integer minPages, Integer maxPages) {
        SearchFilter filter = new SearchFilter();
        filter.setMinPages(minPages);
        filter.setMaxPages(maxPages);
        return searchBooks(filter);
    }
    
    /**
     * Get all unique genres in the database.
     * @return list of unique genres
     */
    public List<String> getAllGenres() {
        return bookRepository.findAll()
                .stream()
                .map(Book::getGenre)
                .filter(genre -> genre != null && !genre.trim().isEmpty())
                .distinct()
                .sorted()
                .toList();
    }
    
    /**
     * Get all unique series names in the database.
     * @return list of unique series names
     */
    public List<String> getAllSeriesNames() {
        return bookRepository.findAll()
                .stream()
                .map(Book::getSeriesName)
                .filter(seriesName -> seriesName != null && !seriesName.trim().isEmpty())
                .distinct()
                .sorted()
                .toList();
    }
    
    /**
     * Get all unique authors in the database.
     * @return list of unique authors
     */
    public List<String> getAllAuthors() {
        return bookRepository.findAll()
                .stream()
                .map(Book::getAuthor)
                .filter(author -> author != null && !author.trim().isEmpty())
                .distinct()
                .sorted()
                .toList();
    }
    
    /**
     * Find books by genre.
     * @param genre the genre to search for
     * @return list of books in the specified genre
     */
    public List<Book> getBooksByGenre(String genre) {
        return bookRepository.findByGenre(genre);
    }
    
    /**
     * Find books by title (partial match).
     * @param title the title fragment to search for
     * @return list of matching books
     */
    public List<Book> getBooksByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }
    
    /**
     * Find books by author (partial match).
     * @param author the author name fragment to search for
     * @return list of matching books
     */
    public List<Book> getBooksByAuthor(String author) {
        return bookRepository.findByAuthorContainingIgnoreCase(author);
    }
    
    /**
     * Find books in a series, ordered by series order.
     * @param seriesName the series name
     * @return list of books in the series, ordered by series order
     */
    public List<Book> getBooksBySeries(String seriesName) {
        return bookRepository.findBySeriesNameOrderBySeriesOrder(seriesName);
    }
    
    /**
     * Find books within a page range.
     * @param minPages minimum number of pages
     * @param maxPages maximum number of pages
     * @return list of books within the page range
     */
    public List<Book> getBooksByPageRange(Integer minPages, Integer maxPages) {
        return bookRepository.findByTotalPagesBetween(minPages, maxPages);
    }
    
    /**
     * Update book information.
     * @param id the book ID
     * @param updatedBook the updated book data
     * @return the updated book
     * @throws IllegalArgumentException if book not found
     */
    public Book updateBook(String id, Book updatedBook) {
        Book existingBook = bookRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + id));
        
        existingBook.setTitle(updatedBook.getTitle());
        existingBook.setAuthor(updatedBook.getAuthor());
        existingBook.setGenre(updatedBook.getGenre());
        existingBook.setTotalPages(updatedBook.getTotalPages());
        existingBook.setSeriesName(updatedBook.getSeriesName());
        existingBook.setSeriesOrder(updatedBook.getSeriesOrder());
        
        return bookRepository.save(existingBook);
    }
    
    /**
     * Delete book by ID.
     * @param id the book ID
     * @throws IllegalArgumentException if book not found
     */
    public void deleteBook(String id) {
        if (!bookRepository.existsById(id)) {
            throw new IllegalArgumentException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
    }
    
    /**
     * Add a store location to a book.
     * @param bookId the book ID
     * @param store the store to add
     * @return the updated book
     * @throws IllegalArgumentException if book not found
     */
    public Book addStoreToBook(String bookId, Store store) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + bookId));
        
        store.setBook(book);
        storeRepository.save(store);
        book.getPurchaseLocations().add(store);
        
        return book;
    }
    
    /**
     * Clean search string by trimming and converting empty strings to null.
     * @param input the input string
     * @return cleaned string or null
     */
    private String cleanSearchString(String input) {
        if (input == null) {
            return null;
        }
        String trimmed = input.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
} 