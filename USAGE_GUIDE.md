# Usage Guide

## Quick Start

### Prerequisites
- JDK 17 or higher
- Gradle 8.x (or use the included Gradle wrapper)

### Building the Project

Navigate to the project root directory:

```bash
cd "/Users/fadhilichambo/IdeaProjects/Sample projects/multi-service-app"
```

Build all modules:

```bash
./gradlew clean build
```

### Running the Services

#### Option 1: Run from Command Line

**Terminal 1 - Authentication Service:**
```bash
./gradlew :services:authentication-service:bootRun
```

**Terminal 2 - Dashboard Service:**
```bash
./gradlew :services:dashboard-service:bootRun
```

#### Option 2: Run from IntelliJ IDEA

1. Open the project in IntelliJ IDEA
2. Wait for Gradle to sync
3. Navigate to:
   - `services/authentication-service/src/main/java/com/multiservice/auth/AuthenticationServiceApplication.java`
   - Right-click → Run
4. Navigate to:
   - `services/dashboard-service/src/main/java/com/multiservice/dashboard/DashboardServiceApplication.java`
   - Right-click → Run

### Verifying the Services are Running

**Check Authentication Service:**
```bash
curl http://localhost:8081/api/auth/validate
# Should return an error (expected, as no token provided)
```

**Check Dashboard Service:**
```bash
curl http://localhost:8082/api/dashboard/public/health
# Should return: "Dashboard Service is running!"
```

## Architecture Overview

### Module Structure

```
multi-service-app/
├── common/                              # Shared utilities module
│   ├── dto/                             # Data Transfer Objects
│   │   ├── AuthRequest.java
│   │   ├── AuthResponse.java
│   │   ├── RegisterRequest.java
│   │   ├── ValidationResponse.java
│   │   └── ErrorResponse.java
│   ├── exception/                       # Custom exceptions
│   │   └── CustomException.java
│   └── util/                            # Utilities
│       └── JwtUtil.java                 # JWT generation & validation
├── repository/                          # Shared repository module
│   └── BaseRepository.java              # Base repository interface
├── services/                            # Service modules
│   ├── authentication-service/          # Authentication & JWT
│   │   ├── controller/
│   │   │   └── AuthController.java      # REST endpoints
│   │   ├── service/
│   │   │   └── AuthService.java         # Business logic
│   │   ├── repository/
│   │   │   └── UserRepository.java      # Data access
│   │   ├── model/
│   │   │   └── User.java                # Entity
│   │   └── config/
│   │       └── SecurityConfig.java      # Security configuration
│   └── dashboard-service/               # Protected Dashboard APIs
│       ├── controller/
│       │   └── DashboardController.java
│       ├── service/
│       │   ├── DashboardService.java
│       │   └── AuthenticationService.java  # Calls auth service
│       ├── security/
│       │   └── JwtAuthenticationFilter.java # JWT validation
│       └── config/
│           ├── SecurityConfig.java
│           └── WebClientConfig.java

```

### Communication Flow

1. **User Registration/Login:**
   ```
   Client → Authentication Service → Generate JWT → Return Token
   ```

2. **Accessing Protected Dashboard Endpoints:**
   ```
   Client (with JWT) → Dashboard Service 
   → JwtAuthenticationFilter 
   → Validate with Auth Service 
   → Allow/Deny Access
   ```

## Key Features

### 1. Common Module
- **Shared DTOs**: Reusable request/response objects
- **JWT Utility**: Centralized JWT generation and validation logic
- **Custom Exceptions**: Consistent error handling across services

### 2. Authentication Service
- **User Registration**: Create new user accounts with encrypted passwords
- **Login**: Authenticate users and issue JWT tokens
- **Token Validation**: Verify JWT tokens for other services
- **H2 Database**: In-memory database for development
- **BCrypt**: Password encryption

### 3. Dashboard Service
- **JWT Protection**: All endpoints (except public) require valid JWT
- **Service-to-Service Communication**: Validates tokens via Auth Service
- **WebClient**: Non-blocking HTTP client for inter-service calls
- **Mock Data**: Sample dashboard statistics

## Configuration

### Authentication Service (application.yml)

```yaml
server:
  port: 8081

jwt:
  secret: mySecretKeyForJWTTokenGenerationAndValidation12345678901234567890
  expiration: 86400000 # 24 hours

spring:
  datasource:
    url: jdbc:h2:mem:authdb
```

### Dashboard Service (application.yml)

```yaml
server:
  port: 8082

jwt:
  secret: mySecretKeyForJWTTokenGenerationAndValidation12345678901234567890
  expiration: 86400000 # 24 hours
```

**Important**: The JWT secret must match the authentication service for token validation to work.

## Development Tips

### Adding New Endpoints

**Authentication Service:**
```java
@GetMapping("/api/auth/newEndpoint")
public ResponseEntity<?> newEndpoint() {
    // Implementation
}
```

**Dashboard Service (Protected):**
```java
@GetMapping("/api/dashboard/newEndpoint")
public ResponseEntity<?> newEndpoint(Authentication authentication) {
    String email = authentication.getName(); // Get user from JWT
    // Implementation
}
```

### Adding Dependencies

Edit the module's `build.gradle`:

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-mongodb'
    // Add more dependencies
}
```

Then refresh Gradle:
```bash
./gradlew clean build
```

### Accessing H2 Console

When authentication service is running, visit:
```
http://localhost:8081/h2-console
```

Connection details:
- JDBC URL: `jdbc:h2:mem:authdb`
- Username: `sa`
- Password: (leave empty)

## Understanding Auto-Configuration

### How JWT Security Works

The `common` module provides **automatic JWT security configuration** using Spring Boot's auto-configuration mechanism. This means services get JWT authentication without writing any security code.

### What Gets Auto-Configured

When you include the `common` module, it automatically provides:

1. **JwtAuthenticationFilter** - Validates JWT tokens on every request
2. **SecurityFilterChain** - Configures Spring Security with JWT
3. **PasswordEncoder** - BCrypt password encoder
4. **JwtUtil** - Token generation and validation utilities

### For New Services

To add JWT protection to a new service:

**Step 1: Add dependency**
```gradle
dependencies {
    implementation project(':common')
    implementation 'org.springframework.boot:spring-boot-starter-web'
}
```

**Step 2: Add JWT config**
```yaml
jwt:
  secret: mySecretKeyForJWTTokenGenerationAndValidation12345678901234567890
  expiration: 86400000
```

**That's it!** Your service is now JWT-protected. No filters, no security config needed.

### Customizing Security (Optional)

If you need custom authorization rules (like the auth service does):

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()  // Custom rules
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        return http.build();
    }
}
```

This overrides the auto-configured security chain with your custom rules.

### Disabling Auto-Configuration (Not Recommended)

If you need to completely disable JWT security for a service:

```yaml
jwt:
  security:
    enabled: false
```

## Production Considerations

### Security Enhancements

1. **JWT Secret**: Use environment variables instead of hardcoded secrets
   ```yaml
   jwt:
     secret: ${JWT_SECRET}
   ```

2. **Database**: Replace H2 with production database (PostgreSQL, MySQL)
   ```gradle
   runtimeOnly 'org.postgresql:postgresql'
   ```

3. **HTTPS**: Enable SSL/TLS for production

4. **Token Refresh**: Implement refresh token mechanism

5. **Rate Limiting**: Add request throttling

### Scalability

1. **Service Discovery**: Use Eureka or Consul
2. **API Gateway**: Add Spring Cloud Gateway
3. **Config Server**: Centralize configuration
4. **Load Balancing**: Deploy multiple instances

### Monitoring

1. **Spring Boot Actuator**: Add health checks and metrics
2. **Logging**: Configure ELK stack or similar
3. **Distributed Tracing**: Use Sleuth + Zipkin

## Troubleshooting

### Services Won't Start

**Check ports:**
```bash
lsof -i :8081
lsof -i :8082
```

**Kill processes if needed:**
```bash
kill -9 <PID>
```

### Authentication Issues

1. Verify token format: `Bearer <token>`
2. Check token expiration (24 hours by default)
3. Ensure both services are running

### Build Failures

```bash
./gradlew clean
./gradlew build --refresh-dependencies
```

## Next Steps

1. **Add Database Persistence**: Replace H2 with PostgreSQL/MySQL
2. **Implement Refresh Tokens**: Allow long-lived sessions
3. **Add Role-Based Access Control**: Implement user roles and permissions
4. **Create Docker Compose**: Containerize the services
5. **Add API Gateway**: Centralize routing and cross-cutting concerns
6. **Implement Circuit Breaker**: Handle service failures gracefully
7. **Add Unit & Integration Tests**: Ensure code quality

## Support

For issues or questions, refer to:
- Spring Boot Documentation: https://spring.io/projects/spring-boot
- Spring Security: https://spring.io/projects/spring-security
- JWT: https://jwt.io/
