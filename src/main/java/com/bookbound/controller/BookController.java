package com.bookbound.controller;

import com.bookbound.dto.SearchFilter;
import com.bookbound.model.Book;
import com.bookbound.model.Store;
import com.bookbound.service.BookService;
import lombok.RequiredArgsConstructor;
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
public class BookController {
    
    private final BookService bookService;
    
    /**
     * Create a new book.
     * POST /api/books
     * @param book the book to create
     * @return the created book with 201 status
     */
    @PostMapping
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
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
     * Search books using multiple filters.
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
        
        try {
            List<Book> books = bookService.searchBooks(searchFilter);
            return ResponseEntity.ok(books);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Search books using SearchFilter DTO in request body.
     * POST /api/books/search
     * @param searchFilter the search criteria
     * @return list of books matching the criteria
     */
    @PostMapping("/search")
    public ResponseEntity<List<Book>> searchBooksWithBody(@RequestBody SearchFilter searchFilter) {
        try {
            List<Book> books = bookService.searchBooks(searchFilter);
            return ResponseEntity.ok(books);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get books by genre.
     * GET /api/books/genre/{genre}
     * @param genre the genre to search for
     * @return list of books in the specified genre
     */
    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<Book>> getBooksByGenre(@PathVariable String genre) {
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
        List<Book> books = bookService.getBooksBySeries(seriesName);
        return ResponseEntity.ok(books);
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
            return ResponseEntity.ok(updatedBook);
        } catch (IllegalArgumentException e) {
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
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
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
            return ResponseEntity.ok(updatedBook);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
} 