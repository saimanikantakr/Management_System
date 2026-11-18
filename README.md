# Employee & Department Management System

A production-grade, enterprise-ready RESTful API built with **Java 21**, **Spring Boot 3/4**, **Spring Data JPA**, **Hibernate ORM**, and **MySQL 8**. This project demonstrates clean layered architecture, industry-standard domain modeling, derived & custom queries, bean validation, centralized error handling, and automated test coverage.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Technologies Used](#2-technologies-used)
3. [Prerequisites](#3-prerequisites)
4. [Project Architecture](#4-project-architecture)
5. [Database Design & Relationships](#5-database-design--relationships)
6. [Repository Queries Explanation](#6-repository-queries-explanation)
7. [MySQL Database Setup](#7-mysql-database-setup)
8. [Configuration & Environment Variables](#8-configuration--environment-variables)
9. [Running the Application](#9-running-the-application)
10. [API Endpoint Documentation](#10-api-endpoint-documentation)
11. [Example Request & Response Payloads](#11-example-request--response-payloads)
12. [Postman Collection & Testing Guide](#12-postman-collection--testing-guide)
13. [Automated Testing](#13-automated-testing)

---

## 1. Project Overview

The **Employee & Department Management System** provides RESTful services to manage organizational departments and their employees. 

Key capabilities:
- **Full CRUD** for Employees and Departments.
- **Relational Integrity**: Bidirectional `@OneToMany` and `@ManyToOne` associations with helper methods keeping both sides synchronized in memory and the database.
- **Advanced Querying**:
  - Spring Data JPA Derived Query Methods.
  - Custom JPQL Queries with `@Query`.
  - Vendor-optimized Native SQL queries.
- **Pagination & Dynamic Sorting**: Page metadata (`totalElements`, `totalPages`, `currentPage`, `pageSize`, `isLast`) and sorting whitelisting.
- **Jakarta Bean Validation**: Declarative input validation (`@NotBlank`, `@Email`, `@Positive`, `@Size`, etc.).
- **Robust Exception Handling**: Global `@RestControllerAdvice` returning structured `ErrorResponse` objects with sanitized messages and specific HTTP status codes (200, 201, 204, 400, 404, 409, 500).
- **Anti-Corruption Layer (DTOs & Mappers)**: JPA entities are never exposed across the network boundary, eliminating recursive JSON serialization loops and leaking internal schema details.

---

## 2. Technologies Used

- **Java**: Version 21 (LTS)
- **Framework**: Spring Boot (Spring Web, Spring Data JPA)
- **ORM / Persistence**: Hibernate ORM
- **Database**: MySQL 8.x (Production/Local), H2 In-Memory (Automated Testing)
- **Validation**: Jakarta Bean Validation (`hibernate-validator` 9.x)
- **Build Tool**: Apache Maven 3.9+
- **Testing**: JUnit 5, Mockito, Spring Boot Test, MockMvc
- **API Client**: Postman (v2.1 Collection provided)

---

## 3. Prerequisites

Before running the application, ensure the following are installed on your machine:
- **JDK 21** or later (`java -version`)
- **MySQL Server 8.0+** running locally or accessible remotely
- **Maven 3.9+** (or use the provided Maven wrapper)
- **Postman** (optional, for manual API exploration)

---

## 4. Project Architecture

The application strictly follows a clean layered architecture:

```text
src/main/java/com/example/employeemanagement/
│
├── EmployeeManagementApplication.java          # Spring Boot Main Entry Point
│
├── controller/                                 # REST Controllers (Thin API Layer)
│   ├── EmployeeController.java
│   └── DepartmentController.java
│
├── service/                                    # Service Contracts
│   ├── EmployeeService.java
│   ├── DepartmentService.java
│   └── impl/                                   # Business Logic Implementations
│       ├── EmployeeServiceImpl.java
│       └── DepartmentServiceImpl.java
│
├── repository/                                 # Spring Data JPA Repositories
│   ├── EmployeeRepository.java
│   └── DepartmentRepository.java
│
├── entity/                                     # JPA Entities
│   ├── Employee.java
│   └── Department.java
│
├── dto/                                        # Data Transfer Objects
│   ├── request/
│   │   ├── EmployeeRequestDTO.java
│   │   ├── DepartmentRequestDTO.java
│   │   └── AssignEmployeeDTO.java
│   │
│   └── response/
│       ├── EmployeeResponseDTO.java
│       ├── DepartmentResponseDTO.java
│       └── PageResponseDTO.java
│
├── exception/                                  # Centralized Exception Handling
│   ├── ResourceNotFoundException.java
│   ├── DuplicateResourceException.java
│   ├── GlobalExceptionHandler.java
│   └── ErrorResponse.java
│
└── mapper/                                     # Manual Entity <-> DTO Mappers
    ├── EmployeeMapper.java
    └── DepartmentMapper.java
```

---

## 5. Database Design & Relationships

### `Department` Entity (`departments` table)
- `id` (BIGINT, Primary Key, Auto Increment)
- `name` (VARCHAR(100), NOT NULL)
- `location` (VARCHAR(100), NOT NULL)
- `employees` (`@OneToMany(mappedBy = "department", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)`)

### `Employee` Entity (`employees` table)
- `id` (BIGINT, Primary Key, Auto Increment)
- `name` (VARCHAR(100), NOT NULL)
- `email` (VARCHAR(120), NOT NULL, UNIQUE)
- `salary` (DOUBLE PRECISION, NOT NULL)
- `department` (`@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "department_id")`)

### Key Relationship Highlights:
1. **Bidirectional Synchronization**:
   The `Department` entity includes `addEmployee(Employee)` and `removeEmployee(Employee)` convenience methods to keep both sides of the relationship in sync in Java memory.
2. **Preventing Infinite JSON Recursion**:
   JPA entities are never serialized directly to JSON. The `EmployeeResponseDTO` contains `departmentId` and `departmentName`. The `DepartmentResponseDTO` embeds a list of `EmployeeResponseDTO` objects, ensuring zero circular references.
3. **Preventing N+1 Queries**:
   The `DepartmentRepository` uses `findByIdWithEmployees` with `LEFT JOIN FETCH d.employees` to fetch a department and its employees in a single query when requested by ID.

---

## 6. Repository Queries Explanation

The `EmployeeRepository` demonstrates the 3 primary querying mechanisms in Spring Data JPA:

| Query Type | Example | When to Use | Advantages & Limitations |
| :--- | :--- | :--- | :--- |
| **Derived Query** | `findBySalaryGreaterThan(Double salary)`<br>`findByNameContainingIgnoreCase(String name)`<br>`findBySalaryBetween(Double min, Double max)`<br>`findByDepartmentId(Long departmentId)` | Standard CRUD filters on entity attributes | **Pros:** Zero SQL boilerplate, automatically validated by Spring Data.<br>**Cons:** Method names become cumbersome for complex multi-table joins. |
| **JPQL (`@Query`)** | `@Query("SELECT e FROM Employee e WHERE e.salary > :salary ORDER BY e.salary DESC")` | Complex entity-level queries across relationships | **Pros:** Object-oriented, database-agnostic, validated at application startup.<br>**Cons:** Cannot invoke vendor-specific SQL functions or native database engine hints. |
| **Native SQL** | `@Query(value = "SELECT * FROM employees WHERE department_id = :departmentId", nativeQuery = true)` | Performance-critical joins, database-specific functions | **Pros:** Complete access to native database capabilities, indexed query hints.<br>**Cons:** Vendor-locked; queries are not verified at startup. |

---

## 7. MySQL Database Setup

1. Start your local MySQL server.
2. Log into the MySQL client:
   ```bash
   mysql -u root -p
   ```
3. Create the database:
   ```sql
   CREATE DATABASE IF NOT EXISTS employee_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
4. Verify user permissions:
   ```sql
   GRANT ALL PRIVILEGES ON employee_management.* TO 'root'@'localhost';
   FLUSH PRIVILEGES;
   ```

Hibernate `ddl-auto=update` will automatically create the tables and constraints on startup.

---

## 8. Configuration & Environment Variables

Database credentials are parameterized using Spring environment variable placeholders in `src/main/resources/application.properties`:

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/employee_management?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:password}
server.port=${SERVER_PORT:8080}
```

### Setting Environment Variables

A `.env.example` file is included in the project root:

```bash
# Windows PowerShell
$env:DB_URL="jdbc:mysql://localhost:3306/employee_management?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_password"
$env:SERVER_PORT="8080"

# Linux / macOS Bash
export DB_URL="jdbc:mysql://localhost:3306/employee_management?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export DB_USERNAME="root"
export DB_PASSWORD="your_password"
export SERVER_PORT="8080"
```

---

## 9. Running the Application

### A. Backend (Spring Boot)
```bash
# Compile and package
mvn clean package

# Run the packaged executable JAR
java -jar target/employeemanagement-0.0.1-SNAPSHOT.jar

# Or run directly via wrapper
.\mvnw.cmd spring-boot:run
```
Backend will be available at `http://localhost:8082` (or your configured port).

### B. Frontend (Angular + Bootstrap 5)
```bash
cd frontend
npm start
```
Frontend will be available at `http://localhost:4200`.

---

## 10. API Endpoint Documentation

### Department Endpoints (`/api/departments`)

| Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/departments` | Create new department | `201 CREATED` |
| `GET` | `/api/departments` | List all departments | `200 OK` |
| `GET` | `/api/departments/{id}` | Get department by ID with associated employees | `200 OK` / `404 NOT FOUND` |
| `PUT` | `/api/departments/{id}` | Update department details | `200 OK` / `404 NOT FOUND` |
| `DELETE` | `/api/departments/{id}` | Delete department by ID | `204 NO CONTENT` / `404 NOT FOUND` |
| `POST` | `/api/departments/{deptId}/employees/{empId}` | Assign employee to department (path variables) | `200 OK` / `404 NOT FOUND` |
| `POST` | `/api/departments/{deptId}/employees` | Assign employee to department (request body) | `200 OK` / `404 NOT FOUND` |
| `GET` | `/api/departments/{deptId}/employees` | Get all employees belonging to department | `200 OK` / `404 NOT FOUND` |

### Employee Endpoints (`/api/employees`)

| Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/employees` | Create new employee | `201 CREATED` / `400` / `409` |
| `GET` | `/api/employees` | List all employees with department info | `200 OK` |
| `GET` | `/api/employees/{id}` | Get employee by ID | `200 OK` / `404 NOT FOUND` |
| `PUT` | `/api/employees/{id}` | Update employee | `200 OK` / `400` / `404` / `409` |
| `DELETE` | `/api/employees/{id}` | Delete employee by ID | `204 NO CONTENT` / `404 NOT FOUND` |
| `GET` | `/api/employees/paginated?page=0&size=5&sortBy=name&direction=asc` | Paginated & sorted employee listing | `200 OK` / `400 BAD REQUEST` |
| `GET` | `/api/employees/search/name?keyword=kumar` | Search employees by name keyword (Derived) | `200 OK` |
| `GET` | `/api/employees/search/salary?min=50000` | Search employees with salary > min (Derived) | `200 OK` |
| `GET` | `/api/employees/search/salary/sorted?min=50000` | Search employees with salary > min sorted DESC (JPQL) | `200 OK` |
| `GET` | `/api/employees/search/salary-range?min=40000&max=80000` | Search employees in salary range (Derived) | `200 OK` / `400 BAD REQUEST` |
| `GET` | `/api/employees/department/{departmentId}` | Employees by department ID (Derived) | `200 OK` / `404 NOT FOUND` |
| `GET` | `/api/employees/department/{departmentId}/native` | Employees by department ID (Native SQL) | `200 OK` / `404 NOT FOUND` |

---

## 11. Example Request & Response Payloads

### 1. Create Department
**Request (`POST /api/departments`):**
```json
{
  "name": "Engineering",
  "location": "Bangalore"
}
```

**Response (`201 CREATED`):**
```json
{
  "id": 1,
  "name": "Engineering",
  "location": "Bangalore",
  "employees": []
}
```

---

### 2. Create Employee
**Request (`POST /api/employees`):**
```json
{
  "name": "Rahul Kumar",
  "email": "rahul@gmail.com",
  "salary": 45000,
  "departmentId": 1
}
```

**Response (`201 CREATED`):**
```json
{
  "id": 1,
  "name": "Rahul Kumar",
  "email": "rahul@gmail.com",
  "salary": 45000.0,
  "departmentId": 1,
  "departmentName": "Engineering"
}
```

---

### 3. Get Department By ID (with associated employees)
**Request (`GET /api/departments/1`):**

**Response (`200 OK`):**
```json
{
  "id": 1,
  "name": "Engineering",
  "location": "Bangalore",
  "employees": [
    {
      "id": 1,
      "name": "Rahul Kumar",
      "email": "rahul@gmail.com",
      "salary": 45000.0,
      "departmentId": 1,
      "departmentName": "Engineering"
    }
  ]
}
```

---

### 4. Paginated & Sorted Employees
**Request (`GET /api/employees/paginated?page=0&size=2&sortBy=salary&direction=desc`):**

**Response (`200 OK`):**
```json
{
  "currentPage": 0,
  "pageSize": 2,
  "totalElements": 5,
  "totalPages": 3,
  "last": false,
  "content": [
    {
      "id": 2,
      "name": "Priya Sharma",
      "email": "priya@gmail.com",
      "salary": 90000.0,
      "departmentId": 1,
      "departmentName": "Engineering"
    },
    {
      "id": 1,
      "name": "Rahul Kumar",
      "email": "rahul@gmail.com",
      "salary": 45000.0,
      "departmentId": 1,
      "departmentName": "Engineering"
    }
  ]
}
```

---

### 5. Error Responses

#### A. Resource Not Found (`404 NOT FOUND`)
```json
{
  "timestamp": "2026-11-05T12:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Employee not found with id: '999'",
  "path": "/api/employees/999"
}
```

#### B. Validation Error (`400 BAD REQUEST`)
```json
{
  "timestamp": "2026-11-05T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Salary must be greater than zero",
  "path": "/api/employees",
  "validationErrors": {
    "salary": "Salary must be greater than zero",
    "email": "Email must be valid"
  }
}
```

#### C. Duplicate Conflict (`409 CONFLICT`)
```json
{
  "timestamp": "2026-11-05T12:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Employee already exists with email: 'rahul@gmail.com'",
  "path": "/api/employees"
}
```

---

## 12. Postman Collection & Testing Guide

An importable Postman collection file is included at the root of the project:
`Employee_Department_Management_System.postman_collection.json`

### Importing into Postman:
1. Open Postman.
2. Click **Import** in the upper left corner.
3. Drag & drop or select `Employee_Department_Management_System.postman_collection.json`.
4. Ensure the collection variable `baseUrl` is set to `http://localhost:8080`.

### Included Request Suites:
- **Department Management**: Create, Read All, Read by ID, Update, Delete, Assign Employee.
- **Employee CRUD & Repository Queries**: Create, Read All, Read by ID, Update, Delete, Paginated Search, Name Search, Salary Threshold Search, JPQL Sorted Search, Salary Range Search, Department Search (Derived & Native).
- **Negative Test Cases**:
  - Nonexistent employee ID (404)
  - Nonexistent department ID (404)
  - Duplicate email conflict (409)
  - Invalid email format (400)
  - Blank employee name (400)
  - Negative salary (400)
  - Nonexistent department foreign key (404)
  - Negative page number (400)
  - Unwhitelisted sort property (400)
  - Inverted salary range (min > max) (400)

---

## 13. Automated Testing

The project includes **53 automated tests** covering:
- **Service Layer**: Business logic isolation with Mockito (`EmployeeServiceTest`, `DepartmentServiceTest`).
- **Repository Layer**: Query method tests on embedded H2 database in MySQL mode (`EmployeeRepositoryTest`), validating Derived Queries, JPQL queries, and Native queries.
- **Controller Layer**: REST endpoints, status codes, JSON mappings, and bean validation with MockMvc (`EmployeeControllerTest`, `DepartmentControllerTest`).
- **Exception Handling**: Global exception handler scenarios.

### Running Automated Tests
```bash
mvn test
```

All 53 tests run and pass cleanly:
```text
[INFO] Results:
[INFO] 
[INFO] Tests run: 53, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] BUILD SUCCESS
```

