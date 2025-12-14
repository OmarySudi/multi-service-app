# Project Structure

This document describes the reorganized multi-module project structure.

## Overview

The project has been organized into the following structure:

```
multi-service-app/
├── common/                              # Shared utilities and DTOs
├── repository/                          # Shared repository interfaces
├── services/                            # All microservices
│   ├── authentication-service/          # JWT authentication service
│   └── dashboard-service/               # Dashboard API service
├── build.gradle                         # Root build configuration
└── settings.gradle                      # Module definitions
```

## Modules

### 1. Common Module (`/common`)
**Purpose:** Shared utility library with opt-in security, DTOs, and JWT utilities.

**Contains:**
- **Security Auto-Configuration**: `JwtSecurityAutoConfiguration` - Opt-in Spring Boot auto-config
- **Security Filters**: `JwtAuthenticationFilter` - JWT validation with role extraction
- **DTOs**: Request/Response objects (`AuthRequest`, `AuthResponse`, etc.)
- **Utilities**: JWT token generation/validation with role support (`JwtUtil`)
- **Exceptions**: Custom exception classes
- **Build Configuration**: Non-executable JAR (library module)

**Key Features:**
- **Opt-In Security** - Security ONLY enabled when `jwt.security.enabled=true`
- **Flexible Usage** - Services can use utilities without being forced into security
- **Permission-Based Access** - Role-based access control with `@PreAuthorize`
- **Role Management** - JWT tokens include user roles for authorization
- Services making REST calls can use JwtUtil without security overhead
- Auto-configures: JwtAuthenticationFilter, SecurityFilterChain, PasswordEncoder (when enabled)
- Services can override with custom security config if needed

**Used by:** authentication-service, dashboard-service (and any future services)

---

### 2. Repository Module (`/repository`)
**Purpose:** Shared repository interfaces and data access patterns.

**Contains:**
- **BaseRepository**: Base interface with common repository methods
- **Build Configuration**: Non-executable JAR (library module)

**Usage Example:**
```java
public interface UserRepository extends BaseRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
```

**Used by:** Any service that needs database access

---

### 3. Services Parent (`/services`)
**Purpose:** Organizational container for all microservices.

**Contains:**
- **authentication-service**: User authentication and JWT management
- **dashboard-service**: Protected dashboard APIs

---

### 4. Authentication Service (`/services/authentication-service`)
**Purpose:** Handle user authentication, registration, and JWT token management.

**Key Features:**
- User registration with password encryption (BCrypt)
- Login with JWT token generation
- Token validation endpoint (optional, for external services)
- H2 in-memory database (development)
- Spring Security configuration

**Port:** 8081

**API Endpoints:**
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Authenticate and get token
- `GET /api/auth/validate` - Validate JWT token

**Dependencies:**
- common (JWT utilities, DTOs)
- Spring Boot Web
- Spring Security
- Spring Data JPA
- H2 Database

---

### 5. Dashboard Service (`/services/dashboard-service`)
**Purpose:** Provide protected dashboard APIs with permission-based access control.

**Key Features:**
- **Opt-In Security** - Explicitly enables security via `jwt.security.enabled=true`
- **Permission-Based Endpoints** - Uses `@PreAuthorize` for role-based access
- **Zero Boilerplate** - No filter or security config code needed
- **Role-Based Authorization** - Different endpoints require different roles
- No inter-service communication required for token validation

**Port:** 8082

**API Endpoints:**
- `GET /api/dashboard/stats` - Dashboard statistics (Requires: USER or ADMIN)
- `GET /api/dashboard/profile` - User profile (Requires: USER or ADMIN)
- `GET /api/dashboard/admin/users` - Admin endpoint (Requires: ADMIN)
- `GET /api/dashboard/public/health` - Health check (Public)

**Configuration:**
```yaml
jwt:
  secret: shared-secret-key
  security:
    enabled: true  # Explicitly enable security
```

**Dependencies:**
- common (provides opt-in security, JWT utilities, DTOs)
- Spring Boot Web
- Spring Boot Validation

**What's NOT in this service:**
- ❌ No SecurityConfig class
- ❌ No JwtAuthenticationFilter
- ❌ No manual security configuration
- ✅ Security opt-in via configuration
- ✅ Permission control via annotations

---

## Build Commands

### Build All Modules
```bash
./gradlew clean build
```

### Build Specific Module
```bash
./gradlew :common:build
./gradlew :repository:build
./gradlew :services:authentication-service:build
./gradlew :services:dashboard-service:build
```

### Run Services
```bash
# Authentication Service
./gradlew :services:authentication-service:bootRun

# Dashboard Service
./gradlew :services:dashboard-service:bootRun
```

---

## Module Dependencies

```
services/authentication-service
    ↓
  common
  
services/dashboard-service
    ↓
  common

repository
    ↓
  (standalone - can be used by any service)
```

---

## Adding New Services

To add a new service to the project:

1. **Create service directory:**
   ```bash
   mkdir -p services/new-service/src/main/java/com/multiservice/newservice
   mkdir -p services/new-service/src/main/resources
   ```

2. **Create `build.gradle`:**
   ```gradle
   dependencies {
       implementation project(':common')
       implementation project(':repository')  // if needed
       implementation 'org.springframework.boot:spring-boot-starter-web'
       // Add other dependencies
   }
   ```

3. **Update `settings.gradle`:**
   ```gradle
   include 'services:new-service'
   ```

4. **Create Spring Boot application class:**
   ```java
   @SpringBootApplication
   @ComponentScan(basePackages = {"com.multiservice.newservice", "com.multiservice.common"})
   public class NewServiceApplication {
       public static void main(String[] args) {
           SpringApplication.run(NewServiceApplication.class, args);
       }
   }
   ```

5. **Create `application.yml`:**
   ```yaml
   spring:
     application:
       name: new-service
   server:
     port: 808X
   ```

---

## Key Files

| File | Purpose |
|------|---------|
| `/settings.gradle` | Defines all project modules |
| `/build.gradle` | Root build configuration for all subprojects |
| `/common/build.gradle` | Common module configuration (library) |
| `/repository/build.gradle` | Repository module configuration (library) |
| `/services/build.gradle` | Services parent configuration |
| `/services/*/build.gradle` | Individual service configurations |

---

## Benefits of This Structure

1. **Clear Separation**: Services are grouped under `/services` folder
2. **Auto-Configuration**: Security automatically applied via common module
3. **Zero Boilerplate**: No security code needed in services
4. **Shared Code**: Common utilities and DTOs in `/common`
5. **Shared Repositories**: Database access patterns in `/repository`
6. **Scalability**: Easy to add new services (just include common module)
7. **Maintainability**: Clear module boundaries
8. **Reusability**: Shared modules reduce code duplication
9. **Consistency**: All services use the same security implementation
10. **Library Pattern**: Common module acts as a reusable security library

---

## Migration Notes

If you have existing services, the reorganization includes:

- **Moved**: `authentication-service` → `services/authentication-service`
- **Moved**: `dashboard-service` → `services/dashboard-service`
- **Added**: `repository` module for shared repositories
- **Updated**: All Gradle commands now use `services:` prefix
- **Fixed**: JWT utility for JJWT 0.12.x compatibility

---

## Next Steps

1. **Database Configuration**: Replace H2 with production database
2. **Service Discovery**: Add Eureka or Consul
3. **API Gateway**: Add Spring Cloud Gateway
4. **Config Server**: Centralize configuration
5. **Monitoring**: Add Actuator and Prometheus
6. **Containerization**: Create Dockerfile for each service
7. **CI/CD**: Set up automated build and deployment

---

## Documentation

- **README.md**: Project overview and quick start
- **USAGE_GUIDE.md**: Detailed usage instructions
- **API_EXAMPLES.md**: API endpoint examples with curl commands
- **PROJECT_STRUCTURE.md**: This file - architecture documentation
