# Permission-Based Security Architecture

## Overview

The application uses a **permission-based access control** system where:
- **Roles** are groups of **permissions**
- **Permissions** are granular capabilities (e.g., `dashboard:view`, `payment:initiate`)
- Controllers use **permission guards** via `@PreAuthorize` annotations
- JWT tokens store user roles, which are converted to permissions at runtime

## Architecture

```
User
  ↓
Assigned Roles (e.g., ADMIN, USER, OPERATOR)
  ↓
Roles Map to Permissions (e.g., dashboard:view, payment:initiate)
  ↓
JWT Token includes Roles
  ↓
JwtAuthenticationFilter extracts Permissions from Roles
  ↓
Spring Security Authorities set to Permissions
  ↓
@PreAuthorize checks Permissions
```

## Core Components

### 1. Permissions Constants (`common/constants/Permissions.java`)

Centralized definition of all permissions in the system:

```java
public final class Permissions {
    // Dashboard Permissions
    public static final String DASHBOARD_VIEW = "dashboard:view";
    public static final String DASHBOARD_STATS = "dashboard:stats";
    public static final String DASHBOARD_PROFILE = "dashboard:profile";
    public static final String DASHBOARD_ADMIN = "dashboard:admin";

    // Tips Service Permissions
    public static final String TIPS_CREATE = "tips:create";
    public static final String TIPS_READ = "tips:read";
    public static final String TIPS_UPDATE = "tips:update";
    public static final String TIPS_DELETE = "tips:delete";
    public static final String TIPS_APPROVE = "tips:approve";
    public static final String TIPS_REPORT = "tips:report";

    // USSD Push Permissions
    public static final String USSD_SEND = "ussd:send";
    public static final String USSD_VIEW = "ussd:view";
    public static final String USSD_HISTORY = "ussd:history";
    public static final String USSD_CONFIG = "ussd:config";

    // Payment Permissions
    public static final String PAYMENT_INITIATE = "payment:initiate";
    public static final String PAYMENT_VIEW = "payment:view";
    public static final String PAYMENT_REFUND = "payment:refund";
    public static final String PAYMENT_REPORT = "payment:report";
    public static final String PAYMENT_RECONCILE = "payment:reconcile";

    // User Management Permissions
    public static final String USER_CREATE = "user:create";
    public static final String USER_READ = "user:read";
    public static final String USER_UPDATE = "user:update";
    public static final String USER_DELETE = "user:delete";
    public static final String USER_LIST = "user:list";

    // Admin Permissions
    public static final String ADMIN_ALL = "admin:all";
    public static final String ADMIN_SYSTEM_CONFIG = "admin:system_config";
    public static final String ADMIN_VIEW_LOGS = "admin:view_logs";
}
```

**Benefits:**
- Single source of truth for all permissions
- Easy to discover available permissions
- Type-safe permission references
- IDE autocomplete support

### 2. Roles Constants (`common/constants/Roles.java`)

Defines roles as groups of permissions:

```java
public final class Roles {
    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";
    public static final String MANAGER = "MANAGER";
    public static final String OPERATOR = "OPERATOR";
    public static final String VIEWER = "VIEWER";

    // Role to Permissions Mapping
    static {
        ROLE_PERMISSIONS.put(ADMIN, Arrays.asList(
            Permissions.ADMIN_ALL,
            Permissions.DASHBOARD_VIEW,
            Permissions.DASHBOARD_STATS,
            Permissions.PAYMENT_INITIATE,
            // ... all permissions
        ));

        ROLE_PERMISSIONS.put(OPERATOR, Arrays.asList(
            Permissions.DASHBOARD_VIEW,
            Permissions.TIPS_CREATE,
            Permissions.USSD_SEND,
            Permissions.PAYMENT_INITIATE
        ));

        ROLE_PERMISSIONS.put(USER, Arrays.asList(
            Permissions.DASHBOARD_VIEW,
            Permissions.DASHBOARD_PROFILE,
            Permissions.TIPS_READ,
            Permissions.PAYMENT_VIEW
        ));
    }

    public static List<String> getPermissions(String role) {
        return ROLE_PERMISSIONS.getOrDefault(role, Collections.emptyList());
    }
}
```

### 3. JWT Token Flow

**Token Generation (Authentication Service):**
```java
// User registers/logs in
User user = userRepository.save(newUser);
user.setRoles("USER,OPERATOR");  // Comma-separated roles

// Generate JWT with roles
List<String> roles = Arrays.asList("USER", "OPERATOR");
String token = jwtUtil.generateToken(user.getEmail(), roles);
```

**Token Contents:**
```json
{
  "sub": "user@example.com",
  "roles": ["USER", "OPERATOR"],
  "iat": 1234567890,
  "exp": 1234654290
}
```

**Token Validation (All Protected Services):**
```java
// JwtAuthenticationFilter extracts token
String token = authHeader.substring(7);
String email = jwtUtil.extractEmail(token);

// Convert roles to permissions
List<String> permissions = jwtUtil.extractPermissions(token);
// Returns: ["dashboard:view", "tips:create", "ussd:send", ...]

// Set as Spring Security authorities
List<GrantedAuthority> authorities = permissions.stream()
    .map(perm -> (GrantedAuthority) () -> perm)
    .collect(Collectors.toList());
```

### 4. Controller Guards

**Using Permission Guards:**
```java
@RestController
@RequestMapping("/api/tips")
public class TipsController {

    @GetMapping
    @PreAuthorize("hasAuthority('" + TIPS_READ + "')")
    public ResponseEntity<List<Tip>> getAllTips() {
        // Only users with tips:read permission can access
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + TIPS_CREATE + "')")
    public ResponseEntity<Tip> createTip(@RequestBody TipRequest request) {
        // Only users with tips:create permission can access
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('" + TIPS_APPROVE + "')")
    public ResponseEntity<Tip> approveTip(@PathVariable Long id) {
        // Only users with tips:approve permission can access
    }
}
```

## Service Implementations

### Dashboard Service (Port 8082)

**Permissions Used:**
- `dashboard:view` - View dashboard
- `dashboard:stats` - View statistics
- `dashboard:profile` - View/edit profile
- `dashboard:admin` - Admin access

**Example Endpoint:**
```java
@GetMapping("/stats")
@PreAuthorize("hasAuthority('" + DASHBOARD_STATS + "')")
public ResponseEntity<DashboardStats> getStats() {
    return ResponseEntity.ok(dashboardService.getStats());
}
```

### Tips Service (Port 8083)

**Permissions Used:**
- `tips:create` - Create tips
- `tips:read` - Read tips
- `tips:update` - Update tips
- `tips:delete` - Delete tips
- `tips:approve` - Approve tips (managers/admins)
- `tips:report` - View reports

**Example Endpoint:**
```java
@PostMapping("/{id}/approve")
@PreAuthorize("hasAuthority('" + TIPS_APPROVE + "')")
public ResponseEntity<TipResponse> approveTip(@PathVariable Long id) {
    // Only ADMIN and MANAGER roles have tips:approve permission
    return ResponseEntity.ok(tipsService.approve(id));
}
```

### USSD Push Service (Port 8084)

**Permissions Used:**
- `ussd:send` - Send USSD push
- `ussd:view` - View USSD details
- `ussd:history` - View history
- `ussd:config` - Configure USSD settings

**Example Endpoint:**
```java
@PostMapping("/send")
@PreAuthorize("hasAuthority('" + USSD_SEND + "')")
public ResponseEntity<UssdPushResponse> sendUssdPush(@RequestBody UssdPushRequest request) {
    // Only OPERATOR and ADMIN can send USSD
    return ResponseEntity.ok(ussdService.send(request));
}
```

### Online Payment Service (Port 8085)

**Permissions Used:**
- `payment:initiate` - Initiate payments
- `payment:view` - View payment details
- `payment:refund` - Process refunds
- `payment:report` - View reports
- `payment:reconcile` - Reconcile payments

**Example Endpoint:**
```java
@PostMapping("/initiate")
@PreAuthorize("hasAuthority('" + PAYMENT_INITIATE + "')")
public ResponseEntity<PaymentResponse> initiatePayment(@RequestBody PaymentInitiateRequest request) {
    // OPERATOR and ADMIN can initiate payments
    return ResponseEntity.ok(paymentService.initiate(request));
}

@PostMapping("/{id}/refund")
@PreAuthorize("hasAuthority('" + PAYMENT_REFUND + "')")
public ResponseEntity<PaymentResponse> refundPayment(@PathVariable Long id) {
    // Only ADMIN can refund payments
    return ResponseEntity.ok(paymentService.refund(id));
}
```

## Role Definitions

### ADMIN
**Has all permissions** - Complete system access

**Use Case:** System administrators, technical staff

### MANAGER
**Permissions:**
- All read/view permissions
- User management (read, update, list)
- Tips management (create, update, approve, report)
- USSD viewing and history
- Payment viewing and reports

**Use Case:** Business managers, supervisors

### OPERATOR
**Permissions:**
- Dashboard view and stats
- Create/update tips
- Send USSD
- Initiate payments and view them

**Use Case:** Day-to-day operators, customer service

### USER
**Permissions:**
- Dashboard view and profile
- Read tips
- View payments

**Use Case:** Regular users, customers

### VIEWER
**Permissions:**
- Dashboard view
- Read tips
- View USSD
- View payments

**Use Case:** Auditors, read-only access

## Adding New Permissions

### Step 1: Define Permission Constant

```java
// In Permissions.java
public static final String NEW_FEATURE_ACTION = "new_feature:action";
```

### Step 2: Assign to Roles

```java
// In Roles.java, add to appropriate roles
ROLE_PERMISSIONS.put(ADMIN, Arrays.asList(
    Permissions.NEW_FEATURE_ACTION,
    // ... other permissions
));
```

### Step 3: Use in Controller

```java
@PostMapping("/new-feature")
@PreAuthorize("hasAuthority('" + NEW_FEATURE_ACTION + "')")
public ResponseEntity<?> newFeatureAction() {
    // Implementation
}
```

## Testing Permissions

### Create Test Users

```java
// Admin user
User admin = User.builder()
    .email("admin@example.com")
    .roles("ADMIN")
    .build();

// Operator user
User operator = User.builder()
    .email("operator@example.com")
    .roles("OPERATOR")
    .build();

// Regular user
User user = User.builder()
    .email("user@example.com")
    .roles("USER")
    .build();
```

### Test Endpoint Access

```bash
# Login as operator
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"operator@example.com","password":"password"}'

# Use token to access USSD endpoint (should work - operator has ussd:send)
curl -X POST http://localhost:8084/api/ussd/send \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber":"+255712345678","message":"Test"}'

# Try to refund payment (should fail - operator doesn't have payment:refund)
curl -X POST http://localhost:8085/api/payments/1/refund \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"amount":1000,"reason":"Test"}'
# Response: 403 Forbidden
```

## Benefits of Permission-Based System

### 1. **Granular Control**
- Fine-grained access control at endpoint level
- Different actions require different permissions
- Flexible role compositions

### 2. **Maintainable**
- Permissions defined in one place (`Permissions.java`)
- Easy to see all available permissions
- Type-safe references prevent typos

### 3. **Flexible Roles**
- Roles are just permission groups
- Easy to create new roles
- Users can have multiple roles (permissions are merged)

### 4. **Clear Security Model**
- `@PreAuthorize` clearly shows required permission
- Easy to audit which permissions are needed
- Self-documenting code

### 5. **Scalable**
- Add new services without changing security infrastructure
- New permissions don't affect existing code
- Easy to extend with new roles

## Common Patterns

### Multiple Permissions (OR)
```java
@PreAuthorize("hasAnyAuthority('" + TIPS_UPDATE + "', '" + TIPS_APPROVE + "')")
public ResponseEntity<?> modifyTip() {
    // User needs either update OR approve permission
}
```

### Multiple Permissions (AND)
```java
@PreAuthorize("hasAuthority('" + PAYMENT_RECONCILE + "') and hasAuthority('" + ADMIN_ALL + "')")
public ResponseEntity<?> criticalOperation() {
    // User needs BOTH permissions
}
```

### Custom Logic
```java
@PreAuthorize("hasAuthority('" + USER_UPDATE + "') or #userId == authentication.name")
public ResponseEntity<?> updateUser(@PathVariable String userId) {
    // User can update if they have permission OR it's their own profile
}
```

## Migration from Role-Based

**Before (Role-Based):**
```java
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> adminAction() { }
```

**After (Permission-Based):**
```java
@PreAuthorize("hasAuthority('" + ADMIN_ALL + "')")
public ResponseEntity<?> adminAction() { }
```

**Benefits:**
- More granular (can give specific admin permissions)
- Clearer intent (shows what permission is needed)
- Flexible (multiple roles can have same permission)

## Summary

- **Permissions** = Granular capabilities (`dashboard:view`, `payment:refund`)
- **Roles** = Groups of permissions (`ADMIN`, `OPERATOR`, `USER`)
- **Constants** = Single source of truth for permissions
- **Guards** = `@PreAuthorize` with permission checks
- **JWT** = Stores roles, converted to permissions at runtime
- **Flexible** = Easy to add services, permissions, and roles
