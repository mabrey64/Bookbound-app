package com.bookbound.service;

import com.bookbound.model.Book;
import com.bookbound.model.Store;
import com.bookbound.repository.BookRepository;
import com.bookbound.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Service to initialize the database with sample data for testing.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataInitializationService implements CommandLineRunner {
    
    private final BookRepository bookRepository;
    private final StoreRepository storeRepository;
    
    @Override
    public void run(String... args) throws Exception {
        if (bookRepository.count() == 0) {
            log.info("Database is empty. Initializing with sample data...");
            initializeSampleBooks();
            log.info("Sample data initialization completed!");
        } else {
            log.info("Database already contains {} books. Skipping sample data initialization.", bookRepository.count());
        }
    }
    
    private void initializeSampleBooks() {
        List<Book> sampleBooks = Arrays.asList(
            // Science Fiction Books
            createBook("Dune", "Frank Herbert", "Science Fiction", 688, null, null),
            createBook("The Time Machine", "H.G. Wells", "Science Fiction", 118, null, null),
            createBook("Foundation", "Isaac Asimov", "Science Fiction", 244, "Foundation", 1),
            createBook("Foundation and Empire", "Isaac Asimov", "Science Fiction", 247, "Foundation", 2),
            createBook("Second Foundation", "Isaac Asimov", "Science Fiction", 210, "Foundation", 3),
            createBook("Ender's Game", "Orson Scott Card", "Science Fiction", 324, "Ender's Saga", 1),
            createBook("Speaker for the Dead", "Orson Scott Card", "Science Fiction", 415, "Ender's Saga", 2),
            
            // Fantasy Books
            createBook("The Hobbit", "J.R.R. Tolkien", "Fantasy", 310, null, null),
            createBook("The Fellowship of the Ring", "J.R.R. Tolkien", "Fantasy", 423, "The Lord of the Rings", 1),
            createBook("The Two Towers", "J.R.R. Tolkien", "Fantasy", 352, "The Lord of the Rings", 2),
            createBook("The Return of the King", "J.R.R. Tolkien", "Fantasy", 416, "The Lord of the Rings", 3),
            createBook("A Game of Thrones", "George R.R. Martin", "Fantasy", 694, "A Song of Ice and Fire", 1),
            createBook("A Clash of Kings", "George R.R. Martin", "Fantasy", 761, "A Song of Ice and Fire", 2),
            createBook("The Name of the Wind", "Patrick Rothfuss", "Fantasy", 662, "The Kingkiller Chronicle", 1),
            
            // Mystery Books
            createBook("The Murder of Roger Ackroyd", "Agatha Christie", "Mystery", 256, null, null),
            createBook("And Then There Were None", "Agatha Christie", "Mystery", 264, null, null),
            createBook("The Big Sleep", "Raymond Chandler", "Mystery", 231, null, null),
            createBook("In the Woods", "Tana French", "Mystery", 429, null, null),
            
            // Romance Books
            createBook("Pride and Prejudice", "Jane Austen", "Romance", 432, null, null),
            createBook("Jane Eyre", "Charlotte Brontë", "Romance", 507, null, null),
            createBook("Outlander", "Diana Gabaldon", "Romance", 627, "Outlander", 1),
            createBook("Dragonfly in Amber", "Diana Gabaldon", "Romance", 743, "Outlander", 2),
            
            // Thriller Books
            createBook("Gone Girl", "Gillian Flynn", "Thriller", 419, null, null),
            createBook("The Girl with the Dragon Tattoo", "Stieg Larsson", "Thriller", 465, "Millennium", 1),
            createBook("The Girl Who Played with Fire", "Stieg Larsson", "Thriller", 503, "Millennium", 2),
            
            // Non-Fiction
            createBook("Sapiens", "Yuval Noah Harari", "Non-Fiction", 443, null, null),
            createBook("Educated", "Tara Westover", "Non-Fiction", 334, null, null),
            createBook("The Immortal Life of Henrietta Lacks", "Rebecca Skloot", "Non-Fiction", 381, null, null),
            
            // Classic Literature
            createBook("To Kill a Mockingbird", "Harper Lee", "Classic Literature", 376, null, null),
            createBook("1984", "George Orwell", "Classic Literature", 328, null, null),
            createBook("The Great Gatsby", "F. Scott Fitzgerald", "Classic Literature", 180, null, null),
            createBook("One Hundred Years of Solitude", "Gabriel García Márquez", "Classic Literature", 417, null, null)
        );
        
        // Save all books
        for (Book book : sampleBooks) {
            Book savedBook = bookRepository.save(book);
            
            // Add sample stores for some books
            if (savedBook.getTitle().contains("Time") || savedBook.getTitle().contains("Dune") || 
                savedBook.getTitle().contains("Foundation")) {
                addSampleStores(savedBook);
            }
        }
        
        log.info("Created {} sample books", sampleBooks.size());
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