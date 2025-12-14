# JWT Security Library - Auto-Configuration Guide

## Overview

The `common` module acts as a **reusable security library** that automatically configures JWT authentication for all services that include it. This eliminates the need for boilerplate security code in each service.

## Architecture Pattern

This implementation follows the **Spring Boot Starter** pattern, where:
- Common module = Security library/starter
- Services = Consumers of the library
- Zero configuration required in consumer services
- Security is automatically applied via Spring Boot auto-configuration

## How It Works

### 1. Auto-Configuration Class

Located at: `common/src/main/java/com/multiservice/common/config/JwtSecurityAutoConfiguration.java`

```java
@AutoConfiguration
@ConditionalOnClass({HttpSecurity.class, JwtUtil.class})
@ConditionalOnProperty(name = "jwt.security.enabled", havingValue = "true", matchIfMissing = true)
@EnableWebSecurity
public class JwtSecurityAutoConfiguration {
    // Auto-configures: JwtAuthenticationFilter, SecurityFilterChain, PasswordEncoder
}
```

**Key Features:**
- `@AutoConfiguration` - Spring Boot detects and loads this automatically
- `@ConditionalOnClass` - Only activates if Spring Security is on classpath
- `@ConditionalOnProperty` - Can be disabled via configuration
- `@ConditionalOnMissingBean` - Services can override with custom beans

### 2. Auto-Configuration Registration

Located at: `common/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

```
com.multiservice.common.config.JwtSecurityAutoConfiguration
```

This file tells Spring Boot to load the auto-configuration class.

### 3. JWT Authentication Filter

Located at: `common/src/main/java/com/multiservice/common/security/JwtAuthenticationFilter.java`

```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(...) {
        // Extract JWT from Authorization header
        // Validate token using JwtUtil
        // Set authentication in SecurityContext
    }
}
```

**Responsibilities:**
- Extracts JWT token from `Authorization: Bearer <token>` header
- Validates token using shared `JwtUtil`
- Sets Spring Security authentication context
- Handles validation errors gracefully

## What Gets Auto-Configured

When a service includes the `common` module, it automatically gets:

### 1. JwtAuthenticationFilter Bean
- Validates JWT tokens on every request
- Extracts user email from token
- Sets authentication in SecurityContext

### 2. SecurityFilterChain Bean
- Configures Spring Security
- Permits: `/api/*/public/**`, `/h2-console/**`, `/actuator/**`
- Requires authentication for all other endpoints
- Disables CSRF (stateless JWT)
- Stateless session management

### 3. PasswordEncoder Bean
- BCrypt password encoder
- Used for password hashing
- Services can override if needed

### 4. JwtUtil Bean
- Already configured in common module
- Used by filter for token validation
- Used by services for token generation

## Usage in Services

### Dashboard Service (Zero Configuration)

The dashboard service has **NO security code**:

```
dashboard-service/
├── src/main/java/com/multiservice/dashboard/
│   ├── DashboardServiceApplication.java  ← Just the app
│   ├── controller/                       ← Controllers only
│   └── service/                          ← Business logic only
└── build.gradle                          ← Includes common module
```

**build.gradle:**
```gradle
dependencies {
    implementation project(':common')  // ← Security auto-configures!
    implementation 'org.springframework.boot:spring-boot-starter-web'
}
```

**application.yml:**
```yaml
jwt:
  secret: mySecretKeyForJWTTokenGenerationAndValidation12345678901234567890
```

That's it! The service is now JWT-protected.

### Authentication Service (Custom Configuration)

The auth service needs custom rules (permit /api/auth/**), so it provides its own SecurityFilterChain:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        // Custom authorization rules
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()  // Custom!
            .anyRequest().authenticated()
        );
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

Because of `@ConditionalOnMissingBean(SecurityFilterChain.class)`, the auto-configured SecurityFilterChain won't be created, and the auth service's custom one will be used instead.

## Conditional Beans Explained

```java
@Bean
@ConditionalOnMissingBean(SecurityFilterChain.class)
public SecurityFilterChain securityFilterChain(...) {
    // This bean is created ONLY IF no other SecurityFilterChain exists
}
```

**What this means:**
- If a service defines its own `SecurityFilterChain` bean → Use that (custom)
- If a service has no `SecurityFilterChain` bean → Use auto-configured one (default)

Same applies to:
- `PasswordEncoder` - Can be overridden
- `JwtAuthenticationFilter` - Can be overridden (by bean name)

## Adding JWT Protection to a New Service

### Step 1: Create Service

```bash
mkdir -p services/new-service/src/main/java/com/multiservice/newservice
```

### Step 2: Add build.gradle

```gradle
dependencies {
    implementation project(':common')
    implementation 'org.springframework.boot:spring-boot-starter-web'
}
```

### Step 3: Create Application Class

```java
@SpringBootApplication
@ComponentScan(basePackages = {"com.multiservice.newservice", "com.multiservice.common"})
public class NewServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NewServiceApplication.class, args);
    }
}
```

### Step 4: Add application.yml

```yaml
spring:
  application:
    name: new-service

server:
  port: 8083

jwt:
  secret: mySecretKeyForJWTTokenGenerationAndValidation12345678901234567890
  expiration: 86400000
```

### Step 5: Create Controllers

```java
@RestController
@RequestMapping("/api/newservice")
public class NewServiceController {
    
    @GetMapping("/protected")
    public ResponseEntity<String> protectedEndpoint(Authentication auth) {
        String email = auth.getName(); // User from JWT
        return ResponseEntity.ok("Hello, " + email);
    }
    
    @GetMapping("/public/health")
    public ResponseEntity<String> publicEndpoint() {
        return ResponseEntity.ok("Service is running");
    }
}
```

**Done!** The service is fully JWT-protected without writing any security code.

## Configuration Options

### Enable/Disable Auto-Configuration

```yaml
jwt:
  security:
    enabled: true  # Set to false to disable auto-configuration
```

### JWT Configuration

```yaml
jwt:
  secret: ${JWT_SECRET:default-secret-key-for-development}
  expiration: 86400000  # 24 hours in milliseconds
```

## Customization Scenarios

### Scenario 1: Different Public Paths

**Problem:** Need to allow public access to `/api/myservice/health`

**Solution:** Override SecurityFilterChain

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
                .requestMatchers("/api/myservice/health").permitAll()
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

### Scenario 2: Role-Based Authorization

**Problem:** Need role-based access control

**Solution:** Extend JwtAuthenticationFilter or create custom filter

```java
@Bean
public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtil jwtUtil) {
    return new JwtAuthenticationFilter(jwtUtil) {
        @Override
        protected void doFilterInternal(...) {
            // Extract roles from JWT claims
            // Set authorities in authentication
            super.doFilterInternal(request, response, filterChain);
        }
    };
}
```

### Scenario 3: No Security Needed

**Problem:** Service doesn't need authentication (e.g., public API)

**Solution:** Disable auto-configuration

```yaml
jwt:
  security:
    enabled: false
```

Or exclude security dependency:
```gradle
dependencies {
    implementation project(':common') {
        exclude group: 'org.springframework.boot', module: 'spring-boot-starter-security'
    }
}
```

## Benefits of This Approach

### 1. **Zero Boilerplate**
- Services don't need security filters
- Services don't need security configurations
- Services don't need authentication logic

### 2. **Consistency**
- All services use the same security implementation
- Centralized security logic in one place
- Easy to update security across all services

### 3. **Library Pattern**
- Common module acts as a reusable library
- Import once, security works everywhere
- Follows Spring Boot conventions

### 4. **Flexibility**
- Services can override any bean
- Services can disable auto-configuration
- Services can customize as needed

### 5. **Maintainability**
- Security logic in one location
- Changes propagate to all services
- No code duplication

### 6. **Testability**
- Security logic can be tested once
- Services focus on business logic
- Clear separation of concerns

## Comparison: Before vs After

### Before (Manual Configuration)

**Each service needed:**
```
service/
├── config/
│   └── SecurityConfig.java          ← 40 lines
├── security/
│   └── JwtAuthenticationFilter.java ← 80 lines
└── service/
    └── AuthenticationService.java   ← 50 lines
```

**Total:** ~170 lines of security code per service

### After (Auto-Configuration)

**Each service needs:**
```
service/
└── (no security code)
```

**Total:** 0 lines of security code per service

**All security logic:** In common module (~150 lines total, shared by all services)

## Troubleshooting

### Issue: Auto-configuration not working

**Check:**
1. Is `common` module included in dependencies?
2. Is `jwt.secret` configured in application.yml?
3. Is auto-configuration enabled? (`jwt.security.enabled=true`)
4. Check logs for "Auto-configuring JWT Security Filter Chain"

### Issue: Custom SecurityFilterChain not being used

**Reason:** Bean name might conflict

**Solution:** Ensure your SecurityFilterChain bean is properly annotated with `@Bean`

### Issue: Getting 403 Forbidden

**Check:**
1. Is JWT token included in Authorization header?
2. Is the token valid? (not expired, correct signature)
3. Is the JWT secret the same across all services?

## Best Practices

1. **Same JWT Secret:** All services must use the same JWT secret
2. **Environment Variables:** Use `${JWT_SECRET}` in production
3. **Public Endpoints:** Use consistent pattern like `/api/*/public/**`
4. **Override Sparingly:** Only override auto-configuration when truly needed
5. **Test Locally:** Test security locally before deploying

## Conclusion

The common module provides a powerful, reusable security library that follows Spring Boot's auto-configuration pattern. Services get JWT authentication automatically without writing any security code, while still maintaining the flexibility to customize when needed.

This approach:
- ✅ Eliminates boilerplate
- ✅ Ensures consistency
- ✅ Simplifies maintenance
- ✅ Follows Spring Boot conventions
- ✅ Provides flexibility when needed
