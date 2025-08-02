package com.bookbound.service;

import com.bookbound.model.*;
import com.bookbound.repository.BookRepository;
import com.bookbound.repository.StoreRepository;
import com.bookbound.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Service to initialize sample data when the application starts.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializationService implements CommandLineRunner {
    
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final GoogleBooksService googleBooksService;
    
    @Override
    public void run(String... args) throws Exception {
        if (bookRepository.count() == 0) {
            log.info("Initializing sample data...");
            initializeSampleData();
            log.info("Sample data initialization complete!");
        } else {
            log.info("Sample data already exists, skipping initialization.");
        }
    }
    
    private void initializeSampleData() {
        try {
            log.info("Initializing sample data...");
            
            // Create sample books
            initializeSampleBooks();
            
            // Create sample users
            User user1 = createUser("Alice Johnson", "alice@example.com");
            User user2 = createUser("Bob Smith", "bob@example.com");
            User user3 = createUser("Carol Davis", "carol@example.com");
            
            log.info("Sample data initialization completed successfully!");
            
        } catch (Exception e) {
            log.error("Error initializing sample data: {}", e.getMessage(), e);
        }
    }
    
    private void initializeSampleBooks() {
        // Science Fiction Books
        Book dune = createBook("Dune", "Frank Herbert", "Science Fiction", 688, null, null);
        Book timeMachine = createBook("The Time Machine", "H.G. Wells", "Science Fiction", 118, null, null);
        Book foundation1 = createBook("Foundation", "Isaac Asimov", "Science Fiction", 244, "Foundation", 1);
        Book foundation2 = createBook("Foundation and Empire", "Isaac Asimov", "Science Fiction", 247, "Foundation", 2);
        Book foundation3 = createBook("Second Foundation", "Isaac Asimov", "Science Fiction", 210, "Foundation", 3);
        Book ender1 = createBook("Ender's Game", "Orson Scott Card", "Science Fiction", 324, "Ender's Saga", 1);
        Book ender2 = createBook("Speaker for the Dead", "Orson Scott Card", "Science Fiction", 415, "Ender's Saga", 2);
        
        // Fantasy Books
        Book hobbit = createBook("The Hobbit", "J.R.R. Tolkien", "Fantasy", 310, null, null);
        Book lotr1 = createBook("The Fellowship of the Ring", "J.R.R. Tolkien", "Fantasy", 423, "The Lord of the Rings", 1);
        Book lotr2 = createBook("The Two Towers", "J.R.R. Tolkien", "Fantasy", 352, "The Lord of the Rings", 2);
        Book lotr3 = createBook("The Return of the King", "J.R.R. Tolkien", "Fantasy", 416, "The Lord of the Rings", 3);
        Book got1 = createBook("A Game of Thrones", "George R.R. Martin", "Fantasy", 694, "A Song of Ice and Fire", 1);
        Book got2 = createBook("A Clash of Kings", "George R.R. Martin", "Fantasy", 761, "A Song of Ice and Fire", 2);
        Book kingkiller = createBook("The Name of the Wind", "Patrick Rothfuss", "Fantasy", 662, "The Kingkiller Chronicle", 1);
        
        // Mystery Books
        Book christie1 = createBook("The Murder of Roger Ackroyd", "Agatha Christie", "Mystery", 256, null, null);
        Book christie2 = createBook("And Then There Were None", "Agatha Christie", "Mystery", 264, null, null);
        Book chandler = createBook("The Big Sleep", "Raymond Chandler", "Mystery", 231, null, null);
        Book french = createBook("In the Woods", "Tana French", "Mystery", 429, null, null);
        
        // Romance Books
        Book austen = createBook("Pride and Prejudice", "Jane Austen", "Romance", 432, null, null);
        Book bronte = createBook("Jane Eyre", "Charlotte Brontë", "Romance", 507, null, null);
        Book outlander1 = createBook("Outlander", "Diana Gabaldon", "Romance", 627, "Outlander", 1);
        Book outlander2 = createBook("Dragonfly in Amber", "Diana Gabaldon", "Romance", 743, "Outlander", 2);
        
        // Thriller Books
        Book flynn = createBook("Gone Girl", "Gillian Flynn", "Thriller", 419, null, null);
        Book larsson1 = createBook("The Girl with the Dragon Tattoo", "Stieg Larsson", "Thriller", 465, "Millennium", 1);
        Book larsson2 = createBook("The Girl Who Played with Fire", "Stieg Larsson", "Thriller", 503, "Millennium", 2);
        
        // Non-Fiction
        Book harari = createBook("Sapiens", "Yuval Noah Harari", "Non-Fiction", 443, null, null);
        Book westover = createBook("Educated", "Tara Westover", "Non-Fiction", 334, null, null);
        Book skloot = createBook("The Immortal Life of Henrietta Lacks", "Rebecca Skloot", "Non-Fiction", 381, null, null);
        
        // Classic Literature
        Book lee = createBook("To Kill a Mockingbird", "Harper Lee", "Classic Literature", 376, null, null);
        Book orwell = createBook("1984", "George Orwell", "Classic Literature", 328, null, null);
        Book fitzgerald = createBook("The Great Gatsby", "F. Scott Fitzgerald", "Classic Literature", 180, null, null);
        Book marquez = createBook("One Hundred Years of Solitude", "Gabriel García Márquez", "Classic Literature", 417, null, null);
        
        // Save all books and add stores for some of them
        Book[] allBooks = {dune, timeMachine, foundation1, foundation2, foundation3, ender1, ender2,
                          hobbit, lotr1, lotr2, lotr3, got1, got2, kingkiller,
                          christie1, christie2, chandler, french,
                          austen, bronte, outlander1, outlander2,
                          flynn, larsson1, larsson2,
                          harari, westover, skloot,
                          lee, orwell, fitzgerald, marquez};
        
        for (Book book : allBooks) {
            Book savedBook = bookRepository.save(book);
            
            // Add sample stores for some popular books
            if (savedBook.getTitle().contains("Dune") || savedBook.getTitle().contains("Foundation") || 
                savedBook.getTitle().contains("Time Machine")) {
                addSampleStores(savedBook);
            }
        }
    }
    
    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword("password123"); // Simple password for demo purposes
        return userRepository.save(user);
    }
    
    private Book createBook(String title, String author, String genre, Integer totalPages, 
                           String seriesName, Integer seriesOrder) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setGenre(genre);
        book.setTotalPages(totalPages);
        book.setSeriesName(seriesName);
        book.setSeriesOrder(seriesOrder);
        
        // Try to fetch real cover image from Google Books API
        try {
            log.info("Attempting to fetch cover image for '{}' by {}", title, author);
            String coverUrl = googleBooksService.getBookCoverUrl(title, author);
            
            if (coverUrl != null && !coverUrl.trim().isEmpty()) {
                book.setImageUrl(coverUrl);
                log.info("✅ Found cover image for '{}' by {}: {}", title, author, coverUrl);
            } else {
                // Fallback to placeholder image
                String placeholderUrl = "https://via.placeholder.com/300x400/cccccc/666666?text=" + title.replace(" ", "+");
                book.setImageUrl(placeholderUrl);
                log.info("❌ No cover image found for '{}' by {}, using placeholder: {}", title, author, placeholderUrl);
            }
        } catch (Exception e) {
            // Fallback to placeholder image if API call fails
            String placeholderUrl = "https://via.placeholder.com/300x400/cccccc/666666?text=" + title.replace(" ", "+");
            book.setImageUrl(placeholderUrl);
            log.warn("⚠️ Failed to fetch cover image for '{}' by {}: {}. Using placeholder: {}", 
                    title, author, e.getMessage(), placeholderUrl);
        }
        
        return book;
    }
    
    private void addSampleStores(Book book) {
        Store amazonStore = new Store();
        amazonStore.setStoreName("Amazon");
        amazonStore.setStoreUrl("https://amazon.com/books/" + book.getTitle().toLowerCase().replace(" ", "-"));
        amazonStore.setStoreAddress("Online");
        amazonStore.setBook(book);
        
        Store barnesStore = new Store();
        barnesStore.setStoreName("Barnes & Noble");
        barnesStore.setStoreUrl("https://barnesandnoble.com/books/" + book.getTitle().toLowerCase().replace(" ", "-"));
        barnesStore.setStoreAddress("Online");
        barnesStore.setBook(book);
        
        storeRepository.save(amazonStore);
        storeRepository.save(barnesStore);
        
        book.getPurchaseLocations().add(amazonStore);
        book.getPurchaseLocations().add(barnesStore);
    }
} 