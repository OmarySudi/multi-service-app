# Opt-In Security with Permission-Based Access Control

## Overview

The `common` module provides **opt-in JWT security** with **role-based access control (RBAC)**. Security is NOT enabled by default - you must explicitly enable it in each service that needs it.

## Key Changes

### 1. Security is Opt-In

**Before (Auto-enabled):**
- Including `common` module → Security automatically enabled
- All endpoints protected by default

**Now (Opt-in):**
- Including `common` module → Just utilities (JwtUtil, DTOs, etc.)
- Security ONLY enabled when you set `jwt.security.enabled=true`
- Services can use common utilities without being forced into security

### 2. Permission-Based Access Control

- JWT tokens now include user **roles**
- Controllers use `@PreAuthorize` annotations for fine-grained access control
- Support for `hasRole()`, `hasAnyRole()`, custom SpEL expressions

## Architecture Pattern

```
Common Module
├── Utilities (Always available)
│   ├── JwtUtil - Token generation/validation
│   ├── DTOs - Request/Response objects
│   └── Exceptions - Custom exceptions
│
└── Security (Opt-in via configuration)
    ├── JwtAuthenticationFilter - JWT validation filter
    ├── JwtSecurityAutoConfiguration - Auto-config
    └── SecurityFilterChain - Default security setup
```

## How to Use

### Services NOT Needing Security

**Example: Notification Service (makes REST calls, no authentication needed)**

**build.gradle:**
```gradle
dependencies {
    implementation project(':common')  // Get utilities only
    implementation 'org.springframework.boot:spring-boot-starter-web'
}
```

**application.yml:**
```yaml
spring:
  application:
    name: notification-service

server:
  port: 8084

# NO jwt.security.enabled - security won't be loaded
```

**Result:** Service can use `JwtUtil`, DTOs, etc., but has NO security filters.

### Services Needing Security

**Example: Dashboard Service (protected endpoints)**

**build.gradle:**
```gradle
dependencies {
    implementation project(':common')  // Get utilities + security
    implementation 'org.springframework.boot:spring-boot-starter-web'
}
```

**application.yml:**
```yaml
spring:
  application:
    name: dashboard-service

server:
  port: 8082

jwt:
  secret: mySecretKeyForJWTTokenGenerationAndValidation12345678901234567890
  security:
    enabled: true  # ← Explicitly enable security
```

**Result:** Service gets full JWT security with auto-configured filters.

## Permission-Based Endpoints

### Using @PreAuthorize Annotations

```java
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    // Accessible by any authenticated user with USER or ADMIN role
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<DashboardStats> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    // Only ADMIN role can access
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminOnly() {
        return ResponseEntity.ok("Admin access granted");
    }

    // Public endpoint - no authentication required
    @GetMapping("/public/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Service is running");
    }

    // Custom permission expression
    @GetMapping("/profile/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.name")
    public ResponseEntity<UserProfile> getProfile(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getProfile(userId));
    }
}
```

### Available @PreAuthorize Expressions

| Expression | Description | Example |
|------------|-------------|---------|
| `isAuthenticated()` | User is authenticated | `@PreAuthorize("isAuthenticated()")` |
| `hasRole('ROLE')` | User has specific role | `@PreAuthorize("hasRole('ADMIN')")` |
| `hasAnyRole('R1', 'R2')` | User has any of the roles | `@PreAuthorize("hasAnyRole('USER', 'ADMIN')")` |
| `hasAuthority('AUTH')` | User has specific authority | `@PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")` |
| `permitAll()` | Allow everyone | `@PreAuthorize("permitAll()")` |
| Custom SpEL | Complex logic | `@PreAuthorize("#userId == authentication.name")` |

## Role Management

### 1. Assigning Roles During Registration

**AuthService.java:**
```java
public AuthResponse register(RegisterRequest request) {
    User user = User.builder()
        .username(request.getUsername())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .roles("USER")  // ← Default role
        .build();

    userRepository.save(user);

    List<String> roles = Arrays.asList("USER");
    String token = jwtUtil.generateToken(user.getEmail(), roles);  // Include roles in token

    return AuthResponse.builder()
        .token(token)
        .type("Bearer")
        .email(user.getEmail())
        .build();
}
```

### 2. Multiple Roles

Store roles as comma-separated string in database:

```java
user.setRoles("USER,ADMIN");  // Multiple roles
```

### 3. JWT Token with Roles

**Token Generation:**
```java
List<String> roles = Arrays.asList("USER", "ADMIN");
String token = jwtUtil.generateToken(email, roles);
```

**Token Contents:**
```json
{
  "sub": "user@example.com",
  "roles": ["USER", "ADMIN"],
  "iat": 1234567890,
  "exp": 1234654290
}
```

**Token Extraction:**
```java
String email = jwtUtil.extractEmail(token);
List<String> roles = jwtUtil.extractRoles(token);
```

## Security Configuration Levels

### Level 1: Completely Disabled (No Security)

**application.yml:**
```yaml
# NO jwt.security.enabled
```

**Result:**
- No security filters
- No authentication required
- Service can use common utilities
- Useful for: Internal services, notification services, etc.

### Level 2: Enabled with Default Config

**application.yml:**
```yaml
jwt:
  secret: your-secret-key
  security:
    enabled: true
```

**Result:**
- JWT authentication filter active
- Default security rules:
  - `/api/*/public/**` - Public
  - `/h2-console/**` - Public
  - `/actuator/**` - Public
  - Everything else - Authenticated
- Method-level security with `@PreAuthorize`

### Level 3: Custom Security Configuration

**application.yml:**
```yaml
jwt:
  secret: your-secret-key
  security:
    enabled: true
```

**SecurityConfig.java:**
```java
@Configuration
@EnableWebSecurity
public class CustomSecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtFilter) throws Exception {
        
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()  // Custom rules
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }
}
```

**Result:**
- Custom authorization rules
- Overrides auto-configured security chain
- Full control over endpoints

## Example: Service Making REST Calls

**Problem:** You have a service that makes REST calls to other services. You don't want it to require authentication for its own endpoints, but it needs to use `JwtUtil` to include tokens when calling other services.

**Solution:**

**build.gradle:**
```gradle
dependencies {
    implementation project(':common')  // Get JwtUtil
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-webflux'  // For WebClient
}
```

**application.yml:**
```yaml
spring:
  application:
    name: integration-service

server:
  port: 8085

# NO jwt.security.enabled - this service is NOT protected

jwt:
  secret: mySecretKeyForJWTTokenGenerationAndValidation12345678901234567890  # For creating tokens
```

**Service Code:**
```java
@Service
@RequiredArgsConstructor
public class IntegrationService {

    private final JwtUtil jwtUtil;  // From common module
    private final WebClient webClient;

    public DashboardStats fetchDashboardStats(String userEmail) {
        // Generate token for service-to-service communication
        String token = jwtUtil.generateToken(userEmail, Arrays.asList("SERVICE"));

        return webClient.get()
            .uri("http://localhost:8082/api/dashboard/stats")
            .header("Authorization", "Bearer " + token)  // Include JWT
            .retrieve()
            .bodyToMono(DashboardStats.class)
            .block();
    }
}
```

**Result:**
- Service can use `JwtUtil` from common module
- Service endpoints are NOT protected (no security enabled)
- Service can make authenticated calls to other services

## Common Use Cases

### Use Case 1: Public API Service

**Scenario:** Service provides public APIs, no authentication needed

**Configuration:**
```yaml
# NO jwt.security.enabled
```

### Use Case 2: Protected Dashboard

**Scenario:** Dashboard with user-specific data

**Configuration:**
```yaml
jwt:
  security:
    enabled: true
```

**Controller:**
```java
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@GetMapping("/dashboard")
public ResponseEntity<Dashboard> getDashboard(Authentication auth) {
    return ResponseEntity.ok(dashboardService.getForUser(auth.getName()));
}
```

### Use Case 3: Admin Panel

**Scenario:** Admin-only management interface

**Controller:**
```java
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")  // All endpoints require ADMIN
public class AdminController {
    
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
```

### Use Case 4: Mixed Access Levels

**Scenario:** Service with public, user, and admin endpoints

**Controller:**
```java
@RestController
@RequestMapping("/api/posts")
public class PostController {
    
    // Public - anyone can view
    @GetMapping
    public ResponseEntity<List<Post>> getPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }
    
    // USER - authenticated users can create
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Post> createPost(@RequestBody PostRequest request, Authentication auth) {
        return ResponseEntity.ok(postService.create(request, auth.getName()));
    }
    
    // ADMIN - only admins can delete
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

## Migration Guide

### From Auto-Enabled to Opt-In

**Before (all services had security):**
```yaml
# Security was enabled just by including common module
```

**After (must explicitly enable):**
```yaml
jwt:
  security:
    enabled: true  # ← Must add this
```

### Adding Permission-Based Access

**1. Add roles to User model:**
```java
@Column(name = "roles")
private String roles;  // "USER,ADMIN"
```

**2. Include roles in token generation:**
```java
List<String> roles = Arrays.asList("USER", "ADMIN");
String token = jwtUtil.generateToken(email, roles);
```

**3. Add @PreAuthorize to endpoints:**
```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/dashboard")
public ResponseEntity<AdminDashboard> getAdminDashboard() {
    // ...
}
```

## Benefits of Opt-In Security

### 1. **Flexibility**
- Services can use common utilities without security
- Make REST calls without being forced into JWT
- Choose which services need protection

### 2. **Clear Intent**
- `jwt.security.enabled=true` explicitly shows security is on
- No surprises from auto-configuration
- Easy to see which services are protected

### 3. **Simpler Integration Services**
- Services that integrate with external APIs don't need auth
- Internal services can remain lightweight
- Only public-facing services need security

### 4. **Fine-Grained Control**
- Role-based access at method level
- Different permissions for different endpoints
- Support for complex authorization logic

## Troubleshooting

### Issue: Security not working after enabling

**Check:**
1. `jwt.security.enabled=true` is set
2. JWT secret is configured
3. Service includes `common` module in dependencies

### Issue: @PreAuthorize not working

**Reason:** Method security not enabled

**Solution:** Ensure `@EnableMethodSecurity` is present (auto-configured in common module when security is enabled)

### Issue: Getting 403 Forbidden with valid token

**Check:**
1. Does token contain required roles?
2. Do roles match `@PreAuthorize` requirements?
3. Are roles prefixed with `ROLE_`? (auto-added by filter)

### Issue: Service making REST calls gets 401

**Reason:** Target service requires authentication

**Solution:** Include JWT token in request:
```java
.header("Authorization", "Bearer " + token)
```

## Best Practices

1. **Enable security only where needed** - Don't secure internal services unnecessarily
2. **Use least privilege** - Give users minimum roles needed
3. **Consistent role naming** - Use `ADMIN`, `USER`, `MANAGER` (uppercase)
4. **Document permissions** - Document which roles can access which endpoints
5. **Test authorization** - Write tests for permission-based access
6. **Secure secrets** - Use environment variables for JWT secrets in production
7. **Role hierarchy** - Consider implementing role hierarchy (ADMIN includes USER permissions)

## Summary

- **Opt-In Security**: Must set `jwt.security.enabled=true` to enable
- **Common Module**: Provides utilities without forcing security
- **Role-Based Access**: Use `@PreAuthorize` for fine-grained control
- **Flexible**: Services can use common utilities without authentication
- **Clear**: Explicit configuration makes intent obvious
