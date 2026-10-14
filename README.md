# BloodLink India 🇮🇳 - Backend (Spring Boot)

A Spring Boot 3.x REST API backend for the **Blood Donor Finder with Emergency Requests** application, built with Java 17, Spring Security (JWT), Spring Data JPA, and MySQL.

## 🛠 Tech Stack
- **Java 17** + **Spring Boot 3.3.4**
- **Spring Web** – REST API
- **Spring Security 6** – Stateless JWT authentication (BCrypt)
- **Spring Data JPA / Hibernate** – MySQL via JPA
- **MySQL 8.0** – Database (`blood_donor_finder`)
- **Lombok** – Boilerplate reduction
- **Maven** – Build tool

## 🗄️ Database Setup
Ensure MySQL is running locally. Database is auto-created on first run:
```
URL:      jdbc:mysql://localhost:3306/blood_donor_finder
Username: root
Password: root
```

## 🚀 Running Locally
```bash
# Clone the repo
git clone https://github.com/vaishnavikuchi/bloodlink-india-backend.git
cd bloodlink-india-backend

# Run with Maven
mvn spring-boot:run
```
Server starts at: **http://localhost:8081**

## 🔑 Demo Credentials
All seeded users use password: `password123`

## 📡 Key API Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login and get JWT |
| GET | `/api/donors/search` | Public donor search |
| GET | `/api/requests` | List blood requests |
| GET | `/api/stats` | Public statistics |

## 🏗 Project Structure
```
src/main/java/com/blooddonor/
├── controller/     # REST Controllers
├── service/        # Business Logic
├── repository/     # JPA Repositories
├── entity/         # JPA Entities
├── dto/            # Request/Response DTOs
├── security/       # JWT Filter & Config
└── config/         # CORS, Scheduling
```

## 🔗 Frontend Repository
[bloodlink-india-frontend](https://github.com/vaishnavikuchi/bloodlink-india-frontend)
