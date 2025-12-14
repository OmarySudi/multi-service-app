# API Examples

This document provides examples of how to use the authentication and dashboard APIs.

## Authentication Service (Port 8081)

### 1. Register a New User

```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "email": "john@example.com",
  "message": "User registered successfully"
}
```

### 2. Login

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "email": "john@example.com",
  "message": "Login successful"
}
```

### 3. Validate Token

```bash
curl -X GET http://localhost:8081/api/auth/validate \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response:**
```json
{
  "valid": true,
  "email": "john@example.com",
  "message": "Token is valid"
}
```

## Dashboard Service (Port 8082)

### 1. Health Check (Public - No Authentication Required)

```bash
curl -X GET http://localhost:8082/api/dashboard/public/health
```

**Response:**
```
Dashboard Service is running!
```

### 2. Get Dashboard Statistics (Protected)

```bash
curl -X GET http://localhost:8082/api/dashboard/stats \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response:**
```json
{
  "totalUsers": 1250,
  "activeUsers": 845,
  "totalOrders": 3420,
  "revenue": 125430.50,
  "lastUpdated": "2024-01-15T10:30:45"
}
```

### 3. Get User Profile (Protected)

```bash
curl -X GET http://localhost:8082/api/dashboard/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response:**
```json
{
  "email": "john@example.com",
  "role": "USER",
  "message": "Profile fetched successfully"
}
```

## Complete Workflow Example

### Step 1: Register a new user
```bash
RESPONSE=$(curl -s -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "test123456"
  }')

echo $RESPONSE
```

### Step 2: Extract the token from the response
```bash
TOKEN=$(echo $RESPONSE | grep -o '"token":"[^"]*' | sed 's/"token":"//')
echo "Token: $TOKEN"
```

### Step 3: Use the token to access protected endpoints
```bash
curl -X GET http://localhost:8082/api/dashboard/stats \
  -H "Authorization: Bearer $TOKEN"

curl -X GET http://localhost:8082/api/dashboard/profile \
  -H "Authorization: Bearer $TOKEN"
```

## Error Responses

### Authentication Failed
```json
{
  "timestamp": "2024-01-15T10:30:45",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired token"
}
```

### Validation Error
```json
{
  "timestamp": "2024-01-15T10:30:45",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "email": "Invalid email format",
    "password": "Password must be at least 6 characters"
  }
}
```

### User Already Exists
```json
{
  "timestamp": "2024-01-15T10:30:45",
  "status": 400,
  "error": "Bad Request",
  "message": "Email already exists"
}
```

## Testing with Postman

You can also test these APIs using Postman:

1. Import the endpoints listed above
2. Create a collection for Authentication Service and another for Dashboard Service
3. Use environment variables to store the JWT token
4. Set up a test script in the login request to automatically save the token:

```javascript
var jsonData = pm.response.json();
pm.environment.set("jwt_token", jsonData.token);
```

5. Use `{{jwt_token}}` in the Authorization header for protected endpoints
