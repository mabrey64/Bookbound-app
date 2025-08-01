package com.bookbound.controller;

import com.bookbound.dto.ExternalBookDto;
import com.bookbound.service.GoogleBooksService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Google Books API integration.
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Slf4j
public class GoogleBooksController {
    
    private final GoogleBooksService googleBooksService;
    
    /**
     * Search books using Google Books API.
     * GET /api/books/google?query=searchTerm
     * @param query the search term (title, author, genre, etc.)
     * @return list of books from Google Books API
     */
    @GetMapping("/google")
    public ResponseEntity<List<ExternalBookDto>> searchGoogleBooks(@RequestParam String query) {
        log.info("Searching Google Books API with query: {}", query);
        
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        List<ExternalBookDto> books = googleBooksService.searchBooks(query);
        return ResponseEntity.ok(books);
    }
    
    /**
     * Advanced search using specific criteria.
     * GET /api/books/google/advanced?title=...&author=...&subject=...
     * @param title book title (optional)
     * @param author book author (optional)  
     * @param subject book subject/genre (optional)
     * @return list of books matching the criteria
     */
    @GetMapping("/google/advanced")
    public ResponseEntity<List<ExternalBookDto>> advancedSearchGoogleBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String subject) {
        
        log.info("Advanced Google Books search - Title: {}, Author: {}, Subject: {}", title, author, subject);
        
        // At least one parameter must be provided
        if ((title == null || title.trim().isEmpty()) && 
            (author == null || author.trim().isEmpty()) && 
            (subject == null || subject.trim().isEmpty())) {
            return ResponseEntity.badRequest().build();
        }
        
        List<ExternalBookDto> books = googleBooksService.searchBooks(title, author, subject);
        return ResponseEntity.ok(books);
    }
} 