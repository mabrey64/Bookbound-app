package com.bookbound.service;

import com.bookbound.dto.ExternalBookDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for integrating with Google Books API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleBooksService {
    
    private static final String GOOGLE_BOOKS_API_URL = "https://www.googleapis.com/books/v1/volumes";
    private static final int MAX_RESULTS = 20;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    /**
     * Search books using Google Books API.
     * @param query the search term (title, author, genre, etc.)
     * @return list of books from Google Books API
     */
    public List<ExternalBookDto> searchBooks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            String url = buildSearchUrl(query);
            log.info("Searching Google Books API with URL: {}", url);
            
            String response = restTemplate.getForObject(url, String.class);
            return parseGoogleBooksResponse(response);
            
        } catch (Exception e) {
            log.error("Error searching Google Books API: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Search books by specific criteria.
     * @param title book title
     * @param author book author
     * @param subject book subject/genre
     * @return list of books matching the criteria
     */
    public List<ExternalBookDto> searchBooks(String title, String author, String subject) {
        StringBuilder queryBuilder = new StringBuilder();
        
        if (title != null && !title.trim().isEmpty()) {
            queryBuilder.append("intitle:").append(title.trim());
        }
        
        if (author != null && !author.trim().isEmpty()) {
            if (queryBuilder.length() > 0) queryBuilder.append("+");
            queryBuilder.append("inauthor:").append(author.trim());
        }
        
        if (subject != null && !subject.trim().isEmpty()) {
            if (queryBuilder.length() > 0) queryBuilder.append("+");
            queryBuilder.append("subject:").append(subject.trim());
        }
        
        if (queryBuilder.length() == 0) {
            return new ArrayList<>();
        }
        
        return searchBooks(queryBuilder.toString());
    }
    
    /**
     * Build the Google Books API search URL.
     * @param query the search query
     * @return formatted URL
     */
    private String buildSearchUrl(String query) {
        return String.format("%s?q=%s&maxResults=%d&printType=books",
                GOOGLE_BOOKS_API_URL,
                query.replace(" ", "+"),
                MAX_RESULTS);
    }
    
    /**
     * Parse the JSON response from Google Books API.
     * @param jsonResponse the raw JSON response
     * @return list of ExternalBookDto objects
     */
    private List<ExternalBookDto> parseGoogleBooksResponse(String jsonResponse) {
        List<ExternalBookDto> books = new ArrayList<>();
        
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode items = root.get("items");
            
            if (items == null || !items.isArray()) {
                log.info("No books found in Google Books API response");
                return books;
            }
            
            for (JsonNode item : items) {
                ExternalBookDto book = parseBookItem(item);
                if (book != null && book.isValid()) {
                    books.add(book);
                }
            }
            
            log.info("Parsed {} books from Google Books API", books.size());
            
        } catch (Exception e) {
            log.error("Error parsing Google Books API response: {}", e.getMessage(), e);
        }
        
        return books;
    }
    
    /**
     * Parse a single book item from the Google Books API response.
     * @param item the JSON node representing a book
     * @return ExternalBookDto or null if parsing fails
     */
    private ExternalBookDto parseBookItem(JsonNode item) {
        try {
            JsonNode volumeInfo = item.get("volumeInfo");
            if (volumeInfo == null) {
                return null;
            }
            
            ExternalBookDto book = new ExternalBookDto();
            
            // Title
            JsonNode titleNode = volumeInfo.get("title");
            book.setTitle(titleNode != null ? titleNode.asText() : null);
            
            // Authors
            JsonNode authorsNode = volumeInfo.get("authors");
            if (authorsNode != null && authorsNode.isArray()) {
                List<String> authors = new ArrayList<>();
                for (JsonNode author : authorsNode) {
                    authors.add(author.asText());
                }
                book.setAuthors(authors);
            }
            
            // Description
            JsonNode descriptionNode = volumeInfo.get("description");
            book.setDescription(descriptionNode != null ? descriptionNode.asText() : null);
            
            // Categories
            JsonNode categoriesNode = volumeInfo.get("categories");
            if (categoriesNode != null && categoriesNode.isArray()) {
                List<String> categories = new ArrayList<>();
                for (JsonNode category : categoriesNode) {
                    categories.add(category.asText());
                }
                book.setCategories(categories);
            }
            
            // Page count
            JsonNode pageCountNode = volumeInfo.get("pageCount");
            book.setPageCount(pageCountNode != null ? pageCountNode.asInt() : null);
            
            // Publisher
            JsonNode publisherNode = volumeInfo.get("publisher");
            book.setPublisher(publisherNode != null ? publisherNode.asText() : null);
            
            // Preview link
            JsonNode previewLinkNode = volumeInfo.get("previewLink");
            book.setPreviewLink(previewLinkNode != null ? previewLinkNode.asText() : null);
            
            // Thumbnail
            JsonNode imageLinksNode = volumeInfo.get("imageLinks");
            if (imageLinksNode != null) {
                JsonNode thumbnailNode = imageLinksNode.get("thumbnail");
                book.setThumbnailUrl(thumbnailNode != null ? thumbnailNode.asText() : null);
            }
            
            // Published date
            JsonNode publishedDateNode = volumeInfo.get("publishedDate");
            book.setPublishedDate(publishedDateNode != null ? publishedDateNode.asText() : null);
            
            // Language
            JsonNode languageNode = volumeInfo.get("language");
            book.setLanguage(languageNode != null ? languageNode.asText() : "en");
            
            return book;
            
        } catch (Exception e) {
            log.error("Error parsing individual book item: {}", e.getMessage(), e);
            return null;
        }
    }
} 