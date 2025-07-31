package com.bookbound.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String landing() { return "landing"; }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/signup")
    public String signup() { return "signup"; }

    @GetMapping("/home")
    public String home() { return "home"; }

    @GetMapping("/search")
    public String search() { return "search"; }

    @GetMapping("/library")
    public String library() { return "library"; }

    @GetMapping("/book/{id}")
    public String bookDetail() { return "book-detail"; }

    @GetMapping("/analytics")
    public String analytics() { return "analytics"; }

    @GetMapping("/categories")
    public String categories() { return "categories"; }

    @GetMapping("/settings")
    public String settings() { return "settings"; }
}
