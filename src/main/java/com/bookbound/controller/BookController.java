package com.bookbound.controller;

import com.bookbound.dto.SearchFilter;
import com.bookbound.model.Book;
import com.bookbound.model.Store;
import com.bookbound.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for Book entity operations.
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Slf4j
public class BookController {
    
    private final BookService bookService;
    
    // =============================
    // CRUD Operations
    // =============================
    
    /**
     * Create a new book.
     * POST /api/books
     * @param book the book to create
     * @return the created book with 201 status
     */
    @PostMapping
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        log.info("Creating new book: {}", book.getTitle());
        Book createdBook = bookService.createBook(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }
    
    /**
     * Get all books.
     * GET /api/books
     * @return list of all books
     */
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = bookService.getAllBooks();
        log.info("Retrieved {} books", books.size());
        return ResponseEntity.ok(books);
    }
    
    /**
     * Get book by ID.
     * GET /api/books/{id}
     * @param id the book ID
     * @return the book if found, 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable String id) {
        Optional<Book> book = bookService.getBookById(id);
        return book.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Update book information.
     * PUT /api/books/{id}
     * @param id the book ID
     * @param book the updated book data
     * @return the updated book
     */
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable String id, @RequestBody Book book) {
        try {
            Book updatedBook = bookService.updateBook(id, book);
            log.info("Updated book with id: {}", id);
            return ResponseEntity.ok(updatedBook);
        } catch (IllegalArgumentException e) {
            log.error("Book not found for update: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Delete book by ID.
     * DELETE /api/books/{id}
     * @param id the book ID
     * @return 204 No Content if successful, 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable String id) {
        try {
            bookService.deleteBook(id);
            log.info("Deleted book with id: {}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Book not found for deletion: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
    
    // =============================
    // Search Operations
    // =============================
    
    /**
     * Advanced search using SearchFilter DTO in request body.
     * POST /api/books/search
     * @param searchFilter the search criteria
     * @return list of books matching the criteria
     */
    @PostMapping("/search")
    public ResponseEntity<List<Book>> searchBooksAdvanced(@RequestBody SearchFilter searchFilter) {
        log.info("Advanced search with filter: {}", searchFilter);
        try {
            List<Book> books = bookService.searchBooks(searchFilter);
            return ResponseEntity.ok(books);
        } catch (IllegalArgumentException e) {
            log.error("Invalid search criteria: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Search books using query parameters.
     * GET /api/books/search?title=...&author=...&genre=...&minPages=...&maxPages=...&seriesName=...
     * @param title title filter (optional)
     * @param author author filter (optional)
     * @param genre genre filter (optional)
     * @param minPages minimum pages filter (optional)
     * @param maxPages maximum pages filter (optional)
     * @param seriesName series name filter (optional)
     * @return list of books matching the criteria
     */
    @GetMapping("/search")
    public ResponseEntity<List<Book>> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Integer minPages,
            @RequestParam(required = false) Integer maxPages,
            @RequestParam(required = false) String seriesName) {
        
        SearchFilter searchFilter = new SearchFilter(title, author, genre, minPages, maxPages, seriesName);
        log.info("Search with query params: {}", searchFilter);
        
        try {
            List<Book> books = bookService.searchBooks(searchFilter);
            return ResponseEntity.ok(books);
        } catch (IllegalArgumentException e) {
            log.error("Invalid search criteria: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    // =============================
    // Convenience Search Endpoints
    // =============================
    
    /**
     * Search books by title only.
     * GET /api/books/search/title?q=searchTerm
     * @param q the title search term
     * @return list of books with matching titles
     */
    @GetMapping("/search/title")
    public ResponseEntity<List<Book>> searchBooksByTitle(@RequestParam String q) {
        log.info("Searching books by title: {}", q);
        List<Book> books = bookService.searchBooksByTitle(q);
        return ResponseEntity.ok(books);
    }
    
    /**
     * Search books by author only.
     * GET /api/books/search/author?q=searchTerm
     * @param q the author search term
     * @return list of books by matching author
     */
    @GetMapping("/search/author")
    public ResponseEntity<List<Book>> searchBooksByAuthor(@RequestParam String q) {
        log.info("Searching books by author: {}", q);
        List<Book> books = bookService.searchBooksByAuthor(q);
        return ResponseEntity.ok(books);
    }
    
    /**
     * Get books by genre.
     * GET /api/books/genre/{genre}
     * @param genre the genre to search for
     * @return list of books in the specified genre
     */
    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<Book>> getBooksByGenre(@PathVariable String genre) {
        log.info("Getting books by genre: {}", genre);
        List<Book> books = bookService.getBooksByGenre(genre);
        return ResponseEntity.ok(books);
    }
    
    /**
     * Get books by series.
     * GET /api/books/series/{seriesName}
     * @param seriesName the series name
     * @return list of books in the series, ordered by series order
     */
    @GetMapping("/series/{seriesName}")
    public ResponseEntity<List<Book>> getBooksBySeries(@PathVariable String seriesName) {
        log.info("Getting books by series: {}", seriesName);
        List<Book> books = bookService.getBooksBySeries(seriesName);
        return ResponseEntity.ok(books);
    }
    
    /**
     * Search books within a page range.
     * GET /api/books/search/pages?min=100&max=500
     * @param min minimum pages
     * @param max maximum pages
     * @return list of books within the page range
     */
    @GetMapping("/search/pages")
    public ResponseEntity<List<Book>> searchBooksByPageRange(
            @RequestParam(required = false) Integer min,
            @RequestParam(required = false) Integer max) {
        log.info("Searching books by page range: {} to {}", min, max);
        List<Book> books = bookService.searchBooksByPageRange(min, max);
        return ResponseEntity.ok(books);
    }
    
    // =============================
    // Metadata Endpoints
    // =============================
    
    /**
     * Get all unique genres.
     * GET /api/books/metadata/genres
     * @return list of unique genres
     */
    @GetMapping("/metadata/genres")
    public ResponseEntity<List<String>> getAllGenres() {
        List<String> genres = bookService.getAllGenres();
        log.info("Retrieved {} unique genres", genres.size());
        return ResponseEntity.ok(genres);
    }
    
    /**
     * Get all unique series names.
     * GET /api/books/metadata/series
     * @return list of unique series names
     */
    @GetMapping("/metadata/series")
    public ResponseEntity<List<String>> getAllSeriesNames() {
        List<String> series = bookService.getAllSeriesNames();
        log.info("Retrieved {} unique series", series.size());
        return ResponseEntity.ok(series);
    }
    
    /**
     * Get all unique authors.
     * GET /api/books/metadata/authors
     * @return list of unique authors
     */
    @GetMapping("/metadata/authors")
    public ResponseEntity<List<String>> getAllAuthors() {
        List<String> authors = bookService.getAllAuthors();
        log.info("Retrieved {} unique authors", authors.size());
        return ResponseEntity.ok(authors);
    }
    
    // =============================
    // Store Management
    // =============================
    
    /**
     * Add a store location to a book.
     * POST /api/books/{id}/stores
     * @param id the book ID
     * @param store the store to add
     * @return the updated book
     */
    @PostMapping("/{id}/stores")
    public ResponseEntity<Book> addStoreToBook(@PathVariable String id, @RequestBody Store store) {
        try {
            Book updatedBook = bookService.addStoreToBook(id, store);
            log.info("Added store to book with id: {}", id);
            return ResponseEntity.ok(updatedBook);
        } catch (IllegalArgumentException e) {
            log.error("Book not found for adding store: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
} 