package com.bookbound.controller;

import com.bookbound.service.BookService;
import com.bookbound.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Main controller for serving Thymeleaf templates and frontend integration.
 * Handles page routing and data injection for the frontend views.
 */
@Controller
@RequiredArgsConstructor
public class MainController {

    private final BookService bookService;
    private final UserService userService;

    /**
     * Landing page - entry point for the application.
     */
    @GetMapping("/")
    public String landing() {
        return "landing";
    }

    /**
     * Login page for user authentication.
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Signup page for new user registration.
     */
    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    /**
     * User dashboard/home page with personalized data.
     */
    @GetMapping("/home")
    public String home(@RequestParam(required = false) String userId, Model model) {
        if (userId != null) {
            try {
                var user = userService.getUserById(userId);
                model.addAttribute("user", user);

                var recentBooks = bookService.getAllBooks().stream().limit(6).toList();
                var genres = bookService.getAllGenres();

                model.addAttribute("recentBooks", recentBooks);
                model.addAttribute("genres", genres);
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
        if (query != null || genre != null || author != null) {
            try {
                var books = bookService.getAllBooks(); // Could be enhanced with actual search
                model.addAttribute("books", books);
                model.addAttribute("searchQuery", query);
            } catch (Exception e) {
                model.addAttribute("error", "Search failed");
            }
        }

        model.addAttribute("genres", bookService.getAllGenres());
        model.addAttribute("authors", bookService.getAllAuthors());

        return "search";
    }

    /**
     * Individual book detail page.
     */
    @GetMapping("/book/{bookId}")
    public String bookDetail(@PathVariable String bookId,
                             @RequestParam(required = false) String userId,
                             Model model) {
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
        if (userId != null) {
            try {
                var user = userService.getUserById(userId);
                model.addAttribute("user", user);
                // Example: model.addAttribute("trackingList", trackingService.getUserTrackingList(userId));
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
    public String analytics() {
        return "analytics";
    }

    /**
     * Categories overview page.
     */
    @GetMapping("/categories")
    public String categories() {
        return "categories";
    }

    /**
     * User settings page.
     */
    @GetMapping("/settings")
    public String settings() {
        return "settings";
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
