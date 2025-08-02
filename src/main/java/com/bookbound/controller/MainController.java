package com.bookbound.controller;

import com.bookbound.service.BookService;
import com.bookbound.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.bookbound.model.User;
import java.util.Optional;
import java.util.List;
import com.bookbound.service.GoogleBooksService;

/**
 * Main controller for serving Thymeleaf templates and frontend integration.
 * Handles page routing and data injection for the frontend views.
 */
@Controller
@RequiredArgsConstructor
public class MainController {

    private final BookService bookService;
    private final UserService userService;
    private final GoogleBooksService googleBooksService;

    /**
     * Landing page - entry point for the application.
     */
    @GetMapping("/")
    public String landing(Model model) {
        model.addAttribute("tpl", "landing");
        return "landing";
    }

    /**
     * Login page for user authentication.
     */
    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("tpl", "login");
        return "login";
    }

    /**
     * Signup page for new user registration.
     */
    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("tpl", "signup");
        return "signup";
    }

    /**
     * Handle signup form submission.
     */
    @PostMapping("/signup")
    public String handleSignup(@RequestParam String username,
                              @RequestParam String email,
                              @RequestParam String password,
                              Model model) {
        try {
            User newUser = new User();
            newUser.setName(username);
            newUser.setEmail(email);
            newUser.setPassword(password); // In production, this should be hashed
            
            User createdUser = userService.createUser(newUser);
            model.addAttribute("success", "Account created successfully! Please log in.");
            model.addAttribute("tpl", "login");
            return "login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("tpl", "signup");
            return "signup";
        }
    }

    /**
     * Handle login form submission.
     */
    @PostMapping("/login")
    public String handleLogin(@RequestParam String email,
                             @RequestParam String password,
                             Model model) {
        try {
            System.out.println("Login attempt for email: " + email); // Debug log
            
            Optional<User> userOpt = userService.getUserByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                System.out.println("User found: " + user.getName() + ", checking password..."); // Debug log
                
                if (user.getPassword().equals(password)) {
                    System.out.println("Password match! Redirecting to home..."); // Debug log
                    model.addAttribute("user", user);
                    model.addAttribute("tpl", "home");
                    return "redirect:/home?userId=" + user.getId();
                } else {
                    System.out.println("Password mismatch"); // Debug log
                    model.addAttribute("error", "Invalid email or password");
                    model.addAttribute("tpl", "login");
                    return "login";
                }
            } else {
                System.out.println("User not found for email: " + email); // Debug log
                model.addAttribute("error", "Invalid email or password");
                model.addAttribute("tpl", "login");
                return "login";
            }
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage()); // Debug log
            e.printStackTrace();
            model.addAttribute("error", "Login failed: " + e.getMessage());
            model.addAttribute("tpl", "login");
            return "login";
        }
    }

    /**
     * Handle logout.
     */
    @GetMapping("/logout")
    public String logout() {
        return "redirect:/";
    }

    /**
     * User dashboard/home page with personalized data.
     */
    @GetMapping("/home")
    public String home(@RequestParam(required = false) String userId, Model model) {
        model.addAttribute("tpl", "home");
        
        if (userId != null) {
            try {
                var userOpt = userService.getUserById(userId);
                if (userOpt.isPresent()) {
                    var user = userOpt.get();
                    model.addAttribute("user", user);

                    var recentBooks = bookService.getAllBooks().stream().limit(6).toList();
                    var genres = bookService.getAllGenres();

                    model.addAttribute("recentBooks", recentBooks);
                    model.addAttribute("genres", genres);
                } else {
                    model.addAttribute("error", "User not found");
                }
            } catch (Exception e) {
                model.addAttribute("error", "User not found");
            }
        }
        return "home";
    }

    /**
     * Search results page with book data.
     */
    @GetMapping("/search")
    public String search(@RequestParam(required = false) String query,
                         @RequestParam(required = false) String genre,
                         @RequestParam(required = false) String author,
                         Model model) {
        System.out.println("=== SEARCH REQUEST START ===");
        System.out.println("Query: " + query);
        System.out.println("Genre: " + genre);
        System.out.println("Author: " + author);
        
        try {
            // Get all books and filter them
            var allBooks = bookService.getAllBooks();
            var filteredBooks = allBooks.stream()
                .filter(book -> {
                    boolean matches = true;
                    
                    // Filter by query (title, author, or genre)
                    if (query != null && !query.trim().isEmpty()) {
                        String searchQuery = query.toLowerCase();
                        matches = matches && (
                            book.getTitle().toLowerCase().contains(searchQuery) ||
                            book.getAuthor().toLowerCase().contains(searchQuery) ||
                            book.getGenre().toLowerCase().contains(searchQuery)
                        );
                    }
                    
                    // Filter by genre
                    if (genre != null && !genre.trim().isEmpty()) {
                        matches = matches && book.getGenre().equalsIgnoreCase(genre);
                    }
                    
                    // Filter by author
                    if (author != null && !author.trim().isEmpty()) {
                        matches = matches && book.getAuthor().equalsIgnoreCase(author);
                    }
                    
                    return matches;
                })
                .toList();
            
            // Get all genres and authors for the filter dropdowns
            var allGenres = bookService.getAllGenres();
            var allAuthors = bookService.getAllAuthors();
            
            System.out.println("Total books: " + allBooks.size());
            System.out.println("Filtered books: " + filteredBooks.size());
            System.out.println("Genres: " + allGenres);
            System.out.println("Authors: " + allAuthors);
            
            model.addAttribute("tpl", "search");
            model.addAttribute("books", filteredBooks);
            model.addAttribute("genres", allGenres);
            model.addAttribute("authors", allAuthors);
            model.addAttribute("searchQuery", query);
            
            System.out.println("=== SEARCH REQUEST COMPLETED SUCCESSFULLY ===");
            return "search";
            
        } catch (Exception e) {
            System.out.println("❌ SEARCH ERROR: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Search failed: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Individual book detail page.
     */
    @GetMapping("/book/{bookId}")
    public String bookDetail(@PathVariable String bookId,
                             @RequestParam(required = false) String userId,
                             Model model) {
        model.addAttribute("tpl", "book");
        
        try {
            var bookOptional = bookService.getBookById(bookId);
            if (bookOptional.isPresent()) {
                var book = bookOptional.get();
                model.addAttribute("book", book);

                if (userId != null) {
                    model.addAttribute("userId", userId);
                }

                if (book.getSeriesName() != null) {
                    var seriesBooks = bookService.getBooksBySeries(book.getSeriesName());
                    model.addAttribute("seriesBooks", seriesBooks);
                } else {
                    var similarBooks = bookService.getBooksByGenre(book.getGenre()).stream().limit(4).toList();
                    model.addAttribute("similarBooks", similarBooks);
                }
            } else {
                model.addAttribute("error", "Book not found");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Book not found");
        }

        return "book-detail";
    }

    /**
     * User's personal library page.
     */
    @GetMapping("/library")
    public String library(@RequestParam(required = false) String userId, Model model) {
        model.addAttribute("tpl", "library");
        
        if (userId != null) {
            try {
                var userOpt = userService.getUserById(userId);
                if (userOpt.isPresent()) {
                    var user = userOpt.get();
                    model.addAttribute("user", user);
                    
                    // Add books and genres data
                    var books = bookService.getAllBooks();
                    var genres = bookService.getAllGenres();
                    
                    model.addAttribute("books", books);
                    model.addAttribute("genres", genres);
                    // Example: model.addAttribute("trackingList", trackingService.getUserTrackingList(userId));
                } else {
                    model.addAttribute("error", "User not found");
                }
            } catch (Exception e) {
                model.addAttribute("error", "Unable to load library");
            }
        } else {
            return "redirect:/login";
        }

        return "library";
    }

    /**
     * Analytics page.
     */
    @GetMapping("/analytics")
    public String analytics(Model model) {
        model.addAttribute("tpl", "analytics");
        return "analytics";
    }

    /**
     * Categories overview page.
     */
    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("tpl", "categories");
        return "categories";
    }

    /**
     * User settings page.
     */
    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("tpl", "settings");
        return "settings";
    }

    /**
     * Test endpoint to check sample users.
     */
    @GetMapping("/test-users")
    public String testUsers(Model model) {
        try {
            var users = userService.getAllUsers();
            model.addAttribute("users", users);
            model.addAttribute("userCount", users.size());
            return "test-users";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading users: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Test endpoint to check database operations.
     */
    @GetMapping("/test-db")
    public String testDatabase(Model model) {
        try {
            System.out.println("=== DATABASE TEST ===");
            
            // Test getAllBooks
            var allBooks = bookService.getAllBooks();
            System.out.println("Total books: " + allBooks.size());
            
            // Test getAllGenres
            var genres = bookService.getAllGenres();
            System.out.println("Available genres: " + genres);
            
            // Test getAllAuthors
            var authors = bookService.getAllAuthors();
            System.out.println("Available authors: " + authors);
            
            // Test individual book
            if (!allBooks.isEmpty()) {
                var firstBook = allBooks.get(0);
                System.out.println("First book: " + firstBook.getTitle() + " by " + firstBook.getAuthor() + " (" + firstBook.getGenre() + ")");
            }
            
            model.addAttribute("totalBooks", allBooks.size());
            model.addAttribute("genres", genres);
            model.addAttribute("authors", authors);
            model.addAttribute("sampleBook", allBooks.isEmpty() ? null : allBooks.get(0));
            model.addAttribute("success", "Database test completed successfully");
            
            return "test-db";
            
        } catch (Exception e) {
            System.out.println("Database test error: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Database test failed: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Test endpoint to check Google Books API functionality.
     */
    @GetMapping("/test-google-books")
    public String testGoogleBooks(Model model) {
        try {
            String testUrl = googleBooksService.getBookCoverUrl("Dune", "Frank Herbert");
            model.addAttribute("testUrl", testUrl);
            model.addAttribute("success", "Google Books API test completed");
            return "test-google-books";
        } catch (Exception e) {
            model.addAttribute("error", "Google Books API test failed: " + e.getMessage());
            return "error";
        }
    }

    /**
     * API status page for development/debugging.
     */
    @GetMapping("/api-status")
    public String apiStatus(Model model) {
        try {
            model.addAttribute("bookCount", bookService.getAllBooks().size());
            model.addAttribute("userCount", userService.getAllUsers().size());
            model.addAttribute("status", "Backend API is running");
        } catch (Exception e) {
            model.addAttribute("status", "Backend API error: " + e.getMessage());
        }

        return "api-status";
    }
}
