# BookBound - Book Tracking Application

A Spring Boot application for tracking physical book reading progress, built with Java, Spring Boot, H2 Database, and Thymeleaf templates.

## Features

- ✅ User registration and authentication
- ✅ Book tracking (reading, completed, to-read)
- ✅ Series progress tracking
- ✅ Book search and filtering
- ✅ Sample data with popular books
- ✅ Responsive web interface

## Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Running the Application

1. **Clone and navigate to the project:**
   ```bash
   cd Bookbound-app
   ```

2. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

3. **Access the application:**
   - Main app: http://localhost:8081
   - H2 Database Console: http://localhost:8081/h2-console
     - JDBC URL: `jdbc:h2:mem:bookbounddb`
     - Username: `sa`
     - Password: (leave empty)

## Sample Data

The application comes pre-loaded with sample data including:

### Sample Users (for testing):
- **Alice Johnson** - alice@bookbound.com / password123
- **Bob Smith** - bob@bookbound.com / password123  
- **Carol Davis** - carol@bookbound.com / password123

### Sample Books:
- **Science Fiction**: Dune, Foundation series, Ender's Game series, The Time Machine
- **Fantasy**: The Lord of the Rings series, A Game of Thrones, The Hobbit, The Name of the Wind
- **Mystery**: Agatha Christie novels, The Big Sleep, In the Woods
- **Romance**: Pride and Prejudice, Jane Eyre, Outlander series
- **Thriller**: Gone Girl, Millennium series
- **Non-Fiction**: Sapiens, Educated, The Immortal Life of Henrietta Lacks
- **Classic Literature**: To Kill a Mockingbird, 1984, The Great Gatsby, One Hundred Years of Solitude

## Testing the Application

### 1. User Registration
1. Go to http://localhost:8081
2. Click "Sign Up"
3. Fill in name, email, and password
4. Submit the form
5. You should be redirected to login with a success message

### 2. User Login
1. Use one of the sample users or your newly created account
2. Enter email and password
3. You'll be redirected to the home page with personalized greeting

### 3. Book Search
1. Navigate to the search page
2. Try searching by title, author, or genre
3. Browse through the sample books

### 4. Library Management
1. Go to "My Books" to see your reading list
2. Add books to your library
3. Track reading progress

## API Endpoints

### User Management
- `POST /api/users` - Create new user
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users?email={email}` - Get user by email
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Book Management
- `GET /api/books` - Get all books
- `GET /api/books/{id}` - Get book by ID
- `GET /api/books/search?query={query}` - Search books
- `GET /api/books/genre/{genre}` - Get books by genre
- `GET /api/books/series/{seriesName}` - Get books by series

### Book Tracking
- `POST /api/tracking` - Add book to user's list
- `PUT /api/tracking/{id}` - Update reading progress
- `DELETE /api/tracking/{id}` - Remove book from list
- `GET /api/tracking/user/{userId}` - Get user's reading list

## Project Structure

```
src/main/java/com/bookbound/
├── controller/          # REST controllers and page controllers
├── model/              # JPA entities
├── repository/         # Spring Data JPA repositories
├── service/            # Business logic services
├── dto/                # Data transfer objects
└── config/             # Configuration classes

src/main/resources/
├── templates/          # Thymeleaf HTML templates
└── application.properties  # Application configuration
```

## Database Schema

- **users**: User accounts with name, email, password
- **books**: Book information (title, author, genre, pages, series)
- **user_book_tracking**: User's reading progress and status
- **stores**: Purchase locations for books

## Development Notes

- Uses H2 in-memory database for development
- Sample data is automatically loaded on startup
- Passwords are stored in plain text (for demo purposes only)
- No external API dependencies - all data is local

## Next Steps

For production deployment:
1. Switch to PostgreSQL database
2. Implement proper password hashing
3. Add session management
4. Implement Google Books API integration
5. Add comprehensive error handling
6. Add unit and integration tests

