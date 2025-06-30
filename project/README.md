# Spring Boot Application with JWT Security

This is a Spring Boot application with JWT-based authentication and authorization.

## Features

- JWT Authentication
- Role-based Authorization (ADMIN, USER)
- Password Encryption with BCrypt
- RESTful APIs for Customer, Item, Quotation management
- PDF Invoice Generation
- MySQL Database Integration

## Security Configuration

### Roles
- **ADMIN**: Full access to all endpoints including user management
- **USER**: Access to customer, item, quotation, and invoice endpoints

### Protected Endpoints
- `/api/auth/**` - Public (login, register)
- `/api/logins/**` - ADMIN only
- `/api/customer/**` - ADMIN, USER
- `/api/items/**` - ADMIN, USER
- `/api/quat/**` - ADMIN, USER
- `/api/qitem/**` - ADMIN, USER
- `/api/invoices/**` - ADMIN, USER

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `GET /api/auth/user` - Get current user info

### Login Request Format
```json
{
    "username": "your_username",
    "password": "your_password"
}
```

### Registration Request Format
```json
{
    "username": "new_username",
    "password": "new_password",
    "email": "user@example.com",
    "firstName": "First",
    "lastName": "Last",
    "userType": "USER"
}
```

### JWT Response Format
```json
{
    "jwtToken": "eyJhbGciOiJIUzUxMiJ9...",
    "username": "your_username"
}
```

## Usage

1. **Register a new user:**
   ```bash
   curl -X POST http://localhost:8080/api/auth/register \
   -H "Content-Type: application/json" \
   -d '{
     "username": "testuser",
     "password": "password123",
     "email": "test@example.com",
     "firstName": "Test",
     "lastName": "User",
     "userType": "USER"
   }'
   ```

2. **Login:**
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
   -H "Content-Type: application/json" \
   -d '{
     "username": "testuser",
     "password": "password123"
   }'
   ```

3. **Access protected endpoints:**
   ```bash
   curl -X GET http://localhost:8080/api/customer \
   -H "Authorization: Bearer YOUR_JWT_TOKEN"
   ```

## Database Setup

Make sure your MySQL database is running and update the connection details in `application.properties`:

```properties
spring.datasource.url = jdbc:mysql://localhost:3306/your_database
spring.datasource.username = your_username
spring.datasource.password = your_password
```

## Running the Application

```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

## Default Admin User

You can create an admin user by registering with `userType: "ADMIN"` or by directly inserting into the database with the password encrypted using BCrypt.