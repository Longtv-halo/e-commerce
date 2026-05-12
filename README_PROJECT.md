# E-Commerce Employee API - Backend Project

**Status:** ✅ Global Exception Handling Configured

---

## 📋 Project Overview

E-commerce backend API với các tính năng quản lý nhân viên (CRUD operations) kèm phân trang, tìm kiếm, validation, và xử lý lỗi toàn cục.

**Stack:**
- Java 17+
- Spring Boot 3.x
- Spring Data JPA
- H2/MySQL (tuỳ config)
- Lombok
- Validation (Jakarta)

---

## 🏗️ Project Structure

```
src/main/java/com/demo/
├── controller/
│   └── EmployeeController.java       # REST endpoints
├── service/
│   └── EmployeeService.java          # Business logic
├── repository/
│   └── EmployeesRepository.java      # Database queries
├── entity/
│   └── Employees.java                # JPA entity
├── dto/
│   ├── BaseRequest.java              # Base request with pagination
│   ├── BaseResponse.java             # Unified response format
│   ├── RequestEmployeeBean.java      # Create/Update request
│   └── ResponseEmployeeBean.java     # Employee response
├── exception/
│   ├── ResourceNotFoundException.java # 404 exception
│   └── BadRequestException.java       # 400 exception
├── advice/
│   └── GlobalExceptionHandler.java   # Global exception handler
└── Demo1Application.java             # Main class
```

---

## 🚀 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| **POST** | `/api/employee/create` | Tạo nhân viên mới |
| **POST** | `/api/employee/list` | Lấy danh sách nhân viên (phân trang) |
| **POST** | `/api/employee/search` | Tìm nhân viên theo tên |
| **PUT** | `/api/employee/{id}` | Cập nhật nhân viên |
| **DELETE** | `/api/employee/{id}` | Xóa nhân viên |

---

## 📦 Request/Response Format

### BaseRequest (Pagination)
```json
{
  "page": 0,
  "size": 10,
  "sortBy": "name",
  "sortDir": "asc"
}
```

### BaseResponse (Success)
```json
{
  "success": true,
  "code": "201",
  "message": "Employee created successfully",
  "data": { ... },
  "meta": { ... },
  "timestamp": "2024-05-07T10:30:00Z"
}
```

### BaseResponse (Error)
```json
{
  "success": false,
  "code": "404",
  "message": "Employee with id 9999 not found",
  "data": null,
  "timestamp": "2024-05-07T10:30:00Z"
}
```

---

## ✅ Validation Rules

| Field | Type | Rules |
|-------|------|-------|
| `empName` | String | @NotBlank — bắt buộc |
| `empEmail` | String | @NotBlank, @Email — đúng format email |
| `empDepartment` | String | Optional |
| `page` | Integer | @Min(0) — >= 0 |
| `size` | Integer | @Min(1), @Max(100) — từ 1-100 |
| `sortBy` | String | Tên field (id, name, email, department) |
| `sortDir` | String | "asc" hoặc "desc" |

---

## 🛠️ Build & Run

### Prerequisites
- JDK 17+
- Maven 3.8.1+

### Build
```bash
mvn clean package -DskipTests
```

### Run
```bash
mvn spring-boot:run
```

Server mặc định chạy ở: `http://localhost:8080`

### Test
```bash
mvn test
```

---

## 🧪 Test API

Sử dụng file `test-api.json` (Postman Collection format):
1. Import vào Postman
2. Set baseUrl variable = `http://localhost:8080`
3. Chạy từng request hoặc runner

**Quick curl test:**
```bash
# Create employee
curl -X POST http://localhost:8080/api/employee/create \
  -H "Content-Type: application/json" \
  -d '{
    "empName": "John Doe",
    "empEmail": "john@example.com",
    "empDepartment": "Engineering"
  }'

# Get all employees
curl -X POST http://localhost:8080/api/employee/list \
  -H "Content-Type: application/json" \
  -d '{"page": 0, "size": 10, "sortBy": "id", "sortDir": "asc"}'

# Search
curl -X POST http://localhost:8080/api/employee/search \
  -H "Content-Type: application/json" \
  -d '{"empName": "John", "page": 0, "size": 10}'

# Update
curl -X PUT http://localhost:8080/api/employee/1 \
  -H "Content-Type: application/json" \
  -d '{"empName": "John Updated", "empEmail": "john@example.com"}'

# Delete
curl -X DELETE http://localhost:8080/api/employee/1
```

---

## 🔧 Global Exception Handler

**File:** `GlobalExceptionHandler.java`

### Exception Types
| Exception | HTTP Status | Use Case |
|-----------|------------|----------|
| `MethodArgumentNotValidException` | 400 | Validation error (@Valid failures) |
| `ResourceNotFoundException` | 404 | Resource not found (employee không tồn tại) |
| `BadRequestException` | 400 | Business logic error |
| `Exception` | 500 | General error |

### Example Error Responses

**Validation Error:**
```json
{
  "success": false,
  "code": "400",
  "message": "Validation error",
  "data": {
    "empName": "Employee name is required",
    "empEmail": "Invalid email format"
  }
}
```

**Not Found:**
```json
{
  "success": false,
  "code": "404",
  "message": "Employee with id 9999 not found"
}
```

---

## 📝 Important Notes

1. **Pagination**: `page` bắt đầu từ 0 (0-indexed)
2. **Search**: Dùng `LIKE %keyword%` — tìm chứa keyword ở bất cứ đâu
3. **Sort**: Hỗ trợ asc/desc cho bất kỳ field nào
4. **Validation**: Validation chuỗi từ controller → service → repository
5. **Exception Handling**: Tất cả exception tự động map về BaseResponse format

---

## 🐛 Troubleshooting

### IDE không nhận exception classes
```bash
# Rebuild Maven project
mvn clean compile

# Hoặc invalidate cache trong IntelliJ
File > Invalidate Caches > Invalidate and Restart
```

### Database connection error
Check `application.yaml` — đảm bảo database config đúng:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ecommerce
    username: root
    password: password
  jpa:
    hibernate:
      ddl-auto: create-drop
```

---

## 📚 References

- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Validation Jakarta](https://jakarta.ee/learn/guides/validation-specification/)
- [REST API Best Practices](https://restfulapi.net/)

---

**Last Updated:** May 7, 2024  
**Version:** 1.0.0

