# Teacher Content Portal

A backend system designed to manage and distribute educational content for teachers.
The project focuses on secure authentication, role-based access, and scalable content management.

---

## 🚀 Tech Stack

* **Java 17**
* **Spring Boot**
* **Spring Security**
* **JWT Authentication**
* **MySQL**
* **Maven**
* **AWS S3** (for content storage)

---

## 📌 Project Overview

Teacher Content Portal is designed to allow teachers to securely access and manage educational resources.
The system implements authentication and will later support content uploads, user management, and an optimized frontend interface.

---

## ✅ Current Progress

* [x] Database connection setup
* [x] User entity creation
* [x] User repository
* [x] Service layer for user operations
* [x] JWT Utility
* [x] JWT Filter
* [x] Spring Security Configuration
* [x] Authentication backend implementation

---

## Future Goals

* [ ] Implement **UserController**
* [ ] Implement **ContentController**
* [ ] AWS S3 integration for content storage
* [ ] Optimized **Frontend UI**
* [ ] Role-based authorization improvements
* [ ] Content upload and retrieval APIs
* [ ] Deployment on cloud infrastructure

---

##  Project Structure

```
teachercontentportal
│
├── config/            # Security configuration
├── controller/        # REST controllers
├── dto/               # Data Transfer Objects
├── model/             # Entity classes
├── repository/        # JPA repositories
├── security/          # JWT utilities and filters
├── service/           # Business logic layer
└── application.properties
```

---

## 🔐 Authentication

Authentication is implemented using **JWT (JSON Web Tokens)** with Spring Security.

Flow:

1. User sends login request
2. Credentials are verified
3. JWT token is generated
4. Token is used for authenticated requests

---

## 🛠 Setup Instructions

### Clone the repository

```bash
git clone https://github.com/yourusername/teacher-content-portal.git
```

### Navigate into project

```bash
cd teacher-content-portal
```

### Run the project

```bash
mvn spring-boot:run
```

---
### Author
Neel Shinde  | Harshit Panchariya
