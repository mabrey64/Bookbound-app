# BookBound Backend API

**BookBound** is a comprehensive book tracking application backend built with Java Spring Boot. It allows users to track their reading progress, discover new books, manage series progression, and organize their personal reading collections.

## 🚀 Features

- **User Management**: Create and manage user accounts
- **Book Catalog**: Comprehensive book database with series support
- **Reading Progress Tracking**: Track current page, reading status, and completion dates
- **Series Progression**: Smart recommendations for next books in series
- **"To Read Next" Tagging**: Manual priority control for reading queue
- **Google Books Integration**: Discover new books via external API
- **Advanced Search & Filtering**: Find books by title, author, genre, page count, series
- **Purchase Location Tracking**: Store links to where books can be purchased

## 📋 Prerequisites

- **Java 17+**
- **Maven 3.6+**
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code

## 🛠️ Setup & Installation

### 1. Clone the Repository
```bash
git clone <repository-url>
cd BookBound/Bookbound-app
```

### 2. Build the Project
```bash
./mvnw clean compile
```

### 3. Run the Application
```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8081`

### 4. Access H2 Database Console (Development)
- URL: `http://localhost:8081/h2-console`
- JDBC URL: `jdbc:h2:mem:bookbound`
- Username: `sa`
- Password: (leave blank)

## 📚 API Documentation

### Base URL
```
http://localhost:8081/api
```

### 👤 User Management

#### Create User
```http
POST /users
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com"
}
```

#### Get User by ID
```http
GET /users/{userId}
```

#### Find User by Email
```http
GET /users?email=john@example.com
```

#### Search Users by Name
```http
GET /users/search?name=John
```

### 📖 Book Management

#### Get All Books
```http
GET /books
```

#### Get Book by ID
```http
GET /books/{bookId}
```

#### Create New Book
```http
POST /books
Content-Type: application/json

{
  "title": "The Great Gatsby",
  "author": "F. Scott Fitzgerald",
  "genre": "Classic Literature",
  "totalPages": 180,
  "seriesName": null,
  "seriesOrder": null
}
```

#### Advanced Book Search
```http
POST /books/search
Content-Type: application/json

{
  "title": "Foundation",
  "author": "Asimov",
  "genre": "Science Fiction",
  "minPages": 200,
  "maxPages": 500,
  "seriesName": "Foundation"
}
```

#### Search by Specific Criteria
```http
GET /books/search/title?q=Dune
GET /books/search/author?q=Asimov
GET /books/search/pages?min=200&max=400
```

#### Get Metadata
```http
GET /books/metadata/genres
GET /books/metadata/authors
GET /books/metadata/series
```

### 🌐 Google Books Integration

#### Search External Books
```http
GET /books/google?query=science fiction
```

#### Advanced External Search
```http
GET /books/google/advanced?title=Dune&author=Herbert&subject=fiction
```

### 📊 Reading Progress Tracking

#### Add Book to Reading List
```http
POST /users/{userId}/track
Content-Type: application/json

{
  "bookId": "book-uuid",
  "status": "READING",
  "toReadNext": true
}
```

#### Get User's Tracking List
```http
GET /users/{userId}/track
GET /users/{userId}/track?status=READING
```

#### Get Books by Status
```http
GET /users/{userId}/to-read
GET /users/{userId}/reading
GET /users/{userId}/completed
```

#### Update Reading Progress
```http
PUT /track/{trackingId}
Content-Type: application/json

{
  "currentPage": 150
}
```

#### Update Reading Status
```http
PUT /track/{trackingId}
Content-Type: application/json

{
  "status": "COMPLETED"
}
```

#### Mark as Finished
```http
PUT /track/{trackingId}/finish
```

#### Start Reading
```http
PUT /track/{trackingId}/start
```

### 🏷️ "To Read Next" Feature

#### Mark/Unmark as "To Read Next"
```http
PUT /track/{trackingId}/to-read-next
Content-Type: application/json

{
  "toReadNext": true
}
```

#### Get "To Read Next" Books
```http
GET /users/{userId}/to-read
```

### 📈 Series Progress & Recommendations

#### Get Series Progress
```http
GET /users/{userId}/series-progress?seriesName=Foundation
```

**Response Example:**
```json
{
  "seriesName": "Foundation",
  "totalBooksInSeries": 3,
  "booksRead": 1,
  "booksInProgress": 0,
  "nextBook": {
    "id": "book-uuid",
    "title": "Foundation and Empire",
    "seriesOrder": 2
  },
  "genreSuggestions": [...],
  "completionPercentage": 33.33,
  "seriesStarted": true,
  "seriesCompleted": false
}
```

#### Get Reading Recommendations
```http
GET /users/{userId}/recommendations?limit=5
```

#### Get Reading Statistics
```http
GET /users/{userId}/stats
```

## 🗄️ Database Schema

### Core Entities

- **User**: User accounts and profiles
- **Book**: Book catalog with series support
- **UserBookTracking**: Reading progress tracking (junction table)
- **Store**: Purchase location information

### Key Relationships

- User ↔ UserBookTracking (One-to-Many)
- Book ↔ UserBookTracking (One-to-Many)
- Book ↔ Store (One-to-Many)

## 🧪 Sample Data

The application automatically initializes with sample data including:
- **3 Sample Users**: Alice, Bob, and Carol with different reading preferences
- **30+ Books**: Across genres (Sci-Fi, Fantasy, Mystery, Romance, etc.)
- **Series Collections**: Foundation, Lord of the Rings, Game of Thrones, etc.
- **Sample Tracking Data**: Various reading statuses and progress examples

## 🔧 Configuration

### Application Properties
```properties
# Server Configuration
server.port=8081

# H2 Database (Development)
spring.datasource.url=jdbc:h2:mem:bookbound
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# H2 Console (Development)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

### Production Database Setup
For production, update `application.properties`:
```properties
# PostgreSQL Example
spring.datasource.url=jdbc:postgresql://localhost:5432/bookbound
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=validate
```

## 🌐 Frontend Integration

### Thymeleaf Templates
The application includes Thymeleaf templates in `src/main/resources/templates/`:
- `landing.html` - Landing page
- `login.html` - User login
- `signup.html` - User registration  
- `home.html` - Dashboard

### API Integration
Frontend can integrate via:
1. **REST API calls** (fetch/axios) for SPA functionality
2. **Thymeleaf server-side rendering** for traditional web pages
3. **Hybrid approach** combining both methods

### CORS Configuration
For frontend development, CORS is configured to allow cross-origin requests.

## 🚀 Deployment

### Local Development
```bash
./mvnw spring-boot:run
```

### Production Build
```bash
./mvnw clean package
java -jar target/bookbound-app-1.0.0.jar
```

### Docker (Optional)
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/bookbound-app-1.0.0.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🧪 Testing

### Manual Testing with Postman
1. Import the API endpoints into Postman
2. Create a user account
3. Add books to tracking
4. Test series progression
5. Verify "To Read Next" functionality

### Automated Testing
```bash
./mvnw test
```

## 📁 Project Structure

```
src/main/java/com/bookbound/
├── config/           # Configuration classes
├── controller/       # REST API controllers
├── dto/             # Data Transfer Objects
├── model/           # JPA Entities
├── repository/      # Data Access Layer
├── service/         # Business Logic Layer
└── BookboundAppApplication.java
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 🆘 Support

For issues or questions:
1. Check the H2 console for database state
2. Review application logs
3. Verify API endpoints with Postman
4. Check sample data initialization

---

**BookBound Backend** - Built with ❤️ using Java Spring Boot

