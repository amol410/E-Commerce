# ShopEase — Full-Stack E-Commerce App

A full-stack e-commerce application built with **Spring Boot 4.0.3**, **Spring Security + JWT**, **MySQL**, and a **Next.js 16 + Tailwind CSS** frontend.

---

## Tech Stack

| Layer      | Technology                                              |
|------------|---------------------------------------------------------|
| Backend    | Spring Boot 4.0.3, Java 21 (runs on Java 24)           |
| Database   | MySQL 8, Hibernate ORM 7.2, H2 (tests)                 |
| Security   | Spring Security 6, JWT (jjwt 0.12.5)                   |
| Mapping    | MapStruct 1.6.3                                         |
| Docs       | Swagger / OpenAPI 3.0 (springdoc 2.8.6)                |
| Testing    | JUnit 5, Mockito 5.14.2, MockMvc — **46 tests passing** |
| Frontend   | Next.js 16.1.6 (App Router), React 19, Tailwind CSS 4  |
| Deployment | Render.com (backend), Vercel (frontend)                 |

---

## Project Structure

```
Ecom/
├── backend/                  # Spring Boot Maven project
│   └── src/
│       ├── main/
│       │   ├── java/com/ecom/ecommerce/
│       │   │   ├── config/        # SecurityConfig, OpenApiConfig, CORS
│       │   │   ├── controller/    # 5 REST controllers
│       │   │   ├── dto/           # Request/Response DTOs
│       │   │   ├── entity/        # JPA entities + enums
│       │   │   ├── exception/     # GlobalExceptionHandler
│       │   │   ├── repository/    # JpaRepository interfaces
│       │   │   ├── security/      # JwtUtil, JwtAuthFilter
│       │   │   └── service/       # Business logic
│       │   └── resources/
│       │       ├── application.properties        # Dev config
│       │       └── application-prod.properties   # Prod config (env vars)
│       └── test/
│           ├── java/              # Unit + MockMvc integration tests
│           └── resources/
│               └── application.properties        # H2 in-memory test config
├── frontend/                 # Next.js project
│   ├── app/                  # App Router pages
│   ├── components/           # Reusable UI (Navbar, ProductCard, etc.)
│   ├── context/              # AuthContext, CartContext
│   └── lib/                  # Utility functions, Axios instance
├── README.md
├── PROCESS.md
├── ISSUES.md
└── TESTING.md
```

---

## API Endpoints

| Method | Endpoint                    | Auth        | Description              |
|--------|-----------------------------|-------------|--------------------------|
| POST   | /api/auth/register          | Public      | Register new user        |
| POST   | /api/auth/login             | Public      | Login — returns JWT      |
| GET    | /api/products               | Public      | List products (filter + paginate) |
| GET    | /api/products/{id}          | Public      | Get product by ID        |
| POST   | /api/products               | ADMIN       | Create product           |
| PUT    | /api/products/{id}          | ADMIN       | Update product           |
| DELETE | /api/products/{id}          | ADMIN       | Delete product           |
| GET    | /api/categories             | Public      | List all categories      |
| GET    | /api/categories/tree        | Public      | Recursive category tree  |
| GET    | /api/categories/{id}        | Public      | Get category by ID       |
| POST   | /api/categories             | ADMIN       | Create category          |
| PUT    | /api/categories/{id}        | ADMIN       | Update category          |
| DELETE | /api/categories/{id}        | ADMIN       | Delete category          |
| GET    | /api/cart                   | USER        | View cart                |
| POST   | /api/cart/add               | USER        | Add item to cart         |
| PUT    | /api/cart/update/{itemId}   | USER        | Update item quantity     |
| DELETE | /api/cart/remove/{itemId}   | USER        | Remove item from cart    |
| DELETE | /api/cart/clear             | USER        | Clear entire cart        |
| POST   | /api/orders/place           | USER        | Place order from cart    |
| GET    | /api/orders/my              | USER        | Order history (paginated)|
| GET    | /api/orders/{id}            | USER        | Get order by ID          |

Swagger UI available at: `http://localhost:8080/swagger-ui.html`

---

## Running Locally

### Prerequisites
- Java 21+ (tested on Java 24.0.2)
- Maven 3.9+
- MySQL 8
- Node.js 18+

### 1. Database Setup

```sql
CREATE DATABASE ecom_db;
```

### 2. Backend

```bash
cd backend
# Edit src/main/resources/application.properties with your MySQL credentials
mvn spring-boot:run
```

Backend runs at `http://localhost:8080`

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at `http://localhost:3000`

---

## Running Tests

```bash
cd backend
mvn test
```

- Uses H2 in-memory DB — no MySQL needed for tests
- 46 tests: 3 service unit test classes + 5 MockMvc controller test classes

---

## Database Schema

| Entity     | Key Fields                                              |
|------------|---------------------------------------------------------|
| User       | id, name, email, password (BCrypt), role               |
| Category   | id, name, description, parent_id (self-referential)    |
| Product    | id, name, description, price, stock, imageUrl, category|
| Cart       | id, user_id (1-to-1 with User)                         |
| CartItem   | id, cart_id, product_id, quantity                      |
| Order      | id, user_id, status, totalAmount, createdAt            |
| OrderItem  | id, order_id, product_id, quantity, priceAtPurchase    |

---

## Deployment

### Backend → Render.com

1. Push code to GitHub
2. Create new Web Service on Render, connect the repo
3. Set build command: `cd backend && mvn package -DskipTests`
4. Set start command: `java -jar backend/target/ecommerce-0.0.1-SNAPSHOT.jar`
5. Add environment variables:
   - `DB_URL` — your MySQL connection string
   - `DB_USERNAME`, `DB_PASSWORD`
   - `JWT_SECRET` — 64+ char hex string
   - `SPRING_PROFILES_ACTIVE=prod`

### Frontend → Vercel

1. Connect GitHub repo to Vercel
2. Set root directory to `frontend`
3. Add environment variable: `NEXT_PUBLIC_API_URL=https://your-backend.onrender.com`
4. Deploy

---

## Development Versions

| Version | Focus                                           |
|---------|-------------------------------------------------|
| v1.0    | Entity design, Product & Category CRUD, Next.js setup |
| v2.0    | Spring Security + JWT, Login/Register UI        |
| v3.0    | Shopping Cart & Order placement                 |
| v4.0    | Filters, pagination, category tree, Swagger, MapStruct DTOs |
| v5.0    | Validation, JUnit5 + MockMvc tests, deployment config |
| v6.0    | Java 24 compatibility, H2 test config, full test suite |
| v7.0    | Spring Boot 4.0.3 upgrade, Hibernate 7, all breaking changes fixed |

See [PROCESS.md](PROCESS.md) for step-by-step build process.
See [ISSUES.md](ISSUES.md) for known issues and solutions.
See [TESTING.md](TESTING.md) for manual testing guide.
