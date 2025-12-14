# Repository Module

This module contains shared repository interfaces and common data access patterns used across multiple services.

## Purpose

- Define base repository interfaces with common query methods
- Share database-related utilities
- Provide consistent data access patterns across services

## Usage

Services can depend on this module to inherit common repository functionality:

```gradle
dependencies {
    implementation project(':repository')
}
```

## Example

Create a repository that extends the base:

```java
public interface UserRepository extends BaseRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
```

## Best Practices

1. Keep repositories focused and single-purpose
2. Use derived query methods when possible
3. Add custom queries using `@Query` for complex operations
4. Document any custom query methods
